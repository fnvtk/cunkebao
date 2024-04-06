package cn.myerm.alertcenter.alert;

import cn.hutool.core.date.DateUtil;
import cn.myerm.alertcenter.service.impl.EventServiceImpl;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class RedisQueueConsumer {

    private static final Logger logger = LoggerFactory.getLogger(RedisQueueConsumer.class);
    private final RedisTemplate<String, String> redisTemplate;
    private final AlertCenter alertCenter;
    private final EventServiceImpl eventService;

    @Value("${spring.redis.queuekey}")
    private String queueKey;

    @Value("${spring.redis.eventkey}")
    private String eventkey;

    @Autowired
    public RedisQueueConsumer(RedisTemplate<String, String> redisTemplate, AlertCenter alertCenter, EventServiceImpl eventService) {
        this.redisTemplate = redisTemplate;
        this.alertCenter = alertCenter;
        this.eventService = eventService;
    }

    /**
     * 事件处理
     * @throws IOException
     */
    public void consumeEvent() throws IOException {
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        Long size = 0L;
        try {
            size = listOps.size(eventkey);
        } catch (Exception e) {
            logger.error(e.getMessage());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appname", "myerm");
            jsonObject.put("end", "service");
            jsonObject.put("host", "master");
            jsonObject.put("position", "business/alertcenter/redis");
            jsonObject.put("msg", "消息中间件redis出现故障");
            jsonObject.put("detail", e.getMessage());
            jsonObject.put("level", 2);
            jsonObject.put("type", "error");
            jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
            jsonObject.put("env", "{}");
            alertCenter.handleMsg(jsonObject.toJSONString());
            return;
        }

        if (size > 0) {
            List<String> dataList = listOps.range(eventkey, 0, size - 1);
            for (String data : dataList) {
                final JSONObject jsonObject = JSONObject.parseObject(data);
                eventService.newSave(jsonObject);
                listOps.trim(eventkey, 1, -1);
            }
        }
    }

    public void consumeAllFromQueue() throws IOException {
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        Long size;
        try {
            size = listOps.size(queueKey);
        } catch (Exception e) {
            logger.error(e.getMessage());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("appname", "myerm");
            jsonObject.put("end", "service");
            jsonObject.put("host", "master");
            jsonObject.put("position", "business/alertcenter/redis");
            jsonObject.put("msg", "消息中间件redis出现故障");
            jsonObject.put("detail", e.getMessage());
            jsonObject.put("level", 2);
            jsonObject.put("type", "error");
            jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
            jsonObject.put("env", "{}");
            alertCenter.handleMsg(jsonObject.toJSONString());
            return;
        }

        if (size > 0) {
            List<String> dataList = listOps.range(queueKey, 0, size - 1);

            // 处理数据
            for (String data : dataList) {
                try {
                    if (alertCenter.handleMsg(data)) {
                        // 删除已处理的数据
                        listOps.trim(queueKey, 1, -1);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());

                    //记录报错信息
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("appname", "myerm");
                    jsonObject.put("end", "service");
                    jsonObject.put("host", "master");
                    jsonObject.put("position", "business/alertcenter/handlemsg");
                    jsonObject.put("msg", "故障信息保存到ES失败");
                    jsonObject.put("detail", e.getMessage());
                    jsonObject.put("level", 3);
                    jsonObject.put("type", "error");
                    jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
                    jsonObject.put("env", "{}");
                    alertCenter.handleMsg(jsonObject.toJSONString());
                }
            }
        }
    }
}
