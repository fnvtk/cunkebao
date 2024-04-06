package cn.myerm.customertouch.wechat.handler;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
public class Ws {

    private static final Logger LOGGER = LoggerFactory.getLogger(Ws.class);

    /**
     * ws地址
     */
    private final String wsUri = "wss://kf.quwanzhi.com:9993";

    /**
     * Redis操作类
     */
    private final RedisTemplate<String, String> redisTemplate;

    private final WsMessageHandle wsMessageHandle;

    /**
     * 0：链接断开或者异常；1：代表链接中；2：代表正在连接；
     */
    private int isConnect = 0;

    /**
     * 表示已授权成功，否则会被立即剔除
     */
    private int isReady = 0;

    /**
     * ws客户端操作类
     */
    private WebSocketClient wsClient;

    @Value("${spring.redis.queuekey}")
    private String queueKey;

    @Autowired
    public Ws(RedisTemplate<String, String> redisTemplate, WsMessageHandle wsMessageHandle) {
        this.redisTemplate = redisTemplate;
        this.wsMessageHandle = wsMessageHandle;
    }

    /**
     * 获取客户端连接实例
     *
     * @param wsUri
     * @param httpHeaders
     * @return WebSocketClient
     */
    private WebSocketClient createWebSocketClient(String wsUri, Map<String, String> httpHeaders) {

        try {
            //创建客户端连接对象
            WebSocketClient client = new WebSocketClient(new URI(wsUri), httpHeaders) {

                /**
                 * 建立连接调用
                 * @param serverHandshake
                 */
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    LOGGER.info("onOpen -> 客户端建立连接");
                    isConnect = 1;

                    /**
                     * 建立连接后，立即要发送验证信息过去，否则立即被踢
                     */
                    final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();

                    String kfAccessToken = valueOps.get("kfAccessToken");
                    String kfAccountId = valueOps.get("kfAccountId");

                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("accessToken", kfAccessToken);
                    resultMap.put("accountId", kfAccountId);
                    resultMap.put("client", "kefu-client");
                    resultMap.put("cmdType", "CmdSignIn");
                    resultMap.put("seq", System.currentTimeMillis());

                    LOGGER.info("onOpen -> 发送验证信息:" + resultMap.toString());
                    wsClient.send(JSON.toJSONString(resultMap));

                    isReady = 1;
                }

                /**
                 * 收到服务端消息调用
                 * @param s
                 */
                @Override
                public void onMessage(String s) {
                    LOGGER.info("onMessage -> 收到服务端消息：{}", s);
                    final JSONObject jsonObject = JSONObject.parseObject(s);
                    if (jsonObject.getString("notify") != null && (jsonObject.getString("notify").equals("Auth failed") || jsonObject.getString("notify").equals("Kicked out"))) {
                        isReady = 0;
                        wsClient = null;

                        final ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
                        valueOps.set("kfAccessToken", "");
                        valueOps.set("kfAccountId", "");
                        valueOps.set("kfLock", "");
                    } else {
                        wsMessageHandle.exec(jsonObject);
                    }
                }

                /**
                 * 断开连接调用
                 * @param i
                 * @param s
                 * @param b
                 */
                @Override
                public void onClose(int i, String s, boolean b) {
                    LOGGER.info("onClose -> 客户端关闭连接，i：{}，b：{}，s：{}", i, b, s);
                    isConnect = 0;
                    isReady = 0;
                    wsClient = null;

                    //提交到预警中心
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("appname", "customertouch");
                    jsonObject.put("end", "service");
                    jsonObject.put("host", "");
                    jsonObject.put("position", "wechat\\service\\kf\\onError");
                    jsonObject.put("msg", "客户端关闭连接：" + i + "," + s);
                    jsonObject.put("level", 4);
                    jsonObject.put("detail", "");
                    jsonObject.put("type", "info");
                    jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
                    jsonObject.put("env", "{}");
                    redisTemplate.opsForList().rightPush(queueKey, jsonObject.toJSONString());
                }

                /**
                 * 连接报错调用
                 * @param e
                 */
                @Override
                public void onError(Exception e) {
                    LOGGER.error("onError -> 客户端连接异常，异常信息：{}", e.getMessage());
                    if (null != wsClient) {
                        wsClient.close();
                    }
                    isConnect = 0;
                    isReady = 0;
                    wsClient = null;

                    //提交到预警中心
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("appname", "customertouch");
                    jsonObject.put("end", "service");
                    jsonObject.put("host", "");
                    jsonObject.put("position", "wechat\\service\\kf\\onError");
                    jsonObject.put("msg", "客户端连接异常，异常信息：" + e.getMessage());
                    jsonObject.put("level", 4);
                    jsonObject.put("detail", "");
                    jsonObject.put("type", "error");
                    jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
                    jsonObject.put("env", "{}");
                    redisTemplate.opsForList().rightPush(queueKey, jsonObject.toJSONString());
                }
            };

            return client;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }


    /**
     * 连接websocket服务端
     * 注意 synchronized 关键字，保证多个请求同时连接时，
     * 只有一个连接在创建
     *
     * @param uri
     * @param httpHeaders
     */
    private void connect(String uri, Map<String, String> httpHeaders) {

        if (null != wsClient) {
            LOGGER.info("已存在连接，oldWsClient：{}-{}",
                    wsClient.getReadyState(), wsClient.getReadyState().ordinal());
            if (1 == wsClient.getReadyState().ordinal()) {
                LOGGER.info("使用存在且已打开的连接");
            } else {
                LOGGER.info("注销存在且未打开的连接，并重新获取新的连接");
                wsClient.close();
            }
        }

        WebSocketClient newWsClient = createWebSocketClient(uri, httpHeaders);
        newWsClient.connect();
        newWsClient.setConnectionLostTimeout(0);

        // 设置连接状态为正在连接
        isConnect = 2;

        // 连接状态不再是0请求中，判断建立结果是不是1已建立
        long startTime = System.currentTimeMillis();
        while (1 != newWsClient.getReadyState().ordinal()) {
            // 避免网络波动，设置持续等待连接时间
            long endTime = System.currentTimeMillis();
            long waitTime = (endTime - startTime) / 1000;
            if (5L < waitTime) {
                LOGGER.info("建立连接异常，请稍后再试");
                break;
            }
        }

        if (1 == newWsClient.getReadyState().ordinal()) {
            this.wsClient = newWsClient;
            LOGGER.info("客户端连接成功！");
        }
    }

    private synchronized Boolean connect() {
        if (wsClient == null || isReady != 1) {
            connect(wsUri, null);
            for (int i = 0; i < 5; i++) {//循环5s，目的是足够的时间准备就绪
                if (isReady == 1) {
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return wsClient != null;
        }

        return true;
    }

    /**
     * 向wss发送消息
     *
     * @param text
     */
    public void send(String text) {
        LOGGER.info("WS发送参数：" + text);

        if (connect()) {
            wsClient.send(text);
        }
    }

    public void sendPing() {
        if (connect()) {
            wsClient.sendPing();
        }
    }
}
