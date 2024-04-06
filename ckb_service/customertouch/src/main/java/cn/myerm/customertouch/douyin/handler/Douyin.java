package cn.myerm.customertouch.douyin.handler;

import cn.myerm.common.exception.SystemException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class Douyin {

    private static final Logger LOGGER = LoggerFactory.getLogger(Douyin.class);

    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${cjgj.wxOpenId}")
    private String sWxOpenId;

    public Douyin(RestTemplate restTemplate, RedisTemplate<String, String> redisTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取抖音授权的URL
     *
     * @return
     */
    public JSONObject getWxBindDyUrl() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("wxOpenId", sWxOpenId);
        JSONObject jsonObject = httpGet("merchant/wxOpenId/getWxBindDyUrl", mapParam);

        LOGGER.info("获取抖音授权的URL：" + jsonObject.toJSONString());

        return jsonObject.getJSONObject("data");
    }

    /**
     * 获取超级管家的商户列表
     *
     * @return
     */
    public JSONArray getWebMerchantList() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("openId", sWxOpenId);
        JSONObject jsonObject = httpGet("merchant/wxOpenId/getWebMerchantList", mapParam);

        LOGGER.info("获取超级管家的商户列表：" + jsonObject.toJSONString());

        return jsonObject.getJSONArray("data");
    }

    /**
     * 登录抖音账号
     *
     * @return
     */
    public String login(String merchantId) {
        JSONObject mapParam = new JSONObject();
        mapParam.put("merchantId", merchantId);
        JSONObject jsonObject = httpPost("merchant/wxOpenId/login_wx", mapParam);

        LOGGER.info("登录超级管家的某个抖音商户：" + jsonObject.toJSONString());

        return jsonObject.getJSONObject("data").getString("token");
    }

    private JSONObject httpGet(String sUrl, Map<String, Object> mapParam) {
        sUrl = "https://cjgj.lingyundata.com/api/" + sUrl;

        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        final String kfAccessToken = valueOps.get("kfAccessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.add("client", "system");
        headers.add("authorization", "bearer " + kfAccessToken);
        headers.add("Content-Type", "application/json");

        StringBuilder sParam = new StringBuilder();
        for (Map.Entry<String, Object> entry : mapParam.entrySet()) {
            sParam.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        try {
            LOGGER.info("请求客服系统参数：" + mapParam);
            ResponseEntity<JSONObject> response = restTemplate.exchange(sUrl + "?" + sParam, HttpMethod.GET, requestEntity, JSONObject.class);
            //LOGGER.info("请求客服系统返回：" + response.getBody());
            if (response.getBody().getIntValue("code") != 200) {
                throw new SystemException(response.getBody().getString("msg"));
            }

            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("请求客服系统出错：" + e.getMessage());
            throw new SystemException(e.getMessage());
        }
    }

    private JSONObject httpPost(String sUrl, JSONObject mapParam) {
        sUrl = "https://cjgj.lingyundata.com/api/" + sUrl;

        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        final String kfAccessToken = valueOps.get("kfAccessToken");

        HttpHeaders headers = new HttpHeaders();

        StringBuilder sParam = new StringBuilder();
        for (Map.Entry<String, Object> entry : mapParam.entrySet()) {
            sParam.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        try {
            HttpEntity<JSONObject> requestEntity = new HttpEntity<>(mapParam, headers);
            LOGGER.info(mapParam.toJSONString());
            LOGGER.info(headers.toString());
            ResponseEntity<JSONObject> response = restTemplate.exchange(sUrl + "?" + sParam, HttpMethod.POST, requestEntity, JSONObject.class);
            if (response.getBody().getIntValue("code") != 200) {
                throw new SystemException(response.getBody().getString("msg"));
            }

            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("请求请求客服系统出错：" + e.getMessage());

            throw new SystemException(e.getMessage());
        }
    }
}
