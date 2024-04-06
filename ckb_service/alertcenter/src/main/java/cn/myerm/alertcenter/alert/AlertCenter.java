package cn.myerm.alertcenter.alert;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.myerm.alertcenter.entity.ErrorTask;
import cn.myerm.alertcenter.service.impl.CacheServiceImpl;
import cn.myerm.alertcenter.service.impl.ErrorTaskServiceImpl;
import cn.myerm.alertcenter.api.CustomerTouchApi;
import cn.myerm.common.config.ElasticsearchConfig;
import cn.myerm.common.exception.SystemException;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldSort;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AlertCenter {

    private static final Logger logger = LoggerFactory.getLogger(AlertCenter.class);

    private final ElasticsearchConfig elasticsearchConfig;

    private final ElasticsearchClient client;
    private final RestTemplate restTemplate;
    private final CacheServiceImpl cacheService;
    private final ErrorTaskServiceImpl errorTaskService;
    private final CustomerTouchApi customerTouchApi;

    @Value("${elasticsearch.index}")
    private String sIndex;

    @Autowired
    public AlertCenter(ElasticsearchConfig elasticsearchConfig, RestTemplate restTemplate, CacheServiceImpl cacheService, ErrorTaskServiceImpl errorTaskService, CustomerTouchApi customerTouchApi) throws IOException {
        this.elasticsearchConfig = elasticsearchConfig;
        client = elasticsearchConfig.configClint();
        this.restTemplate = restTemplate;
        this.cacheService = cacheService;
        this.errorTaskService = errorTaskService;
        this.customerTouchApi = customerTouchApi;
    }


    /**
     * 处理消息队列传过来的信息，保存到ES
     *
     * @param sData 队列数据
     */
    public boolean handleMsg(String sData) throws IOException {

        logger.info("处理预警中心的日志数据:" + sData);

        //先创建索引
        createEsIndex();

        final JSONObject jsonObject = JSONObject.parseObject(sData);

        //格式化happentime
        final DateTime happenTime = DateUtil.parseDateTime(jsonObject.getString("happentime"));
        jsonObject.put("happentime", DateUtil.format(happenTime, "yyyy-MM-dd HH:mm:ss").replace(" ", "T"));

        //加工数据
        jsonObject.put("raw", sData);
        jsonObject.put("savetime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss").replace(" ", "T"));


        JSONObject jsonObjectClone = JSONObject.parseObject(sData);
        jsonObjectClone.remove("happentime");//去掉happentime，否则同一个事件的md5值不同
        jsonObject.put("md5", SecureUtil.md5(jsonObjectClone.toJSONString()));


        //转化成Java普通类
        final Message message = jsonObject.toJavaObject(Message.class);

        //新建文档
        try {
            client.create(
                    req -> req.index(sIndex).id(IdUtil.simpleUUID()).document(message)
            );
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 判断索引是否存在 可以用来初始化搜索的时候用 如果没有索引那就创建索引
     */
    public boolean createEsIndex() throws IOException {
        ElasticsearchIndicesClient indices = client.indices();

        try {
            boolean bExists = indices.exists(req -> req.index(sIndex)).value();
            if (!bExists) {//索引不存在，创建索引
                indices.create(req -> req.index(sIndex));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * 心跳检测公司的几个系统
     *
     * @throws IOException
     */
    public void heartBeat() throws IOException {

        List<String> listUrl = new ArrayList<>();

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, new HttpHeaders());

        for (String url : listUrl) {
            for (int i = 0; i < 5; i++) {//重试5次
                URL parseUrl = new URL(url);
                try {
                    restTemplate.exchange(url, HttpMethod.POST, requestEntity, Void.class);
                    break;
                } catch (Exception ex) {
                    if (i == 4) {//重试了5次都报错
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("appname", "myerm");
                        jsonObject.put("end", "service");
                        jsonObject.put("host", parseUrl.getHost());
                        jsonObject.put("position", "business/alertcenter/heartbeat");
                        jsonObject.put("msg", "[" + url + "]没有正确响应");
                        jsonObject.put("detail", ex.getMessage());
                        jsonObject.put("level", 1);
                        jsonObject.put("type", "error");
                        jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
                        handleMsg(jsonObject.toJSONString());

                        logger.error(parseUrl.getHost() + "没有正确响应");
                        break;
                    }
                }

                //暂停1秒
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 把故障信息导入到故障派工单
     *
     * @throws IOException
     */
    @Transactional
    public void saveToErrorTask() throws IOException {

        //查询字段：保存时间
        Query byTime = RangeQuery.of(r -> r
                .field("savetime")
                .gt(JsonData.of(cacheService.getOrSet("lastessearchtime", "2023-06-29T00:00:00")))
        )._toQuery();

        //查询字段：故障等级
        Query byLevel = RangeQuery.of(r -> r
                .field("level")
                .gt(JsonData.of(0))
        )._toQuery();


        // 排序字段规则
        FieldSort fs = new FieldSort.Builder()
                .field("savetime")
                .order(SortOrder.Asc)
                .build();

        // 排序操作项
        SortOptions so = new SortOptions.Builder()
                .field(fs)
                .build();

        List<Hit<Message>> hits = null;
        try {
            SearchResponse<Message> response = client.search(s -> s
                            .index(sIndex)
                            .sort(so).size(1000)
                            .query(q -> q.bool(b -> b.must(byLevel).must(byTime))),
                    Message.class
            );
            hits = response.hits().hits();
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        String sLastTime = null;
        for (int i = 0; i < hits.size(); i++) {
            Message message = hits.get(i).source();
            sLastTime = message.getSavetime();

            final QueryWrapper<ErrorTask> errorTaskQueryWrapper = new QueryWrapper<>();
            errorTaskQueryWrapper.eq("sErrorCode", message.getMd5());
            errorTaskQueryWrapper.ne("StatusId", "done");
            if (errorTaskService.count(errorTaskQueryWrapper) > 0) {//存在待处理或处理中的故障，不重复新增
                continue;
            }

            try {
                final ErrorTask errorTask = new ErrorTask();
                errorTask.setSName(message.getMsg());
                errorTask.setSDetail(message.getDetail());
                errorTask.setSErrorCode(message.getMd5());
                errorTask.setDNewTime(LocalDateTime.parse(message.getHappentime()));
                errorTask.setLevelId(message.getLevel());
                errorTask.setStatusId("pending");
                errorTask.setSRawMsg(message.getRaw());
                errorTaskService.save(errorTask);
            } catch (Exception e) {
                //记录报错信息
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("appname", "myerm");
                jsonObject.put("end", "service");
                jsonObject.put("host", "master");
                jsonObject.put("position", "business/alertcenter/saveToErrorTask");
                jsonObject.put("msg", "故障派工单创建失败");
                jsonObject.put("detail", e.getMessage());
                jsonObject.put("level", 3);
                jsonObject.put("type", "error");
                jsonObject.put("happentime", DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
                jsonObject.put("env", "{}");
                handleMsg(jsonObject.toJSONString());
                continue;
            }

            logger.warn("创建故障派工单：" + message);
        }

        if (sLastTime != null) {
            cacheService.set("lastessearchtime", sLastTime);
        }
    }

    /**
     * 1,2级故障通知
     */
    public void alertHighLevel() {
        QueryWrapper<ErrorTask> errorTaskQueryWrapper = new QueryWrapper<>();
        errorTaskQueryWrapper.ne("StatusId", "done");
        errorTaskQueryWrapper.in("LevelId", 1, 2);
        errorTaskQueryWrapper.isNull("dLastNotifyTime");
        for (ErrorTask errorTask : errorTaskService.list(errorTaskQueryWrapper)) {
            try {
                Thread.sleep(5000);

                sendMsg("预警中心：系统新增一条" + errorTask.getLevelId() + "级故障：" + errorTask.getSName() + "。");
                errorTask.setDLastNotifyTime(LocalDateTime.now());
                errorTaskService.saveOrUpdate(errorTask);
            } catch (SystemException e) {
                logger.error("消息发送失败，原因为：" + e.getMessage());
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * 通知到群里
     * 每两个小时执行一次任务，22点-8点不发送
     */
    public void pushToChatroom() {
        int lHour = DateUtil.thisHour(true);
        if (lHour < 22 && lHour > 8) {//22点-8点不发送
            QueryWrapper<ErrorTask> errorTaskQueryWrapper = new QueryWrapper<>();
            errorTaskQueryWrapper.ne("StatusId", "done");
            int lCountUnDone = errorTaskService.count(errorTaskQueryWrapper);
            if (lCountUnDone > 0) {
                StringBuilder sMsg = new StringBuilder();
                sMsg.append("预警中心：系统仍有").append(lCountUnDone).append("条未完成的故障，");

                errorTaskQueryWrapper = new QueryWrapper<>();
                errorTaskQueryWrapper.select("COUNT(*) AS lCount", "LevelId");
                errorTaskQueryWrapper.ne("StatusId", "done");
                errorTaskQueryWrapper.orderByAsc("LevelId");
                final List<Map<String, Object>> listMaps = errorTaskService.listMaps(errorTaskQueryWrapper);

                if (listMaps.size() > 0) {
                    sMsg.append("其中");

                    for (Map<String, Object> listMap : listMaps) {
                        sMsg.append(listMap.get("LevelId")).append("级故障").append(listMap.get("lCount")).append("条，");
                    }

                    sMsg.deleteCharAt(sMsg.length() - 1);//删除最后一个逗号
                    sMsg.append("。");

                    try {
                        sendMsg(sMsg.toString());
                    } catch (SystemException e) {
                        logger.error("消息发送失败，原因为：" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 发送消息到指定的微信群，进行预警通知
     *
     * @param sMsg 消息内容
     */
    public void sendMsg(String sMsg) throws SystemException {
        Map<String, Object> params = new HashMap<>();
        params.put("content", sMsg);
        params.put("accountid", "300745");
        params.put("receiver", "705551");
        params.put("msgtype", "1");

        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", params);
    }
}
