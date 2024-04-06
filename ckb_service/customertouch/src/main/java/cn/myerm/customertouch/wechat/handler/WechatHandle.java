package cn.myerm.customertouch.wechat.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.myerm.common.exception.SystemException;
import cn.myerm.customertouch.wechat.entity.WeChat;
import cn.myerm.customertouch.wechat.entity.WeChatMoment;
import cn.myerm.customertouch.wechat.entity.WeChatMomentCollectCron;
import cn.myerm.customertouch.wechat.param.DownloadVideoParam;
import cn.myerm.customertouch.wechat.param.MomentPublishListParam;
import cn.myerm.customertouch.wechat.param.MomentPublishParam;
import cn.myerm.customertouch.wechat.param.WeChatUsageLogParam;
import cn.myerm.customertouch.wechat.service.impl.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class WechatHandle {
    private static final Logger LOGGER = LoggerFactory.getLogger(WechatHandle.class);
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final Ws ws;
    private final WeChatMomentCollectCronServiceImpl weChatMomentCollectCronService;
    private final WeChatMomentServiceImpl weChatMomentService;
    private final WeChatRoomCollectCronServiceImpl weChatRoomCollectCronService;
    private final WeChatUsageLogServiceImpl weChatUsageLogService;
    private final WeChatServiceImpl weChatService;
    private final JdbcTemplate jdbcTemplate;
    private final Object lock = new Object();

    @Value("${ckb.kfusername}")
    private String sKfUserName;

    @Value("${ckb.kfpasswd}")
    private String sKfPasswd;

    @Value("${spring.redis.queuekey}")
    private String queueKey;

    /**
     * 是否开启防踢
     */
    @Value("${ckb.keepalive}")
    private Boolean bKeepAlive;

    @Autowired
    public WechatHandle(RestTemplate restTemplate, RedisTemplate<String, String> redisTemplate, Ws ws, WeChatMomentCollectCronServiceImpl weChatMomentCollectCronService, WeChatMomentServiceImpl weChatMomentService, WeChatRoomCollectCronServiceImpl weChatRoomCollectCronService, WeChatUsageLogServiceImpl weChatUsageLogService, WeChatServiceImpl weChatService, JdbcTemplate jdbcTemplate) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.ws = ws;
        this.weChatMomentCollectCronService = weChatMomentCollectCronService;
        this.weChatMomentService = weChatMomentService;
        this.weChatRoomCollectCronService = weChatRoomCollectCronService;
        this.weChatUsageLogService = weChatUsageLogService;
        this.weChatService = weChatService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 向wss发送消息
     *
     * @param mapParam
     */
    private void send(Map<String, String> mapParam) {
        if (login()) {//首先要登录客服系统
            ws.send(JSON.toJSONString(mapParam));
        }
    }

    private void send(JSONObject mapParam) {
        if (login()) {//首先要登录客服系统
            ws.send(mapParam.toJSONString());
        }
    }

    /**
     * 发送信息到群里
     */
    public void sendMessageToChatroom(Map<String, String> mapParam) {
        if (StrUtil.isEmpty(mapParam.get("content"))) {
            throw new SystemException("发送内容不能为空");
        }

        if (StrUtil.isEmpty(mapParam.get("wechatAccountId"))) {
            throw new SystemException("wechatAccountId不能为空");
        }

        if (StrUtil.isEmpty(mapParam.get("wechatChatroomId"))) {
            throw new SystemException("wechatChatroomId不能为空");
        }

        if (StrUtil.isEmpty(mapParam.get("msgType"))) {
            throw new SystemException("msgType不能为空");
        }

        mapParam.put("cmdType", "CmdSendMessage");
        mapParam.put("msgSubType", "0");
        mapParam.put("seq", String.valueOf(System.currentTimeMillis()));
        mapParam.put("wechatFriendId", "0");

        send(mapParam);
    }

    /**
     * 发送信息给好友
     */
    public void sendMessageToFriend(Map<String, String> mapParam) {
        if (StrUtil.isEmpty(mapParam.get("content"))) {
            throw new SystemException("发送内容不能为空");
        }

        if (StrUtil.isEmpty(mapParam.get("wechatAccountId"))) {
            throw new SystemException("wechatAccountId不能为空");
        }

        if (StrUtil.isEmpty(mapParam.get("wechatFriendId"))) {
            throw new SystemException("wechatFriendId不能为空");
        }

        if (StrUtil.isEmpty(mapParam.get("msgType"))) {
            throw new SystemException("msgType不能为空");
        }

        mapParam.put("cmdType", "CmdSendMessage");
        mapParam.put("msgSubType", "0");
        mapParam.put("seq", String.valueOf(System.currentTimeMillis()));
        mapParam.put("wechatChatroomId", "0");

        send(mapParam);
    }

    /**
     * 邀请入群
     *
     * @param mapParam
     */
    public void chatRoomInvite(JSONObject mapParam) {
        mapParam.put("cmdType", "CmdChatroomInvite");
        mapParam.put("seq", String.valueOf(System.currentTimeMillis()));

        send(mapParam);
    }

    /**
     * 添加好友
     */
    public void sendFriendRequest(Map<String, String> mapParam) {
        if (StrUtil.isEmpty(mapParam.get("Message"))) {
            throw new SystemException("Message不能为空");
        }

        if (StrUtil.isEmpty(mapParam.get("Phone")) && StrUtil.isEmpty(mapParam.get("TargetWechatId"))) {
            throw new SystemException("Phone和TargetWechatId不能同时为空");
        }

        if (!StrUtil.isEmpty(mapParam.get("Phone")) && !StrUtil.isEmpty(mapParam.get("TargetWechatId"))) {
            throw new SystemException("Phone和TargetWechatId不能同时传值");
        }

        if (StrUtil.isEmpty(mapParam.get("WechatAccountId"))) {
            throw new SystemException("WechatAccountId不能为空");
        }

        mapParam.put("cmdType", "CmdSendFriendRequest");
        mapParam.put("seq", String.valueOf(System.currentTimeMillis()));

        send(mapParam);
    }

    /**
     * 下载视频
     *
     * @param downloadVideoParam
     */
    public void downloadVideo(DownloadVideoParam downloadVideoParam) {
        Map<String, String> mapParam = new HashMap<>();

        if (downloadVideoParam.getType().equals("friend")) {
            mapParam.put("friendMessageId", downloadVideoParam.getMessageId());
            mapParam.put("chatroomMessageId", "0");
        } else {
            mapParam.put("chatroomMessageId", downloadVideoParam.getMessageId());
            mapParam.put("friendMessageId", "0");
        }

        mapParam.put("tencentUrl", downloadVideoParam.getTencentUrl());
        mapParam.put("wechatAccountId", downloadVideoParam.getWechatAccountId() + "");
        mapParam.put("seq", System.currentTimeMillis() + "");
        mapParam.put("cmdType", "CmdDownloadVideo");
        send(mapParam);
    }

    /**
     * 搜索好友聊天记录
     *
     * @param mapParam
     */
    public JSONArray searchFriendMessage(Map<String, Object> mapParam) {
        return httpGetArray("/api/FriendMessage/searchMessage", mapParam);
    }

    /**
     * 微信群聊天记录
     *
     * @param mapParam
     * @return JSONArray
     */
    public JSONArray searchChatroomMessage(Map<String, Object> mapParam) {
        return httpGetArray("/api/ChatroomMessage/searchMessage", mapParam);
    }

    /**
     * @param mapParam
     * @return
     */
    public JSONObject chatRoomList(Map<String, Object> mapParam) {
        mapParam.put("wechatAccountKeyword", "");
        mapParam.put("isDeleted", "");
        mapParam.put("groupId", "");
        mapParam.put("wechatChatroomId", 0);
        mapParam.put("memberKeyword", "");

        return httpGetObject("/api/WechatChatroom/pagelist", mapParam);
    }

    public JSONArray listChatroomMember(Integer wechatChatroomId) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("wechatChatroomId", wechatChatroomId);
        return httpGetArray("/api/WechatChatroom/listChatroomMember", mapParam);
    }

    /**
     * 微信好友列表
     *
     * @param mapParam
     * @return JSONObject
     */
    public JSONObject friendList(JSONObject mapParam) {

        mapParam.put("accountKeyword", "");
        mapParam.put("addFrom", "");
        mapParam.put("allotAccountId", "");
        mapParam.put("containSubDepartment", "");
        mapParam.put("departmentId", "");
        mapParam.put("extendFields", "");
        mapParam.put("friendPinYinKeyword", "");
        mapParam.put("friendRegionKeyword", "");
        mapParam.put("friendRemarkKeyword", "");
        mapParam.put("gender", "");
        mapParam.put("groupId", null);
        mapParam.put("isDeleted", null);
        mapParam.put("labels", "");

        JSONArray arrFriend = httpPostArray("/api/WechatFriend/friendlistData", mapParam);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", arrFriend);

        return jsonObject;
    }

    /**
     * 好友数量
     *
     * @param mapParam
     * @return JSONObject
     */
    public JSONObject friendCount(JSONObject mapParam) {
        mapParam.put("accountKeyword", "");
        mapParam.put("addFrom", "");
        mapParam.put("allotAccountId", "");
        mapParam.put("containSubDepartment", "");
        mapParam.put("departmentId", "");
        mapParam.put("extendFields", "");
        mapParam.put("friendKeyword", "");
        mapParam.put("friendPhoneKeyword", "");
        mapParam.put("friendPinYinKeyword", "");
        mapParam.put("friendRegionKeyword", "");
        mapParam.put("friendRemarkKeyword", "");
        mapParam.put("gender", "");
        mapParam.put("groupId", null);
        mapParam.put("isDeleted", null);
        mapParam.put("labels", "");
        mapParam.put("wechatAccountKeyword", "");

        String sCount = httpPostString("/api/WechatFriend/friendlistCount", mapParam);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("total", Integer.parseInt(sCount));

        return jsonObject;
    }

    /**
     * 好友申请
     *
     * @param mapParam
     */
    public void friendAdd(JSONObject mapParam) {
        WeChatUsageLogParam weChatUsageLogParam = new WeChatUsageLogParam();
        weChatUsageLogParam.setWeChatId(mapParam.getInteger("wechatAccountId"));
        weChatUsageLogParam.setTypeId(1);
        weChatUsageLogParam.setTargetId(mapParam.getString("phone"));
        weChatUsageLogParam.setDNewTime(LocalDateTime.now());

        weChatService.checkValid(weChatUsageLogParam);

        httpPostString("/api/AddFriendByPhoneTask/add", mapParam);

        weChatUsageLogService.newSave(weChatUsageLogParam);

        UpdateWrapper<WeChat> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.set("dLastFriendRequestTime", LocalDateTime.now());
        objectUpdateWrapper.eq("lId", mapParam.getInteger("wechatAccountId"));
        weChatService.update(objectUpdateWrapper);
    }

    /**
     * 手机号加好友列表
     *
     * @param mapParam
     * @return JSONArray
     */
    public JSONArray addFriendByPhoneTaskList(JSONObject mapParam) {
        JSONObject jsonObject = httpGetObject("/api/AddFriendByPhoneTask/list", mapParam);
        return jsonObject.getJSONArray("results");
    }


    /**
     * 微信列表
     *
     * @param mapParam
     * @return
     */
    public JSONObject accountList(Map<String, Object> mapParam) {

        //先取到微信列表的数据
        JSONObject accountObject = httpGetObject("/api/WechatAccount/list", mapParam);

        LinkedHashSet<String> wechatAccountIdSet = new LinkedHashSet<>();
        LinkedHashSet<String> deviceIdSet = new LinkedHashSet<>();
        LinkedHashSet<String> accountIdSet = new LinkedHashSet<>();

        JSONArray arrResult = accountObject.getJSONArray("results");
        for (int i = 0; i < arrResult.size(); i++) {
            JSONObject result = arrResult.getJSONObject(i);

            wechatAccountIdSet.add(result.getString("id"));
            deviceIdSet.add(result.getString("currentDeviceId"));
            accountIdSet.add(result.getString("deviceAccountId"));
        }

        //其次，取到微信号的一些重要参数
        mapParam = new HashMap<>();
        mapParam.put("wechatAccountIdsStr", JSON.toJSONString(wechatAccountIdSet));
        mapParam.put("deviceIdsStr", JSON.toJSONString(deviceIdSet));
        mapParam.put("accountIdsStr", JSON.toJSONString(accountIdSet));
        mapParam.put("groupId", null);
        JSONObject wechatPartial = httpGetObject("/api/WechatAccount/listTenantWechatPartial", mapParam);

        for (int i = 0; i < arrResult.size(); i++) {
            JSONObject result = arrResult.getJSONObject(i);

            result.put("deviceAlive", wechatPartial.getJSONObject("deviceAlive").getBoolean(result.getString("currentDeviceId")));
            result.put("femaleFriend", wechatPartial.getJSONObject("femaleFriend").getInteger(result.getString("id")));
            result.put("keFuAlive", wechatPartial.getJSONObject("keFuAlive").getBoolean(result.getString("deviceAccountId")));
            result.put("maleFriend", wechatPartial.getJSONObject("maleFriend").getInteger(result.getString("id")));
            result.put("sevenDayMsgCount", wechatPartial.getJSONObject("sevenDayMsgCount").getInteger(result.getString("id")));
            result.put("thirtyDayMsgCount", wechatPartial.getJSONObject("thirtyDayMsgCount").getInteger(result.getString("id")));
            result.put("totalFriend", wechatPartial.getJSONObject("totalFriend").getInteger(result.getString("id")));
            result.put("unknowFriend", wechatPartial.getJSONObject("unknowFriend").getInteger(result.getString("id")));
            result.put("wechatAlive", wechatPartial.getJSONObject("wechatAlive").getBoolean(result.getString("id")));
            result.put("yesterdayMsgCount", wechatPartial.getJSONObject("yesterdayMsgCount").getInteger(result.getString("id")));
        }

        accountObject.put("results", arrResult);

        return accountObject;
    }


    private JSONArray httpPostArray(String sUrl, JSONObject mapParam) {
        sUrl = "https://kf.quwanzhi.com:9991" + sUrl;

        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        final String kfAccessToken = valueOps.get("kfAccessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.add("client", "system");
        headers.add("authorization", "bearer " + kfAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<JSONObject> requestEntity = new HttpEntity<>(mapParam, headers);
            LOGGER.info(mapParam.toJSONString());
            LOGGER.info(headers.toString());
            ResponseEntity<JSONArray> response = restTemplate.exchange(sUrl, HttpMethod.POST, requestEntity, JSONArray.class);
            //LOGGER.debug("请求客服系统返回：" + response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("请求请求客服系统出错：" + e.getMessage());

            if (e.getMessage().contains("401 Unauthorized")) {
                clearAndLogin();
            }

            throw new SystemException(e.getMessage());
        }
    }

    private String httpPostString(String sUrl, JSONObject mapParam) {
        sUrl = "https://kf.quwanzhi.com:9991" + sUrl;

        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        final String kfAccessToken = valueOps.get("kfAccessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.add("client", "system");
        headers.add("authorization", "bearer " + kfAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            HttpEntity<JSONObject> requestEntity = new HttpEntity<>(mapParam, headers);
            LOGGER.info(mapParam.toJSONString());
            ResponseEntity<String> response = restTemplate.exchange(sUrl, HttpMethod.POST, requestEntity, String.class);
            //LOGGER.debug("请求客服系统返回：" + response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("请求请求客服系统出错：" + e.getMessage());

            if (e.getMessage().contains("401 Unauthorized")) {
                clearAndLogin();
            }

            throw new SystemException(e.getMessage());
        }
    }

    private String httpPut(String sUrl, JSONObject mapParam) {
        sUrl = "https://kf.quwanzhi.com:9991" + sUrl;

        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        final String kfAccessToken = valueOps.get("kfAccessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.add("client", "system");
        headers.add("authorization", "bearer " + kfAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        StringBuilder sParam = new StringBuilder();
        for (Map.Entry<String, Object> entry : mapParam.entrySet()) {
            if (entry.getValue() == null) {
                sParam.append(entry.getKey()).append("=").append("&");
            } else {
                sParam.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        try {
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(sUrl + "?" + sParam, HttpMethod.PUT, requestEntity, String.class);

            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("请求请求客服系统出错：" + e.getMessage());

            if (e.getMessage().contains("401 Unauthorized")) {
                clearAndLogin();
            }

            throw new SystemException(e.getMessage());
        }
    }


    private JSONArray httpGetArray(String sUrl, Map<String, Object> mapParam) {
        return httpGetArray(sUrl, mapParam, HttpMethod.GET);
    }

    /**
     * HTTP的get方式，获取JSON数组
     *
     * @param sUrl
     * @param mapParam
     * @return
     */
    private JSONArray httpGetArray(String sUrl, Map<String, Object> mapParam, HttpMethod method) {
        sUrl = "https://kf.quwanzhi.com:9991" + sUrl;

        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        final String kfAccessToken = valueOps.get("kfAccessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.add("client", "system");
        headers.add("authorization", "bearer " + kfAccessToken);
        headers.add("Content-Type", "application/json");

        StringBuilder sParam = new StringBuilder();
        for (Map.Entry<String, Object> entry : mapParam.entrySet()) {
            if (entry.getValue() == null) {
                sParam.append(entry.getKey()).append("=").append("&");
            } else {
                sParam.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(headers);
        try {
            LOGGER.info("请求客服系统的参数：" + sParam);
            ResponseEntity<JSONArray> response = restTemplate.exchange(sUrl + "?" + sParam, method, requestEntity, JSONArray.class);
            //LOGGER.info("登录客服系统返回：" + response.getBody());

            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("登录请求客服系统出错：" + e.getMessage());

            if (e.getMessage().contains("401 Unauthorized")) {
                clearAndLogin();
            }

            throw new SystemException(e.getMessage());
        }
    }

    private JSONObject httpGetObject(String sUrl, Map<String, Object> mapParam) {
        sUrl = "https://kf.quwanzhi.com:9991" + sUrl;

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

            return response.getBody();
        } catch (RestClientException e) {
            LOGGER.error("请求客服系统出错：" + e.getMessage());

            if (e.getMessage().contains("401 Unauthorized")) {
                clearAndLogin();
            }

            throw new SystemException(e.getMessage());
        }
    }


    /**
     * 登录到客服系统，这里有做了缓存处理
     */
    private Boolean login() throws SystemException {
        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

        if (valueOps.get("kfLock") != null && valueOps.get("kfLock").equals("1")) {//被锁定
            throw new SystemException("客服系统被锁定，请人工处理。");
        }

        String kfAccessToken = valueOps.get("kfAccessToken");
        String kfAccountId = valueOps.get("kfAccountId");

        if (kfAccessToken == null || kfAccessToken.equals("") || kfAccountId == null || kfAccountId.equals("")) {
            try {
                kfAccessToken = getToken();
                kfAccountId = getAccount(kfAccessToken);

                int lTimeout = 600;
                if (bKeepAlive) {//如果有防踢机制，可以延长到半小时
                    lTimeout = 1800;
                }

                //把token保存到redis，api调用的时候可以重复用
                valueOps.set("kfAccessToken", kfAccessToken, lTimeout, TimeUnit.SECONDS);
                valueOps.set("kfAccountId", kfAccountId, lTimeout, TimeUnit.SECONDS);

                return true;
            } catch (Exception e) {
                LOGGER.error(e.getMessage());
                return false;
            }
        }


        return true;
    }

    /**
     * 拿token
     *
     * @return String 返回token
     */
    private String getToken() {
        //获取客服系统的token
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("username", sKfUserName);
        params.add("password", sKfPasswd);
        LOGGER.info("登录客服系统请求参数：" + params);

        HttpHeaders headers = new HttpHeaders();
        headers.add("client", "kefu-client");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<JSONObject> response = restTemplate.exchange("https://kf.quwanzhi.com:9991/token", HttpMethod.POST, requestEntity, JSONObject.class);
            LOGGER.info("登录客服系统返回：" + response.getBody());
            return response.getBody().getString("access_token");
        } catch (SystemException e) {
            throw new SystemException(e.getMessage());
        } catch (HttpClientErrorException e) {
            String responseBody = e.getResponseBodyAsString();
            JSONObject response = JSONObject.parseObject(responseBody);

            if (response.getString("error") != null && response.getString("error").equals("invalid_grant")) {
                //提交到预警中心
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appname", "customertouch");
                jsonObject.put("end", "service");
                jsonObject.put("host", "service");
                jsonObject.put("position", "wechat\\service\\kf\\getToken");
                jsonObject.put("msg", "登录客服系统失败：" + response.getString("error_description") + "，系统被锁定，请人工解锁。");
                jsonObject.put("level", 2);
                jsonObject.put("detail", sKfUserName + ":" + sKfPasswd);
                jsonObject.put("type", "error");
                jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
                jsonObject.put("env", "{}");
                redisTemplate.opsForList().rightPush(queueKey, jsonObject.toJSONString());

                //锁定客服系统，30分钟
                final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
                valueOps.set("kfLock", "1", 1800, TimeUnit.SECONDS);

                throw new SystemException("登录客服系统失败：" + response.getString("error_description") + "，系统被锁定，请人工解锁。");
            }

            return null;
        }
    }

    /**
     * 获取公司账户信息
     *
     * @param sToken
     * @return
     */
    private String getAccount(String sToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("client", "system");
        headers.add("authorization", "bearer " + sToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(new LinkedMultiValueMap<>(), headers);
        try {
            ResponseEntity<JSONObject> response = restTemplate.exchange("https://kf.quwanzhi.com:9991/api/Account/self", HttpMethod.GET, requestEntity, JSONObject.class);
            LOGGER.info("登录客服系统返回：" + response.getBody());

            return response.getBody().getJSONObject("account").getString("id");
        } catch (Exception e) {
            throw new SystemException(e.getMessage());
        }
    }

    /**
     * 防踢
     */
    public void keepAlive() {
        if (bKeepAlive) {
            LOGGER.info("客服系统发送防踢指令");
            if (login()) {//首先要登录客服系统
                ws.sendPing();
            }
        }
    }

    /**
     * 采集朋友圈
     */
    public synchronized void momentCollect() {
        QueryWrapper<WeChatMomentCollectCron> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("dNextCollectTime", LocalDateTime.now());
        //queryWrapper.lt("lTryTime", 5);
        queryWrapper.eq("bEnable", 1);
        List<WeChatMomentCollectCron> listCron = weChatMomentCollectCronService.list(queryWrapper);

        for (WeChatMomentCollectCron collectCron : listCron) {
            JSONObject jsonParam = new JSONObject();

            jsonParam.put("wechatAccountId", collectCron.getWeChatId());
            jsonParam.put("wechatFriendId", collectCron.getWeChatFriendId());
            jsonParam.put("cmdType", "CmdFetchMoment");
            jsonParam.put("count", 10);
            jsonParam.put("isTimeline", false);
            jsonParam.put("prevSnsId", collectCron.getSPrevSnsId());
            jsonParam.put("createTimeSec", System.currentTimeMillis() / 1000L);
            jsonParam.put("seq", System.currentTimeMillis() / 1000L);

            send(jsonParam);

            collectCron.setDLastCollectTime(LocalDateTime.now());
            collectCron.setLTryTime(collectCron.getLTryTime() + 1);

            weChatMomentCollectCronService.calculateNextExecTime(collectCron);
            weChatMomentCollectCronService.saveOrUpdate(collectCron);

            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 采集朋友圈的资源
     */
    public synchronized void momentResourceCollect() {

        LOGGER.info("开始采集朋友圈的资源");

        //删除5分钟以前的，发送中的
        QueryWrapper<WeChatMoment> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lt("sendTime", LocalDateTime.now().minusMinutes(2));
        queryWrapper1.eq("status", 1);
        WeChatMoment updateWeChatMoment = new WeChatMoment();
        updateWeChatMoment.setSendTime(null);
        updateWeChatMoment.setStatus(0);
        weChatMomentService.update(updateWeChatMoment, queryWrapper1);

        ExecutorService threadPool = Executors.newFixedThreadPool(5);

        QueryWrapper<WeChatMoment> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("wechatAccountId");
        queryWrapper.eq("status", 0);
        queryWrapper.groupBy("wechatAccountId");
        List<WeChatMoment> listMomentGroup = weChatMomentService.list(queryWrapper);

        for (WeChatMoment weChatMomentGrp : listMomentGroup) {
            final int wechatAccountId = weChatMomentGrp.getWechatAccountId();
            threadPool.execute(() -> {
                QueryWrapper<WeChatMoment> qw = new QueryWrapper<>();
                qw.eq("wechatAccountId", wechatAccountId);
                qw.eq("status", 1);

                //队列中还有发送中，就不执行任务
                if (weChatMomentService.count(qw) > 0) {
                    return;
                }

                qw = new QueryWrapper<>();
                qw.eq("wechatAccountId", wechatAccountId);
                qw.eq("status", 0);
                qw.orderByDesc("createTime");
                qw.last("LIMIT 1");//一次执行1个
                List<WeChatMoment> listMoment = weChatMomentService.list(qw);

                for (WeChatMoment weChatMoment : listMoment) {
                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("cmdType", "CmdDownloadMomentImages");
                    jsonParam.put("wechatAccountId", weChatMoment.getWechatAccountId());
                    jsonParam.put("seq", System.currentTimeMillis() / 1000L);
                    jsonParam.put("snsId", weChatMoment.getSnsId());
                    jsonParam.put("urls", JSONArray.parseArray(weChatMoment.getUrlsorig()));
                    send(jsonParam);

                    weChatMoment.setStatus(1);//等待响应中
                    weChatMoment.setSendTime(LocalDateTime.now());//记录发送时间
                    weChatMoment.setTrytime(weChatMoment.getTrytime() + 1);
                    weChatMomentService.updateById(weChatMoment);
                }
            });
        }

        threadPool.shutdown();

        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
        }
    }

    /**
     * 发表朋友圈
     *
     * @param momentPublishParam
     */
    public void publishMoment(MomentPublishParam momentPublishParam) {
        LOGGER.info("发布朋友圈");

        JSONObject jsonParam = JSONObject.parseObject(JSON.toJSONString(momentPublishParam));
        jsonParam.put("altList", "");
        jsonParam.put("immediately", false);
        jsonParam.put("isUseLocation", false);
        jsonParam.put("lat", 0);
        jsonParam.put("lng", 0);
        jsonParam.put("poiAddress", "");
        jsonParam.put("poiName", "");
        jsonParam.put("publicMode", 0);
        jsonParam.put("timingTime", jsonParam.getString("beginTime"));
        jsonParam.put("picUrlList", JSONArray.parseArray(jsonParam.getString("picUrlList")));
        jsonParam.put("jobPublishWechatMomentsItems", JSONArray.parseArray(momentPublishParam.getJobPublishWechatMomentsItems()));

        if (StrUtil.isNotEmpty(momentPublishParam.getLink())) {
            JSONObject jsonObject = JSONObject.parseObject(momentPublishParam.getLink());
            jsonParam.put("link", jsonObject);
        } else {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("image", "");
            jsonParam.put("link", jsonObject);
        }

        httpPostString("/api/JobPublishWechatMoments/addJob", jsonParam);
    }

    /**
     * 获得发朋友圈的列表
     *
     * @param momentPublishListParam
     * @return
     */
    public JSONArray publishMomentList(MomentPublishListParam momentPublishListParam) {
        JSONObject jsonParam = JSONObject.parseObject(JSON.toJSONString(momentPublishListParam));

        jsonParam.put("keyword", "");
        jsonParam.put("jobStatus", "");
        jsonParam.put("contentType", "");
        jsonParam.put("only", false);
        JSONObject jsonObject = httpGetObject("/api/JobPublishWechatMoments/listPagination", jsonParam);
        return jsonObject.getJSONArray("results");
    }

    /**
     * 获得朋友圈的详情
     *
     * @param id
     * @return
     */
    public JSONArray getMomentDetail(Integer id) {
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("id", id);
        return httpGetArray("/api/JobPublishWechatMoments/getDetail", jsonParam);
    }

    /**
     * 如果系统被锁定了，可用这个来解锁。
     */
    public void unlock() {
        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set("kfLock", "");
    }

    /**
     * 分配客服
     *
     * @param wechatFriendIds
     */
    public void multiAllocFriendToAccount(String wechatFriendIds) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("wechatFriendIds", wechatFriendIds);
        jsonObject.put("notifyReceiver", false);
        jsonObject.put("toAccountId", "9977");

        httpPut("/api/WechatFriend/multiAllotFriendToAccount", jsonObject);
    }


    /**
     * 清空验证信息
     */
    public void clear() {
        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set("kfAccessToken", "");
        valueOps.set("kfAccountId", "");
        valueOps.set("kfLock", "");
    }

    /**
     * 清空验证并登录，用于请求时验证失败
     */
    private void clearAndLogin() {
        clear();
        login();
    }
}
