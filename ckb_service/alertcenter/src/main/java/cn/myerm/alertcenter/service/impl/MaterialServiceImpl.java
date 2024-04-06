package cn.myerm.alertcenter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.myerm.alertcenter.entity.Material;
import cn.myerm.alertcenter.entity.MaterialLib;
import cn.myerm.alertcenter.mapper.MaterialMapper;
import cn.myerm.alertcenter.service.IMaterialService;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class MaterialServiceImpl extends ServiceImpl<MaterialMapper, Material> implements IMaterialService {
    private static final Logger logger = LoggerFactory.getLogger(MaterialServiceImpl.class);

    private final MaterialLibServiceImpl materialLibService;
    private final OpenAIServiceImpl openAiService;

    @Autowired
    public MaterialServiceImpl(MaterialLibServiceImpl materialLibService, OpenAIServiceImpl openAiService) {
        this.materialLibService = materialLibService;
        this.openAiService = openAiService;
    }

    public synchronized void generateAiContent() {
        QueryWrapper<Material> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.inSql("MaterialLibId", "SELECT lId FROM `MaterialLib` WHERE bEnable=1 AND bAIEnable=1");
        objectQueryWrapper.ne("bAIGenerated", 1);
        objectQueryWrapper.orderByDesc("bAIGenerated", "dNewTime");
        objectQueryWrapper.last("LIMIT 5");

        List<Material> listMaterial = list(objectQueryWrapper);
        ExecutorService executor = Executors.newFixedThreadPool(5); // 创建一个包含5个线程的线程池

        for (Material material : listMaterial) {
            if (StrUtil.isEmpty(material.getSContent())) {
                material.setBAIGenerated(-1);
                updateById(material);
            } else {
                executor.submit(() -> {
                    try {
                        logger.info("正在生成AI：" + material.getLId());

                        MaterialLib materialLib = materialLibService.getById(material.getMaterialLibId());
                        List<ChatCompletionChoice> aiResultList = openAiService.getAiResult(material.getSContent() +
                                "\n------------\n注意：虚线以上是原文，虚线以下是提示词，虚线及虚线以下内容都不要出现在生成的内容中。\n" + materialLib.getSAIRequire());

                        List<String> arrContent = new ArrayList<>();
                        for (ChatCompletionChoice chatCompletionChoice : aiResultList) {
                            String content = chatCompletionChoice.getMessage().getContent();
                            arrContent.add(StrUtil.trim(content));
                        }

                        material.setSContentAI(JSONArray.toJSONString(arrContent));
                        material.setBAIGenerated(1);
                        updateById(material);

                        logger.info("AI生成完毕：" + material.getLId());
                    } catch (Exception e) {
                        logger.error("生成AI内容时发生错误：" + e.getMessage());
                    }
                });
            }
        }

        executor.shutdown(); // 不再接受新任务，如果先前提交的任务还没完成，则继续执行直到完成

        try {
            // 等待直到所有任务完成，或者超时，或者线程被中断
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                // 超时后关闭所有正在执行的任务
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // 如果等待被中断，重新设置中断状态
            Thread.currentThread().interrupt();
            // 立即关闭所有任务
            executor.shutdownNow();
        }
    }
}
