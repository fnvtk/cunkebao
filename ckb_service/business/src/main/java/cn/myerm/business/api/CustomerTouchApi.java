package cn.myerm.business.api;

import cn.myerm.common.config.Businessconfig;
import cn.myerm.common.exception.SystemException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomerTouchApi {

    private static final Logger logger = LoggerFactory.getLogger(CustomerTouchApi.class);

    private final Businessconfig businessConfig;
    private final RestTemplate restTemplate;

    public CustomerTouchApi(Businessconfig businessConfig, RestTemplate restTemplate) {
        this.businessConfig = businessConfig;
        this.restTemplate = restTemplate;
    }

    /**
     * post提交
     *
     * @param mapParam
     */
    public JSONObject post(String sUrl, Map<String, Object> mapParam) throws SystemException {

        MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        for (Map.Entry<String, Object> entry : mapParam.entrySet()) {
            multiValueMap.add(entry.getKey(), entry.getValue());
        }

        HttpHeaders headers = new HttpHeaders();

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multiValueMap, headers);

        try {
            logger.info("请求触客系统，路径为：" + sUrl + "，参数为：" + multiValueMap);
            ResponseEntity<JSONObject> response = restTemplate.exchange(businessConfig.getCustomertouchapiurl() + sUrl, HttpMethod.POST, requestEntity, JSONObject.class);

            if (response.getBody().getIntValue("code") != 10000) {
                throw new SystemException(response.getBody().getLongValue("code"), response.getBody().getString("message"));
            }

            return response.getBody().getJSONObject("data");
        } catch (RestClientException e) {
            throw new SystemException("请求触客系统失败，原因为：" + e.getMessage());
        }
    }

    public JSONArray getWechatByKeyword(String sKeyword) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("pagesize", "40");
        mapParam.put("isalive", "");
        mapParam.put("keyword", sKeyword);

        try {
            JSONObject result = post("/v1/customertouch/wechat/account/list", mapParam);
            return result.getJSONArray("results");
        } catch (SystemException e) {
            throw new SystemException(e.getMessage());
        }
    }

    /**
     * 获取微信号列表
     *
     * @return
     */
    public JSONArray getWechatAccountList() {
        JSONArray arrAccount = new JSONArray();

        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("pagesize", "40");
        mapParam.put("isalive", "");
        mapParam.put("keyword", "");

        int page = 0;
        while (true) {
            mapParam.put("page", page + "");

            try {
                JSONObject result = post("/v1/customertouch/wechat/account/list", mapParam);
                if (result.getJSONArray("results").size() > 0) {
                    arrAccount.addAll(result.getJSONArray("results"));
                } else {
                    break;
                }
            } catch (SystemException e) {
                logger.error(e.getMessage());
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            page++;
        }

        return arrAccount;
    }

    /**
     * 手机号加好友列表
     *
     * @param mapParam
     * @return
     */
    public JSONArray getAddFriendByPhoneList(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/friend/add/list", mapParam);
        return result.getJSONArray("results");
    }

    /**
     * 获取好友列表
     *
     * @param mapParam
     * @return
     */
    public JSONArray getWechatFriendList(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/friend/list", mapParam);
        return result.getJSONArray("results");
    }

    public int getWechatFriendCount(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/friend/count", mapParam);
        return result.getIntValue("total");
    }

    /**
     * 获取微信群列表
     *
     * @param mapParam
     * @return JSONArray
     */
    public JSONArray getWechatroomList(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/chatroom/list", mapParam);
        return result.getJSONArray("results");
    }

    /**
     * 推送采集对象
     */
    public void pushCollectObject(JSONArray jsonArray) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("objects", jsonArray);

        post("/v1/customertouch/wechat/collectobject/push", mapParam);
    }

    /**
     * 拉取朋友圈的列表
     *
     * @param mapParam
     * @return
     */
    public JSONArray pullMoment(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/moment/list", mapParam);
        return result.getJSONArray("results");
    }

    /**
     * 拉取聊天群消息
     *
     * @param mapParam
     * @return
     */
    public JSONArray pullChatroomMessage(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/chatroommessage/search", mapParam);
        return result.getJSONArray("results");
    }

    /**
     * 发送下载视频的命令
     */
    public void downloadVideo(Map<String, Object> mapParam) {
        post("/v1/customertouch/wechat/message/downloadvideo", mapParam);
    }

    /**
     * 发朋友圈
     *
     * @param mapParam
     */
    public void publishMoment(Map<String, Object> mapParam) {
        post("/v1/customertouch/wechat/moment/publish", mapParam);
    }

    /**
     * 发朋友圈的列表
     *
     * @param mapParam
     */
    public JSONArray publishMomentList(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/moment/publishlist", mapParam);
        return result.getJSONArray("results");
    }

    /**
     * 朋友圈的信息
     *
     * @param mapParam
     * @return
     */
    public JSONArray getMomentDetail(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/moment/getdetail", mapParam);
        return result.getJSONArray("results");
    }

    /**
     * 申请添加好友
     *
     * @param mapParam
     */
    public void addFriend(Map<String, Object> mapParam) {
        JSONObject result = post("/v1/customertouch/wechat/friend/add", mapParam);
    }

    /**
     * 获取抖音账号列表
     */
    public JSONArray getWebMerchantList() {
        Map<String, Object> mapParam = new HashMap<>();
        JSONObject result = post("/v1/customertouch/douyin/getWebMerchantList", mapParam);
        return result.getJSONArray("results");
    }

    /**
     * 获取授权抖音的URL
     *
     * @return
     */
    public String getBindDyUrl() {
        Map<String, Object> mapParam = new HashMap<>();
        JSONObject result = post("/v1/customertouch/douyin/getWxBindDyUrl", mapParam);
        return result.getString("url");
    }

    /**
     * 分配客服账号
     *
     * @param wechatFriendIds
     */
    public void multiAllocFriendToAccount(String wechatFriendIds) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("wechatFriendIds", wechatFriendIds);
        post("/v1/customertouch/wechat/friend/multiAllocFriendToAccount", mapParam);
    }

    public JSONArray getChatroomMemberList(Integer wechatChatroomId) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("wechatChatroomId", wechatChatroomId);
        JSONObject result = post("/v1/customertouch/wechat/chatroom/listChatroomMember", mapParam);

        return result.getJSONArray("results");
    }

    /**
     * 是否允许发起好友申请
     * @param wechatId
     * @return
     */
    public boolean canReq(Integer wechatId) {
        try {
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("wechatId", wechatId);
            post("/v1/customertouch/wechat/account/canreq", mapParam);
            return true;
        } catch (SystemException e) {
            return false;
        }
    }
}
