package cn.myerm.business.service.impl;

import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class JdUnionService {

    private static final Logger logger = LoggerFactory.getLogger(JdUnionService.class);

    private final String unionId = "1000048236";//京东联盟ID
    private final String jttAppId = "2302131608283426";//京推推应用appid
    private final String jttAppKey = "15d3cb2efa56d7c908b86c27a4b3bb3a";//京推推应用appkey
    private final String jttChainLink = "http://japi.jingtuitui.com/api/universal";//京推推智能转链接口地址

    private final RestTemplate restTemplate;

    @Autowired
    public JdUnionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * 转链
     *
     * @param sOldLink 原来的链接
     * @param sPosId   推广位Id
     */
    public JSONObject changeLink(String sOldLink, Long lPosId) {
        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("appid", jttAppId);
        multiValueMap.add("appkey", jttAppKey);
        multiValueMap.add("v", "v2");
        multiValueMap.add("unionid", unionId);
        multiValueMap.add("content", sOldLink);
        multiValueMap.add("positionid", lPosId);

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multiValueMap, headers);
        ResponseEntity<JSONObject> response = restTemplate.exchange(jttChainLink, HttpMethod.POST, requestEntity, JSONObject.class);


        return response.getBody();
    }
}
