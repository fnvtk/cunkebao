package cn.myerm.business.collect;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.myerm.business.entity.Material;
import cn.myerm.business.entity.MaterialLib;
import cn.myerm.business.service.impl.MaterialLibServiceImpl;
import cn.myerm.business.service.impl.MaterialServiceImpl;
import cn.myerm.common.exception.SystemException;
import cn.myerm.system.entity.SysAttach;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LongXiaGame {

    private static final Logger logger = LoggerFactory.getLogger(LongXiaGame.class);

    private final MaterialLibServiceImpl materialLibService;
    private final RestTemplate restTemplate;
    private final MaterialServiceImpl materialService;
    private final SysAttachServiceImpl sysAttachService;

    @Autowired
    public LongXiaGame(MaterialLibServiceImpl materialLibService, RestTemplate restTemplate, MaterialServiceImpl materialService, SysAttachServiceImpl sysAttachService) {
        this.materialLibService = materialLibService;
        this.restTemplate = restTemplate;
        this.materialService = materialService;
        this.sysAttachService = sysAttachService;
    }

    public static String extractImageName(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlashIndex + 1);
        }
        return "";
    }

    /**
     * 执行采集
     */
    public synchronized void execCollect() {
        logger.info("开始拉取龙虾网的数据");

        QueryWrapper<MaterialLib> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("bEnable", 1);
        List<MaterialLib> materialLibList = materialLibService.list(objectQueryWrapper);

        for (MaterialLib materialLib : materialLibList) {
            JSONObject jsonObject = JSONObject.parseObject(materialLib.getSConfigJson());
            if (jsonObject.getJSONArray("longxiagame") != null) {
                JSONArray arrCode = jsonObject.getJSONArray("longxiagame");
                for (int i = 0; i < arrCode.size(); i++) {
                    String sCode = arrCode.getString(i);

                    int lPage = 1;
                    while (true) {
                        Map<String, Object> mapParam = new HashMap<>();
                        mapParam.put("shop", 29);
                        mapParam.put("name", materialLib.getSName());
                        mapParam.put("openid", sCode);
                        mapParam.put("Page", lPage);
                        mapParam.put("state", 2);
                        mapParam.put("Cont", "");
                        mapParam.put("qu", "");
                        mapParam.put("fu", "");
                        mapParam.put("cookie", "4804e619d898ca7f972e5dae20be3f86");
                        mapParam.put("price", "0,1000000");

                        JSONArray arrResult = request("https://web.api.yx915.com/kcData/Data/Lists", mapParam);

                        if (arrResult.getJSONObject(0).getString("data") != null) {
                            logger.info(arrResult.getJSONObject(0).toJSONString());
                            break;
                        }

                        JSONArray arrProd = arrResult.getJSONObject(0).getJSONArray("Shangpin");
                        for (int j = 0; j < arrProd.size(); j++) {
                            JSONObject prod = arrProd.getJSONObject(j);

                            String sSourceDataId = "/longxiagame/" + sCode + "/" + prod.getString("Code");
                            QueryWrapper<Material> materialQueryWrapper = new QueryWrapper<>();
                            materialQueryWrapper.eq("SourceDataId", sSourceDataId);
                            materialQueryWrapper.last("LIMIT 1");
                            Material material = materialService.getOne(materialQueryWrapper);
                            if (material == null) {
                                material = new Material();
                            } else {
                                continue;//已经入库了，不必重复入库
                            }

                            material.setSName(prod.getString("Title"));

                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d H:mm:ss");
                            LocalDateTime dateTime = LocalDateTime.parse(prod.getString("Date"), formatter);
                            material.setDNewTime(dateTime);

                            material.setDEditTime(LocalDateTime.now());
                            material.setDCollectTime(LocalDateTime.now());
                            material.setSourceId("longxiagame");
                            material.setTypeId(2);
                            material.setSContent(prod.getString("Description") + "\n\nhttp://zh.lytiao.com/Game_Contentpage.html?code=" + prod.getString("Code") + "," + sCode);
                            material.setSSourceContent(prod.toJSONString());

                            int lImgAmt = 0;
                            List<String> listAttachId = new ArrayList<>();
                            String[] imageLists = prod.getString("ImageList").split(",");
                            for (String image : imageLists) {
                                if (StrUtil.startWith(image, "//")) {
                                    image = image.replace("//", "https://");
                                }

                                Integer AttachId = saveToAttach(image);
                                listAttachId.add(AttachId + "");

                                lImgAmt++;
                                if (lImgAmt == 3) {//仅保留三张图片
                                    break;
                                }
                            }

                            material.setSThumb(listAttachId.get(0));
                            material.setSPic(String.join(",", listAttachId));
                            material.setSourceDataId(sSourceDataId);
                            material.setCollectObjectId("/longxiagame/" + sCode);
                            materialService.saveOrUpdate(material);
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        lPage++;
                    }
                }
            }
        }

        materialService.classify();
    }

    private JSONArray request(String sUrl, Map<String, Object> mapParam) {
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> entry : mapParam.entrySet()) {
            multiValueMap.add(entry.getKey(), entry.getValue());
        }

        Long lTimeStamp = System.currentTimeMillis();

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-service-timestamp", String.valueOf(lTimeStamp));
        headers.add("X-Service-App", "hezuoapp");
        headers.add("X-Service-token", SecureUtil.md5("hezuoappf5647c124ee570e1aa2fb2cdc64cddf5" + lTimeStamp).toUpperCase());

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multiValueMap, headers);

        try {
            ResponseEntity<JSONArray> response = restTemplate.exchange(sUrl, HttpMethod.POST, requestEntity, JSONArray.class);
            return response.getBody();
        } catch (RestClientException e) {
            throw new SystemException("请求龙虾网失败，原因为：" + e.getMessage());
        }
    }

    private Integer saveToAttach(String sUrl) {
        String sFileName = extractImageName(sUrl);
        SysAttach sysAttach = new SysAttach();
        sysAttach.setSName(sFileName);
        sysAttach.setSObjectName("business/material");
        sysAttach.setSFilePath(sUrl);
        sysAttach.setDNewTime(LocalDateTime.now());
        sysAttach.setNewUserId(1);
        sysAttach.setBImage(sysAttachService.isImage(sFileName) ? 1 : 0);
        sysAttachService.save(sysAttach);

        return sysAttach.getLID();
    }

    /**
     * 检查是否已下架，如果下架了，就删除
     */
    public synchronized void checkOnSale() {
        logger.info("检查龙虾网下架商品");

        ArrayList<Integer> objects = new ArrayList<>();

        QueryWrapper<Material> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.likeRight("CollectObjectId", "/longxiagame/");
        List<Material> materialList = materialService.list(objectQueryWrapper);
        for (Material material : materialList) {
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("Code", material.getSourceDataId().substring(material.getSourceDataId().lastIndexOf("/") + 1));
            JSONArray arrResult = request("https://web.api.yx915.com/kcData/Data/Page", mapParam);
            String data = arrResult.getJSONObject(0).getString("data");
            if (data != null && data.equals("账号不存在")) {
                objects.add(material.getLId());
            }
        }

        if (objects.size() > 0) {
            materialService.removeByIds(objects);
            logger.info("共删除已下架的龙虾网商品：" + objects.size() + "件");
        }
    }

    public void fix() {
        QueryWrapper<Material> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.likeRight("CollectObjectId", "/longxiagame/");
        List<Material> materialList = materialService.list(objectQueryWrapper);
        for (Material material : materialList) {
            JSONObject jsonObject = JSONObject.parseObject(material.getSSourceContent());
            String[] split = material.getSourceDataId().split("/");
            material.setSContent(jsonObject.getString("Description") + "\n\nhttp://zh.lytiao.com/Game_Contentpage.html?code=" + split[3] + "," + split[2]);

            String[] pics = material.getSPic().split(",");
            if (pics.length > 3) {
                StringBuilder pic = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    pic.append(pics[i]);

                    if (i < 2) {
                        pic.append(",");
                    }
                }

                material.setSPic(pic.toString());
            }
        }

        materialService.updateBatchById(materialList);
    }
}
