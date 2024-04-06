package cn.myerm.system.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.config.Businessconfig;
import cn.myerm.common.exception.SystemException;
import cn.myerm.system.entity.SysAttach;
import cn.myerm.system.mapper.SysAttachMapper;
import cn.myerm.system.service.ISysAttachService;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-04-16
 */
@Service
public class SysAttachServiceImpl extends BusinessServiceImpl<SysAttachMapper, SysAttach> implements ISysAttachService {

    private static final Logger logger = LoggerFactory.getLogger(SysAttachServiceImpl.class);

    @Resource
    private SysSessionServiceImpl sysSessionService;

    @Resource
    private OSS ossClient;

    @Resource
    private Businessconfig businessconfig;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Value("${spring.redis.queuekey}")
    private String queueKey;

    @Value("${oss.bucket}")
    private String bucketName;

    @Value("${oss.endpoint}")
    private String endpoint;

    public SysAttach newSave(String sObjectName, String sFileName, String sOriginalFilename) {
        SysAttach sysAttach = new SysAttach();
        sysAttach.setSName(sOriginalFilename);
        sysAttach.setSObjectName(sObjectName);
        sysAttach.setSFilePath(sFileName);
        sysAttach.setDNewTime(LocalDateTime.now());
        sysAttach.setNewUserId(sysSessionService.getCurrUser().getLID());
        sysAttach.setBImage(isImage(sFileName) ? 1 : 0);

        save(sysAttach);

        return sysAttach;
    }

    public Boolean isImage(String sFileName) {
        String sFileNameExt = sFileName.substring(sFileName.lastIndexOf(".") + 1);
        if (sFileNameExt.equals("jpg") || sFileNameExt.equals("jpeg") || sFileNameExt.equals("png") || sFileNameExt.equals("gif") || sFileNameExt.equals("webp")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 通过id串获得
     *
     * @param ids
     * @return
     */
    public List<SysAttach> getByIds(String[] ids) {
        if (ids == null || ids.length == 0) {
            return null;
        } else {
            QueryWrapper<SysAttach> objectQueryWrapper = new QueryWrapper<>();
            objectQueryWrapper.in("lID", Arrays.asList(ids));
            return list(objectQueryWrapper);
        }
    }

    public SysAttach getById(Integer id) {
        SysAttach sysAttach = super.getById(id);

        if (StrUtil.isEmpty(sysAttach.getSCdnUrl())) {
            if (!sysAttach.getSFilePath().contains("http://") && !sysAttach.getSFilePath().contains("https://")) {
                sysAttach.setSCdnUrl(businessconfig.getUploadurl() + "/" + sysAttach.getSFilePath());
            } else {
                sysAttach.setSCdnUrl(sysAttach.getSFilePath());
            }
        }

        return sysAttach;
    }

    /**
     * 把附件上传到oss
     */
    public synchronized void putOss() {
        QueryWrapper<SysAttach> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("sCdnUrl");
        List<SysAttach> sysAttachList = list(queryWrapper);
        for (SysAttach sysAttach : sysAttachList) {
            try {

                //远程文件，没有保存在本地。就需要先把它下载到本地。
                if (sysAttach.getSFilePath().startsWith("http://") || sysAttach.getSFilePath().startsWith("https://")) {
                    String sDirPath = DateUtil.format(DateUtil.date(), "yyyy/MM/dd");
                    File tmpFolder = new File(businessconfig.getUploadsavepath() + "/" + sDirPath);
                    if (!tmpFolder.exists()) {
                        if (!tmpFolder.mkdirs()) {
                            throw new SystemException("临时文件夹创建失败");
                        }
                    }

                    String sImgUrl = sysAttach.getSFilePath();
                    boolean bDownloadSuccess = false;
                    int lTry = 1;
                    while (true) {
                        try {
                            // 创建URI对象
                            URI uri = URI.create(sImgUrl);

                            // 转换为URL对象
                            URL url = uri.toURL();

                            // 打开连接
                            InputStream inputStream = url.openStream();

                            // 设置目标路径
                            sysAttach.setSFilePath(sDirPath + "/" + sysAttach.getSFilePath().substring(sysAttach.getSFilePath().lastIndexOf("/") + 1));
                            Path destination = Path.of(businessconfig.getUploadsavepath() + '/' + sysAttach.getSFilePath());

                            // 下载并保存图片
                            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);

                            logger.info("图片下载完成:" + uri.getPath());

                            bDownloadSuccess = true;
                            break;
                        } catch (IOException e) {
                            logger.error("图片下载失败: " + e.getMessage());
                            lTry++;
                        }

                        if (lTry > 5) {
                            break;
                        }
                    }

                    if (!bDownloadSuccess) {
                        sysAttach.setSFilePath(sImgUrl);
                        sysAttach.setSCdnUrl(sImgUrl);
                        updateById(sysAttach);

                        continue;
                    }
                }

                InputStream inputStream = new FileInputStream(businessconfig.getUploadsavepath() + '/' + sysAttach.getSFilePath());

                // 创建PutObjectRequest对象。
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, sysAttach.getSFilePath(), inputStream);

                // 创建PutObject请求。
                PutObjectResult result = ossClient.putObject(putObjectRequest);

                String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + sysAttach.getSFilePath();
                sysAttach.setSCdnUrl(url);
                sysAttach.setSFilePath(url);

                //删除源文件
                File file = new File(businessconfig.getUploadsavepath() + '/' + sysAttach.getSFilePath());
                file.delete();

                updateById(sysAttach);
            } catch (OSSException | ClientException | FileNotFoundException oe) {
                //提交到预警中心
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appname", "ckbservice");
                jsonObject.put("end", "service");
                jsonObject.put("host", "service");
                jsonObject.put("position", "sysattach");
                jsonObject.put("msg", oe.getMessage());
                jsonObject.put("level", 2);
                jsonObject.put("detail", oe.getMessage());
                jsonObject.put("type", "error");
                jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
                jsonObject.put("env", "{}");
                redisTemplate.opsForList().rightPush(queueKey, jsonObject.toJSONString());
            }
        }
    }

    /**
     * 批量获取cdn url
     *
     * @param arrAttachId
     * @return
     */
    public List<String> getCdnUrls(String[] arrAttachId) {
        List<String> listUrl = new ArrayList<>();
        List<SysAttach> sysAttachList = listByIds(Arrays.stream(arrAttachId).toList());
        for (SysAttach sysAttach : sysAttachList) {
            listUrl.add(sysAttach.getSCdnUrl());
        }

        return listUrl;
    }

    public String getCdnUrl(Integer sAttachId) {
        SysAttach sysAttach = getById(sAttachId);
        return sysAttach.getSCdnUrl();
    }

    public String getCdnUrl(String sAttachId) {
        SysAttach sysAttach = getById(sAttachId);
        return sysAttach.getSCdnUrl();
    }
}
