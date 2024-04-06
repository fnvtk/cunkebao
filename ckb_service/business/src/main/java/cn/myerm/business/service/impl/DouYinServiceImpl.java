package cn.myerm.business.service.impl;

import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.entity.DouYin;
import cn.myerm.business.mapper.DouYinMapper;
import cn.myerm.business.service.IDouYinService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DouYinServiceImpl extends BusinessServiceImpl<DouYinMapper, DouYin> implements IDouYinService {

    private static final Logger logger = LoggerFactory.getLogger(DouYinServiceImpl.class);

    @Resource
    private CustomerTouchApi customerTouchApi;

    @Resource
    private RestTemplate restTemplate;

    public List<DouYin> getDouYinList() {
        QueryWrapper<DouYin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("AccountId", getCurrUser().getAccountId());

        return list(queryWrapper);
    }

    /**
     * 同步抖音账号到存客宝
     */
    public void syncMerchantList() {
        logger.info("同步抖音账号");
        JSONArray webMerchantList = customerTouchApi.getWebMerchantList();
        for (int i = 0; i < webMerchantList.size(); i++) {
            JSONObject merchant = webMerchantList.getJSONObject(i);

            DouYin douYin = getById(merchant.getBigInteger("id"));
            if (douYin == null) {
                douYin = new DouYin();
                douYin.setLId(merchant.getBigInteger("id"));
            }

            douYin.setSName(merchant.getString("name"));
            douYin.setSAvatar(merchant.getString("avatar"));
            douYin.setSRaw(merchant.toJSONString());
            saveOrUpdate(douYin);
        }
    }

    /**
     * 绑定抖音账号
     */
    public void bindDy() {
        JSONArray webMerchantList = customerTouchApi.getWebMerchantList();
        for (int i = 0; i < webMerchantList.size(); i++) {
            JSONObject merchant = webMerchantList.getJSONObject(i);
        }
    }

    /**
     * 获取绑定抖音号的二维码
     */
    public String getBindQrcode() {
        String sJumpUrl = customerTouchApi.getBindDyUrl();

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(sJumpUrl).openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(5000);

        try {
            URI uri = new URI(conn.getHeaderField("Location"));
            //解析查询参数
            Map<String, String> params = new HashMap<>();
            if (uri.getQuery() != null) {
                String[] pairs = uri.getQuery().split("&");
                for (String pair : pairs) {
                    int idx = pair.indexOf("=");
                    String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
                    String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : "";
                    params.put(key, value);
                }
            }

            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("client_key", params.get("client_key"));
            mapParam.put("scope", params.get("scope"));
            mapParam.put("next", params.get("redirect_uri"));
            mapParam.put("state", params.get("state"));
            mapParam.put("jump_type", "native");
            mapParam.put("optional_scope_check", "");
            mapParam.put("optional_scope_uncheck", "");
           // mapParam.put("customize_params", "{\"comment_id\":\"\",\"source\":\"pc_auth\",\"not_skip_confirm\":\"true\"}");


            StringBuilder sParam = new StringBuilder();
            for (Map.Entry<String, Object> entry : mapParam.entrySet()) {
                sParam.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }

            logger.info(sParam.toString());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(new HttpHeaders());

            ResponseEntity<JSONObject> response = restTemplate.exchange("https://open.douyin.com/oauth/get_qrcode/?" + sParam, HttpMethod.GET, requestEntity, JSONObject.class);

            return response.getBody().getJSONObject("data").getString("qrcode");
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
