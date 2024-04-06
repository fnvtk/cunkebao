package cn.myerm.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.entity.*;
import cn.myerm.business.mapper.MaterialPushLogMapper;
import cn.myerm.business.param.PushTaskParam;
import cn.myerm.business.service.IMaterialPushLogService;
import cn.myerm.business.service.IMaterialPushTaskService;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MaterialPushLogServiceImpl extends BusinessServiceImpl<MaterialPushLogMapper, MaterialPushLog> implements IMaterialPushLogService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialPushLogServiceImpl.class);

    private final MaterialServiceImpl materialService;
    private final SysAttachServiceImpl sysAttachService;
    private final CustomerTouchApi customerTouchApi;
    private final WeChatRoomServiceImpl weChatRoomService;
    private final MiniProgrameLibServiceImpl miniProgrameLibService;
    private final JdUnionService jdUnionService;

    @Autowired
    public MaterialPushLogServiceImpl(MaterialPushChatroomTaskServiceImpl materialPushChatroomTaskService, MaterialServiceImpl materialService, SysAttachServiceImpl sysAttachService, CustomerTouchApi customerTouchApi, WeChatRoomServiceImpl weChatRoomService, MiniProgrameLibServiceImpl miniProgrameLibService, JdUnionService jdUnionService) {
        this.materialService = materialService;
        this.sysAttachService = sysAttachService;
        this.customerTouchApi = customerTouchApi;
        this.weChatRoomService = weChatRoomService;
        this.miniProgrameLibService = miniProgrameLibService;
        this.jdUnionService = jdUnionService;
    }

    /**
     * 执行推送任务到待推送的队列
     */
    @Transactional
    public synchronized void execPushToLog(IMaterialPushTaskService materialPushTaskService, boolean bImmediately) {

        List<IMaterialPushTask> materialPushTasks = materialPushTaskService.getEnableList(bImmediately);

        for (IMaterialPushTask materialPushTask : materialPushTasks) {

            //第一，判断是否到了排程，如果是，再判断这个排程是否已推送过了
            boolean bCanPush = false;
            LocalTime tScheduleTime = LocalTime.now();
            List<Material> pushMaterialList = new ArrayList<>();//推送的内容
            if (materialPushTask.getBImmediately() == 0) {//定时发送
                String sPushSchedule = materialPushTask.getSPushSchedule();
                String[] arrPushSchedule = sPushSchedule.split(",");
                for (int i = arrPushSchedule.length - 1; i >= 0; i--) {//找到里当前时间最近的一个排程
                    tScheduleTime = LocalTime.parse(arrPushSchedule[i]);
                    if (LocalTime.now().compareTo(tScheduleTime) >= 0) {
                        //判断这个排程是否已经推送过
                        QueryWrapper<MaterialPushLog> pushLogQueryWrapper = new QueryWrapper<>();
                        pushLogQueryWrapper.eq("TaskId", materialPushTask.getLId());
                        pushLogQueryWrapper.eq("TypeId", materialPushTask.getSType());
                        pushLogQueryWrapper.eq("lRound", materialPushTask.getLRound());
                        pushLogQueryWrapper.eq("sPushScheduleTime", arrPushSchedule[i]);

                        LocalDateTime now = LocalDateTime.now();
                        pushLogQueryWrapper.ge("dNewTime", now.with(LocalTime.MIDNIGHT));
                        pushLogQueryWrapper.lt("dNewTime", now.plusDays(1).with(LocalTime.MIDNIGHT));

                        if (count(pushLogQueryWrapper) == 0) {
                            bCanPush = true;//可以推送
                        }

                        break;
                    }
                }

                if (!bCanPush) {
                    continue;//未达到推送排程条件
                }

                //队列1：优先发送，按最新发送
                //队列2：优先次之，按最早发送

                //第二，先计算队列1有没有数据，如果有，则把按最新发送

                String sMaterialLibIds = StrUtil.strip(materialPushTask.getMaterialLibIds(), ";");

                QueryWrapper<Material> materialQueryWrapper = new QueryWrapper<>();
                materialQueryWrapper.in("MaterialLibId", Arrays.stream(sMaterialLibIds.split(";")).toArray());

                //这里是要排除本轮已推送过的内容
                materialQueryWrapper.notInSql("lId", "SELECT MaterialId FROM MaterialPushLog WHERE " +
                        "TaskId='" + materialPushTask.getLId() + "' AND TypeId='" + materialPushTask.getSType() + "' " +
                        "AND lRound='" + materialPushTask.getLRound() + "'");

                materialQueryWrapper.eq("QueueId", 1);
                materialQueryWrapper.orderByDesc("dNewTime");//找最新
                materialQueryWrapper.last("LIMIT 1");
                pushMaterialList = materialService.list(materialQueryWrapper);

                if (pushMaterialList.size() == 0) {//队列1没有符合条件的内容
                    //到队列2里去找
                    materialQueryWrapper = new QueryWrapper<>();
                    materialQueryWrapper.in("MaterialLibId", Arrays.stream(sMaterialLibIds.split(";")).toArray());

                    //这里是要排除本轮已推送过的内容
                    materialQueryWrapper.notInSql("lId", "SELECT MaterialId FROM MaterialPushLog WHERE " +
                            "TaskId='" + materialPushTask.getLId() + "' AND TypeId='" + materialPushTask.getSType() + "' " +
                            "AND lRound='" + materialPushTask.getLRound() + "'");

                    materialQueryWrapper.eq("QueueId", 2);


                    if (materialPushTask.getSType().equals("moment")) {
                        if (materialPushTask.getTypeId() == 2) {//人设号
                            materialQueryWrapper.orderByDesc("dNewTime");//人设号找最新
                        } else {
                            materialQueryWrapper.orderByAsc("dNewTime");//业务号找最早
                        }
                    } else {
                        materialQueryWrapper.orderByAsc("dNewTime");//找最早
                    }

                    materialQueryWrapper.last("LIMIT 1");
                    pushMaterialList = materialService.list(materialQueryWrapper);
                }

                if (pushMaterialList.size() == 0) {//队列1和队列2都没有内容可以发送，跳过
                    if (materialPushTask.getBLoop() == 1) {//是否循环发送
                        QueryWrapper<MaterialPushLog> pushLogQueryWrapper = new QueryWrapper<>();
                        pushLogQueryWrapper.eq("TaskId", materialPushTask.getLId());
                        pushLogQueryWrapper.eq("TypeId", materialPushTask.getSType());
                        pushLogQueryWrapper.eq("lRound", materialPushTask.getLRound());
                        if (count(pushLogQueryWrapper) > 0) {//判断本轮是否已经有内容推送过了，如果有表明本轮已推完了，要进入下一轮推送
                            materialPushTask.setLRound(materialPushTask.getLRound() + 1);//下一轮
                            materialPushTaskService.updateById(materialPushTask);
                        }
                    }

                    continue;
                }

            } else {//立即发送，按顺序把未发送的消息发出去
                String sMaterialLibIds = StrUtil.strip(materialPushTask.getMaterialLibIds(), ";");

                //按发布的时间，查找最后一条，
                QueryWrapper<MaterialPushLog> logQueryWrapper = new QueryWrapper<>();
                logQueryWrapper.select("dNewMaterialTime");
                logQueryWrapper.eq("TaskId", materialPushTask.getLId());
                logQueryWrapper.eq("TypeId", materialPushTask.getSType());
                logQueryWrapper.eq("lRound", materialPushTask.getLRound());
                logQueryWrapper.orderByDesc("dNewMaterialTime");
                logQueryWrapper.last("LIMIT 1");
                MaterialPushLog one = getOne(logQueryWrapper);

                QueryWrapper<Material> materialQueryWrapper = new QueryWrapper<>();
                materialQueryWrapper = new QueryWrapper<>();
                materialQueryWrapper.in("MaterialLibId", Arrays.stream(sMaterialLibIds.split(";")).toArray());

                if (one != null) {
                    materialQueryWrapper.gt("dNewTime", one.getDNewMaterialTime());
                }

                materialQueryWrapper.orderByAsc("dNewTime");//立即发送，类似于同步其他群信息，所以要按顺序

                pushMaterialList = materialService.list(materialQueryWrapper);

                //为了确保不大量群发，只保留最后的3条数据
                if (materialPushTask.getLPushDelay() == 0) {
                    while (pushMaterialList.size() > 3) {
                        pushMaterialList.remove(0); // 移除第一个元素
                    }
                }
            }

            //有待推送的内容
            for (Material pushMaterial : pushMaterialList) {

                if (materialPushTask.getLPushDelay() > 0) {//如果有延时
                    Duration between = Duration.between(pushMaterial.getDCollectTime(), LocalDateTime.now());
                    if (between.toMinutes() < materialPushTask.getLPushDelay()) {
                        continue;//不满足延时的条件，跳过
                    }
                }

                MaterialPushLog materialPushLog = new MaterialPushLog();

                if (materialPushTask.getSType().equals("moment")) {
                    materialPushLog.setSName("[朋友圈推送]" + materialPushTask.getSName());
                    materialPushLog.setDTimingTime(LocalDateTime.now().plusMinutes(5));
                } else if (materialPushTask.getSType().equals("chatroom")) {
                    materialPushLog.setSName("[社群推送]" + materialPushTask.getSName());
                    materialPushLog.setDTimingTime(LocalDateTime.now());
                }

                materialPushLog.setDNewMaterialTime(pushMaterial.getDNewTime());
                materialPushLog.setDNewTime(LocalDateTime.now());
                materialPushLog.setTypeId(materialPushTask.getSType());
                materialPushLog.setTaskId(materialPushTask.getLId());
                materialPushLog.setMaterialId(pushMaterial.getLId());
                materialPushLog.setWeChatIds(materialPushTask.getPushWechatIds());
                materialPushLog.setChatRoomIds(materialPushTask.getPushWechatroomIds());
                materialPushLog.setSPushScheduleTime(tScheduleTime.toString());
                materialPushLog.setLRound(materialPushTask.getLRound());
                materialPushLog.setStatusId(0);

                String sContent = pushMaterial.getSContent();
                if (pushMaterial.getBAIGenerated() == 1) {
                    Random rand = new Random();
                    JSONArray arrContent = JSONArray.parseArray(pushMaterial.getSContentAI());

                    int randomIndex = rand.nextInt(arrContent.size());
                    sContent = arrContent.getString(randomIndex);
                }

                //如果是京东联盟的内容，还需要进行转链
                if (materialPushTask.getJdPromotionSiteId() != null) {
                    JSONObject jChainLink = jdUnionService.changeLink(sContent, materialPushTask.getJdPromotionSiteId());
                    if (jChainLink.getJSONObject("result").getString("msg").equals("全部成功")) {
                        sContent = jChainLink.getJSONObject("result").getString("chain_content");
                    }
                }

                materialPushLog.setSContent(sContent);//保留推送的内容

                if (materialPushTask.getSType().equals("moment")) {
                    JSONObject jsonMd5 = new JSONObject();

                    jsonMd5.put("content", sContent);

                    //推送的客服排序
                    String[] stringArray = materialPushTask.getPushWechatIds().split(",");
                    int[] intArray = new int[stringArray.length];
                    for (int i = 0; i < stringArray.length; i++) {
                        intArray[i] = Integer.parseInt(stringArray[i]);
                    }
                    Arrays.sort(intArray);
                    String result = Arrays.stream(intArray)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(","));
                    jsonMd5.put("wechatids", result);

                    //格式化时间
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                    jsonMd5.put("time", materialPushLog.getDTimingTime().format(formatter));
                    materialPushLog.setSMd5Json(jsonMd5.toJSONString());
                    materialPushLog.setSMd5(SecureUtil.md5(jsonMd5.toJSONString()));
                }

                save(materialPushLog);

                //更新任务的状态信息
                PushTaskParam pushTaskParam = new PushTaskParam();
                pushTaskParam.setTaskId(materialPushTask.getLId());
                pushTaskParam.setDLastPushTime(LocalDateTime.now());

                QueryWrapper<MaterialPushLog> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("TaskId", materialPushTask.getLId());
                queryWrapper.eq("TypeId", materialPushTask.getSType());
                pushTaskParam.setLPushedAmt(count(queryWrapper));

                materialPushTaskService.updateStatus(pushTaskParam);

                logger.info("发送到待推送队列:" + materialPushLog.getSName());
            }
        }
    }

    /**
     * 发布朋友圈到私域系统
     */
    public void publishMoment() {
        QueryWrapper<MaterialPushLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("StatusId", 0);
        queryWrapper.eq("TypeId", "moment");
        queryWrapper.orderByAsc("lId");
        List<MaterialPushLog> materialPushLogs = list(queryWrapper);

        for (MaterialPushLog materialPushLog : materialPushLogs) {
            try {
                Material material = materialService.getById(materialPushLog.getMaterialId());

                Map<String, Object> mapParam = new HashMap<>();

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                mapParam.put("beginTime", materialPushLog.getDTimingTime().format(formatter));
                mapParam.put("endTime", materialPushLog.getDTimingTime().plusMinutes(30).format(formatter));
                mapParam.put("text", materialPushLog.getSContent());

                if (material.getTypeId() == 1 || material.getTypeId() == 2) {
                    mapParam.put("momentContentType", material.getTypeId());
                } else if (material.getTypeId() == 3) {
                    mapParam.put("momentContentType", 4);

                    JSONObject link = new JSONObject();
                    link.put("desc", material.getSTitle());
                    link.put("url", material.getSLink());
                    if (material.getSThumb() != null) {
                        link.put("image", sysAttachService.getCdnUrl(material.getSThumb()));
                    } else {
                        link.put("image", "");
                    }

                    mapParam.put("link", link);

                    material.setSPic("");
                } else {
                    mapParam.put("momentContentType", 3);
                }

                if (StrUtil.isNotEmpty(material.getVideoId())) {
                    List<String> cdnUrls = sysAttachService.getCdnUrls(material.getVideoId().split(","));
                    mapParam.put("videoUrl", cdnUrls.get(0));
                } else {
                    mapParam.put("videoUrl", "");
                }

                if (StrUtil.isNotEmpty(material.getSPic())) {
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.addAll(sysAttachService.getCdnUrls(material.getSPic().split(",")));
                    mapParam.put("picUrlList", jsonArray);
                } else {
                    JSONArray jsonArray = new JSONArray();
                    mapParam.put("picUrlList", jsonArray);

                    //如果是图文，但是没有图片，则切换成文本
                    if (material.getTypeId() == 2) {
                        mapParam.put("momentContentType", 1);
                    }
                }

                JSONArray jsonArray = new JSONArray();
                for (String sWechatId : materialPushLog.getWeChatIds().split(",")) {
                    JSONObject jsonItem = new JSONObject();

                    if (StrUtil.isNotEmpty(material.getSComment())) {
                        List<String> arrComment = new ArrayList<>();
                        arrComment.add(material.getSComment());
                        jsonItem.put("comments", arrComment);
                    } else {
                        jsonItem.put("comments", new JSONArray());
                    }

                    jsonItem.put("labels", new JSONArray());
                    jsonItem.put("wechatAccountId", sWechatId);
                    jsonArray.add(jsonItem);
                }
                mapParam.put("jobPublishWechatMomentsItems", jsonArray);

                customerTouchApi.publishMoment(mapParam);
                materialPushLog.setStatusId(1);
                updateById(materialPushLog);
            } catch (Exception e) {
                logger.error(e.getMessage());

                materialPushLog.setStatusId(-1);
                updateById(materialPushLog);
            }
        }
    }

    /**
     * 把内容内容推送到聊天群
     */
    public synchronized void pushToChatroom() {
        QueryWrapper<MaterialPushLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("StatusId", 0);
        queryWrapper.eq("TypeId", "chatroom");
        queryWrapper.orderByAsc("lId");
        List<MaterialPushLog> materialPushLogs = list(queryWrapper);
        for (MaterialPushLog materialPushLog : materialPushLogs) {
            Material material = materialService.getById(materialPushLog.getMaterialId());

            String[] arrChatroomId = materialPushLog.getChatRoomIds().split(",");
            for (String sChatroomId : arrChatroomId) {
                Map<String, Object> mapParam = new HashMap<>();

                WeChatRoom weChatRoom = weChatRoomService.getById(sChatroomId);
                if (weChatRoom == null) {
                    continue;
                }

                mapParam.put("accountid", weChatRoom.getWechatId());
                mapParam.put("receiver", sChatroomId);
                if (material.getTypeId() == null) {
                    continue;
                } else if (material.getTypeId() == 2) {//图文
                    //图文的内容，先发图片，再发文本内容

                    //发图片
                    List<String> listCdnUrl = new ArrayList<>();
                    if (material.getSPic() != null) {//图片
                        listCdnUrl.addAll(sysAttachService.getCdnUrls(material.getSPic().split(",")));
                    }
                    if (listCdnUrl.size() > 0) {
                        for (String sCdnUrl : listCdnUrl) {
                            mapParam.put("msgtype", 3);
                            mapParam.put("content", sCdnUrl);
                            customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);

                            if (StrUtil.isNotEmpty(materialPushLog.getSContent())) {
                                try {
                                    Thread.sleep(4000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    //发文字
                    if (StrUtil.isNotEmpty(materialPushLog.getSContent())) {
                        mapParam.put("msgtype", 1);
                        mapParam.put("content", materialPushLog.getSContent());
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);
                    }
                } else if (material.getTypeId() == 3) {//链接
                    //发文字
                    if (StrUtil.isNotEmpty(materialPushLog.getSContent())) {
                        mapParam.put("msgtype", 1);
                        mapParam.put("content", materialPushLog.getSContent());
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    //发链接
                    mapParam.put("msgtype", 49);
                    JSONObject jsonLink = new JSONObject();
                    jsonLink.put("type", "link");
                    jsonLink.put("url", material.getSLink());
                    jsonLink.put("title", material.getSTitle());
                    jsonLink.put("desc", materialPushLog.getSContent());

                    String cdnUrl = sysAttachService.getCdnUrl(material.getSThumb());
                    jsonLink.put("thumbPath", cdnUrl);

                    mapParam.put("content", jsonLink.toJSONString());

                    customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);
                } else if (material.getTypeId() == 4) {//视频

                    //发文字
                    if (StrUtil.isNotEmpty(materialPushLog.getSContent())) {
                        mapParam.put("msgtype", 1);
                        mapParam.put("content", materialPushLog.getSContent());
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mapParam.put("msgtype", 43);

                    List<String> listCdnUrl = new ArrayList<>();
                    if (material.getVideoId() != null) {//视频
                        listCdnUrl.addAll(sysAttachService.getCdnUrls(material.getVideoId().split(",")));
                    } else {
                        break;//视频还没下载，不能发送
                    }

                    for (String sCdnUrl : listCdnUrl) {
                        mapParam.put("content", sCdnUrl);
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);
                    }
                } else if (material.getTypeId() == 1) {//文本
                    if (StrUtil.isNotEmpty(materialPushLog.getSContent())) {
                        mapParam.put("msgtype", 1);
                        mapParam.put("content", materialPushLog.getSContent());
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);
                    }
                } else if (material.getTypeId() == 5) {//小程序

                    //发文字
                    if (StrUtil.isNotEmpty(materialPushLog.getSContent())) {
                        mapParam.put("msgtype", 1);
                        mapParam.put("content", materialPushLog.getSContent());
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);

                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    mapParam.put("msgtype", 49);

                    JSONObject jsonObject = null;
                    if (material.getMiniProgrameLibId() != null && material.getMiniProgrameLibId() != 0) {//从小程序库里提取小程序码
                        MiniProgrameLib miniProgrameLib = miniProgrameLibService.getById(material.getMiniProgrameLibId());
                        jsonObject = JSONObject.parseObject(miniProgrameLib.getSContent());
                    } else {
                        jsonObject = JSONObject.parseObject(material.getSSourceContent());
                    }

                    //替换封面图片
                    if (StrUtil.isNotEmpty(material.getSThumb())) {
                        String cdnUrl = sysAttachService.getCdnUrl(material.getSThumb());
                        jsonObject.put("previewImage", cdnUrl);
                    }

                    //替换标题
                    Pattern pattern = Pattern.compile("<title>(.*?)</title>");
                    Matcher matcher = pattern.matcher(jsonObject.getString("contentXml"));
                    String contentXml = matcher.replaceAll("<title>" + material.getSTitle() + "</title>");
                    jsonObject.put("contentXml", contentXml);
                    mapParam.put("content", jsonObject.toJSONString());

                    customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);
                } else if (material.getTypeId() == 6) {//视频号
                    mapParam.put("msgtype", 754974769);

                    JSONObject jsonObject = JSONObject.parseObject(material.getSSourceContent());
                    jsonObject.put("desc", material.getSTitle());
                    mapParam.put("content", jsonObject.toJSONString());

                    customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);
                } else if (material.getTypeId() == -1) {//无法识别的类型
                    continue;
                } else {
                    if (StrUtil.isNotEmpty(material.getSSourceContent())) {
                        mapParam.put("msgtype", material.getTypeId());
                        mapParam.put("content", material.getSSourceContent());
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tochatroom", mapParam);
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            materialPushLog.setStatusId(4);
            updateById(materialPushLog);
        }
    }


    /**
     * 拉取发朋友圈的列表
     */
    public void publishMomentList() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("pageSize", 30);
        jsonObject.put("from", LocalDate.now().minusDays(30).format(formatter));
        jsonObject.put("to", LocalDate.now().format(formatter));
        jsonObject.put("pageIndex", 0);

        JSONArray arrMoment = customerTouchApi.publishMomentList(jsonObject);
        for (int i = 0; i < arrMoment.size(); i++) {
            JSONObject jsonMoment = arrMoment.getJSONObject(i);

            JSONObject jsonMd5 = new JSONObject();
            jsonMd5.put("content", jsonMoment.getString("text"));
            jsonMd5.put("time", jsonMoment.getString("timingTime"));

            JSONArray jobPublishWechatMomentsItems = jsonMoment.getJSONArray("jobPublishWechatMomentsItems");
            int[] intArray = new int[jobPublishWechatMomentsItems.size()];
            for (int j = 0; j < jobPublishWechatMomentsItems.size(); j++) {
                intArray[j] = jobPublishWechatMomentsItems.getJSONObject(j).getIntValue("wechatAccountId");
            }
            Arrays.sort(intArray);
            String result = Arrays.stream(intArray)
                    .mapToObj(String::valueOf)
                    .collect(Collectors.joining(","));
            jsonMd5.put("wechatids", result);

            String sMd5 = SecureUtil.md5(jsonMd5.toJSONString());
            QueryWrapper<MaterialPushLog> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sMd5", sMd5);
            queryWrapper.eq("TypeId", "moment");
            List<MaterialPushLog> materialPushLogList = list(queryWrapper);
            for (MaterialPushLog materialPushLog : materialPushLogList) {
                materialPushLog.setRemoteId(jsonMoment.getIntValue("id"));

                if (jsonMoment.getIntValue("jobStatus") == 0) {
                    materialPushLog.setStatusId(2);
                } else if (jsonMoment.getIntValue("jobStatus") == 1) {
                    materialPushLog.setStatusId(3);
                } else if (jsonMoment.getIntValue("jobStatus") == 2) {
                    materialPushLog.setStatusId(4);
                }

                updateById(materialPushLog);
            }
        }
    }

    /**
     * 获得朋友圈的详情
     *
     * @param id
     * @return
     */
    public JSONArray getMomentStatus(Integer id) {
        MaterialPushLog materialPushLog = getById(id);
        if (materialPushLog.getRemoteId() != null) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("id", materialPushLog.getRemoteId());
            return customerTouchApi.getMomentDetail(jsonObject);
        }
        return new JSONArray();
    }
}
