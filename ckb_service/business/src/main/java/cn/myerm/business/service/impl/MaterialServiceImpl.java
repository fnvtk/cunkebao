package cn.myerm.business.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.entity.*;
import cn.myerm.business.event.EventApi;
import cn.myerm.business.mapper.MaterialMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.param.MaterialParam;
import cn.myerm.business.service.IMaterialService;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.system.entity.SysAttach;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MaterialServiceImpl extends BusinessServiceImpl<MaterialMapper, Material> implements IMaterialService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialServiceImpl.class);

    private final WeChatServiceImpl weChatService;

    private final WeChatFriendServiceImpl weChatFriendService;

    private final CacheServiceImpl cacheService;

    private final CustomerTouchApi customerTouchApi;

    private final SysAttachServiceImpl sysAttachService;

    private final MaterialLibServiceImpl materialLibService;

    private final WeChatRoomServiceImpl weChatRoomService;

    private final WeChatRoomMemberServiceImpl weChatRoomMemberService;

    private final DownloadVideoScheduleServiceImpl downloadVideoScheduleService;

    private final EventApi eventApi;

    @Autowired
    public MaterialServiceImpl(WeChatServiceImpl weChatService, WeChatFriendServiceImpl weChatFriendService,
                               CacheServiceImpl cacheService, CustomerTouchApi customerTouchApi, SysAttachServiceImpl sysAttachService, MaterialLibServiceImpl materialLibService, WeChatRoomServiceImpl weChatRoomService, WeChatRoomMemberServiceImpl weChatRoomMemberService, DownloadVideoScheduleServiceImpl downloadVideoScheduleService, EventApi eventApi) {
        this.weChatService = weChatService;
        this.weChatFriendService = weChatFriendService;
        this.cacheService = cacheService;
        this.customerTouchApi = customerTouchApi;
        this.sysAttachService = sysAttachService;
        this.materialLibService = materialLibService;
        this.weChatRoomService = weChatRoomService;
        this.weChatRoomMemberService = weChatRoomMemberService;
        this.downloadVideoScheduleService = downloadVideoScheduleService;
        this.eventApi = eventApi;
    }

    public static String extractImageName(String imageUrl) {
        int lastSlashIndex = imageUrl.lastIndexOf("/");
        if (lastSlashIndex != -1 && lastSlashIndex < imageUrl.length() - 1) {
            return imageUrl.substring(lastSlashIndex + 1);
        }
        return "";
    }

    /**
     * 视图按钮
     *
     * @return JSONArray
     */
    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();
        btn.put("ID", "new");
        btn.put("sName", "新建");
        btn.put("handler", "handleNewMaterial");
        btn.put("icon", "el-icon-plus");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "del");
        btn.put("sName", "删除");
        btn.put("handler", "handleDel");
        btn.put("icon", "el-icon-delete");
        arrBtn.add(btn);

        MaterialLib materialLib = materialLibService.getById(mapParam.get("ObjectId"));
        if (materialLib != null && materialLib.getBAIEnable() == 1) {
            btn = new JSONObject();
            btn.put("ID", "generateaicontent");
            btn.put("sName", "重新生成AI内容");
            btn.put("handler", "handleGenerateAiContent");
            btn.put("icon", "el-icon-refresh-right");
            arrBtn.add(btn);
        }

        btn = new JSONObject();
        btn.put("ID", "refresh");
        btn.put("sName", "刷新");
        btn.put("handler", "handleRefresh");
        btn.put("icon", "el-icon-refresh");
        arrBtn.add(btn);

        return arrBtn;
    }

    /**
     * 新建保存
     *
     * @param materialParam
     */
    public Integer newSave(MaterialParam materialParam) {
        logger.info(materialParam.toString());

        Material material = new Material();
        material.setSName("人工添加内容");
        material.setDNewTime(LocalDateTime.parse(materialParam.getDNewTime()));
        material.setDEditTime(LocalDateTime.now());
        material.setNewUserId(getCurrUser().getLID());
        material.setSTitle(materialParam.getSTitle());
        material.setSLink(materialParam.getSLink());
        material.setSHead(materialParam.getSHead());
        material.setSFoot(materialParam.getSFoot());
        material.setSContent(materialParam.getSContent());
        material.setSComment(materialParam.getSComment());
        material.setTypeId(materialParam.getTypeId());
        material.setQueueId(2);
        material.setMaterialLibId(materialParam.getMaterialLibId());
        material.setBManual(1);
        material.setBClassify(1);

        if (materialParam.getTypeId() == 2) {//图片
            material.setSPic(materialParam.getSPic());

            //取第一个作为主图
            if (materialParam.getSPic() != null && !materialParam.getSPic().equals("")) {
                String[] arrPicId = material.getSPic().split(",");
                material.setSThumb(arrPicId[0]);
            }
        } else if (materialParam.getTypeId() == 4) {//视频
            material.setVideoId(materialParam.getVideoId());
        } else if (materialParam.getTypeId() == 5) {//小程序
            JSONObject jsonObject = JSONObject.parseObject(materialParam.getSMP());

            material.setSSourceContent(jsonObject.getString("sContent"));

            if (StrUtil.isNotEmpty(materialParam.getSPic())) {
                material.setSPic(materialParam.getSPic());

                String[] arrPicId = material.getSPic().split(",");
                material.setSThumb(arrPicId[0]);
            } else {
                Integer ThumbId = saveToAttach(jsonObject.getString("sThumb"));
                material.setSThumb(ThumbId + "");
            }

            material.setMiniProgrameLibId(jsonObject.getIntValue("lId"));
        }

        material.setSourceId("manual");
        material.setCollectObjectId("/manual/" + material.getNewUserId());
        save(material);

        material.setSourceDataId("/manual/" + material.getNewUserId() + "/" + material.getLId());
        updateById(material);

        updateAmt(material.getMaterialLibId());

        return material.getLId();
    }

    /**
     * 编辑保存
     *
     * @param materialParam
     */
    public void editSave(MaterialParam materialParam) {
        Material material = new Material();
        material.setLId(materialParam.getLId());

        material.setDNewTime(LocalDateTime.parse(materialParam.getDNewTime().replace(" ", "T")));
        material.setDEditTime(LocalDateTime.now());
        material.setSTitle(materialParam.getSTitle());
        material.setSLink(materialParam.getSLink());
        material.setSHead(materialParam.getSHead());
        material.setSFoot(materialParam.getSFoot());
        material.setSContent(materialParam.getSContent());
        material.setSComment(materialParam.getSComment());
        material.setTypeId(materialParam.getTypeId());

        if (materialParam.getTypeId() == 2) {//图片
            material.setSPic(materialParam.getSPic());

            //取第一个作为主图
            if (materialParam.getSPic() != null && !materialParam.getSPic().equals("")) {
                String[] arrPicId = material.getSPic().split(",");
                material.setSThumb(arrPicId[0]);
            }

            material.setVideoId("");
        } else {
            material.setSPic("");
            material.setSThumb("");
        }

        if (materialParam.getTypeId() == 4) {
            material.setVideoId(materialParam.getVideoId());
            material.setSPic("");
            material.setSThumb("");
        } else {
            material.setVideoId("");
        }

        if (materialParam.getTypeId() == 5) {//小程序
            JSONObject jsonObject = JSONObject.parseObject(materialParam.getSMP());

            material.setSSourceContent(jsonObject.getString("sContent"));

            if (StrUtil.isNotEmpty(materialParam.getSPic())) {
                material.setSPic(materialParam.getSPic());

                String[] arrPicId = material.getSPic().split(",");
                material.setSThumb(arrPicId[0]);
            } else {
                Integer ThumbId = saveToAttach(jsonObject.getString("sThumb"));
                material.setSThumb(ThumbId + "");
            }

            material.setMiniProgrameLibId(jsonObject.getIntValue("lId"));
        }

        if (materialParam.getTypeId() == 3) {//链接
            material.setSPic(materialParam.getSPic());

            //取第一个作为主图
            if (materialParam.getSPic() != null && !materialParam.getSPic().equals("")) {
                String[] arrPicId = material.getSPic().split(",");
                material.setSThumb(arrPicId[0]);
            }
        }


        updateById(material);
    }

    /**
     * 从触客系统拉取朋友圈的数据，存入内容库
     */
    public void pullFromMoment() {
        String starttime = cacheService.get("syncwechatmoment");
        if (StrUtil.isEmpty(starttime)) {
            starttime = "";
        }

        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("starttime", starttime);
        mapParam.put("pagesize", 100);

        logger.info("开始拉取朋友圈：" + starttime);

        boolean bCanClassify = false;
        int lPage = 1;
        while (true) {
            mapParam.put("page", lPage);

            JSONArray arrJsonMoment = customerTouchApi.pullMoment(mapParam);
            if (arrJsonMoment.size() > 0) {
                for (int i = 0; i < arrJsonMoment.size(); i++) {
                    JSONObject jsonMoment = arrJsonMoment.getJSONObject(i);

                    if (jsonMoment.getIntValue("type") == 15) {
                        //现在视频无法支持
                        continue;
                    }

                    String sSourceDataId = "/moment/" + jsonMoment.getString("wechatFriendId") + "/" + jsonMoment.getString("snsId");
                    QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("SourceDataId", sSourceDataId);
                    queryWrapper.last("LIMIT 1");
                    Material material = getOne(queryWrapper);
                    if (material == null) {
                        material = new Material();
                    }

                    material.setSName("朋友圈采集");

                    Date date = DateUtil.date(jsonMoment.getLongValue("createTime") * 1000L);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
                    String formattedDateTime = sdf.format(date);
                    material.setDNewTime(LocalDateTime.parse(formattedDateTime.replace(" ", "T")));

                    material.setDEditTime(LocalDateTime.now());
                    material.setDCollectTime(LocalDateTime.parse(jsonMoment.getString("collectTime").replace(" ", "T")));


                    int lTypeId = 0;
                    if (jsonMoment.getIntValue("type") == 1) {//图文
                        lTypeId = 2;
                    } else if (jsonMoment.getIntValue("type") == 15) {//视频
                        lTypeId = 4;
                    } else if (jsonMoment.getIntValue("type") == 2) {//纯文本
                        lTypeId = 1;
                    } else if (jsonMoment.getIntValue("type") == 3) {//链接
                        lTypeId = 3;
                    } else {
                        //lTypeId = jsonMoment.getIntValue("type");
                        continue;//其他类别暂时不支持
                    }

                    material.setSContent(jsonMoment.getString("content"));
                    material.setSTitle(jsonMoment.getString("title"));
                    material.setTypeId(lTypeId);
                    material.setSourceId("moment");
                    material.setSourceDataId(sSourceDataId);
                    material.setCollectObjectId("/moment/" + jsonMoment.getString("wechatFriendId"));
                    material.setBClassify(0);

                    Object[] arrUrl = jsonMoment.getJSONArray("urls").toArray();
                    if (lTypeId == 3) {
                        material.setSLink((String) arrUrl[0]);

                        if (jsonMoment.getString("coverImage") != null) {
                            SysAttach sysAttach = new SysAttach();
                            sysAttach.setSObjectName("business/material");
                            sysAttach.setSFilePath(jsonMoment.getString("coverImage"));
                            sysAttach.setDNewTime(LocalDateTime.now());
                            sysAttach.setNewUserId(1);
                            sysAttach.setSCdnUrl(jsonMoment.getString("coverImage"));
                            sysAttach.setBImage(1);
                            sysAttachService.save(sysAttach);

                            material.setSPic(sysAttach.getLID() + "");
                            material.setSThumb(sysAttach.getLID() + "");
                        }
                    } else if (lTypeId == 2 && arrUrl.length > 0) {
                        List<String> arrAttachId = new ArrayList<>();
                        for (Object url : arrUrl) {
                            String sUrl = (String) url;

                            String sFileName = extractImageName(sUrl);

                            SysAttach sysAttach = new SysAttach();
                            sysAttach.setSName(sFileName);
                            sysAttach.setSObjectName("business/material");
                            sysAttach.setSFilePath(sUrl);
                            sysAttach.setDNewTime(LocalDateTime.now());
                            sysAttach.setNewUserId(1);
                            sysAttach.setSCdnUrl(sUrl);
                            sysAttach.setBImage(sysAttachService.isImage(sFileName) ? 1 : 0);
                            sysAttachService.save(sysAttach);

                            arrAttachId.add(sysAttach.getLID() + "");
                        }

                        String sPicId = String.join(",", arrAttachId);
                        material.setSPic(sPicId);
                        material.setSThumb(arrAttachId.get(0));
                    }
                    saveOrUpdate(material);
                    bCanClassify = true;
                }
            } else {
                break;
            }

            lPage++;
        }

        //把最晚的时间作为时间截点
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SourceId", "moment");
        queryWrapper.orderByDesc("dCollectTime");
        queryWrapper.last("LIMIT 1");
        Material one = getOne(queryWrapper);
        if (one != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            cacheService.set("syncwechatmoment", one.getDCollectTime().format(formatter));
        }

        if (bCanClassify) {
            classify();
        }
    }

    /**
     * 拉取群消息
     */
    public synchronized void pullChatroomMessage() {

        JSONArray collectObjects = materialLibService.getCollectObjects();

        //初始化采集的配置
        HashMap<Integer, HashMap<String, Integer>> mapChatroomMemberId = new HashMap<>();
        ArrayList<Integer> arrChatroomId = new ArrayList<>();

        for (int i = 0; i < collectObjects.size(); i++) {
            JSONObject collectObject = collectObjects.getJSONObject(i);

            String sCollectObjectId = collectObject.getString("sCollectObjectId");

            if (StrUtil.startWith(sCollectObjectId, "/chatroom")) {//寻找群配置
                arrChatroomId.add(collectObject.getIntValue("chatroomId"));

                HashMap<String, Integer> mapMemberId = mapChatroomMemberId.get(collectObject.getIntValue("chatroomId"));
                if (mapMemberId == null) {
                    mapMemberId = new HashMap<>();
                }

                mapMemberId.put(collectObject.getString("sWechatId"), 1);
                mapChatroomMemberId.put(collectObject.getIntValue("chatroomId"), mapMemberId);
            }
        }

        if (arrChatroomId.size() == 0) {
            return;
        }

        int lPull = 0;
        boolean bCanClassify = false;
        List<WeChatRoom> weChatRooms = weChatRoomService.listByIds(arrChatroomId);
        for (WeChatRoom weChatRoom : weChatRooms) {
            //查找最新一条的消息（内容），目的是拿到lWechatTime
            long lWechatTime = System.currentTimeMillis() - 5 * 60 * 1000L;//默认五分钟前
            QueryWrapper<Material> materialQueryWrapper = new QueryWrapper<>();
            materialQueryWrapper.select("lWechatTime");
            materialQueryWrapper.likeRight("CollectObjectId", "/chatroom/" + weChatRoom.getLId() + "/");
            materialQueryWrapper.orderByDesc("lWechatTime");
            materialQueryWrapper.last("LIMIT 1");
            Material newMaterial = getOne(materialQueryWrapper);
            if (newMaterial != null) {
                lWechatTime = newMaterial.getLWechatTime();
            }

            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("keyword", "");
            mapParam.put("msgtype", "");
            mapParam.put("count", 20);
            mapParam.put("accountid", weChatRoom.getWechatId());
            mapParam.put("chatroomid", weChatRoom.getLId());
            mapParam.put("from", lWechatTime - 300 * 1000L);
            mapParam.put("to", System.currentTimeMillis() + 3600 * 1000L);//一个小时后
            mapParam.put("olderdata", true);
            JSONArray arrMsg = customerTouchApi.pullChatroomMessage(mapParam);

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            HashMap<String, Integer> mapMemberId = mapChatroomMemberId.get(weChatRoom.getLId());
            for (int i = 0; i < arrMsg.size(); i++) {
                JSONObject msg = arrMsg.getJSONObject(i);

                JSONObject sender = msg.getJSONObject("sender");
                if (sender == null) {
                    continue;
                }

                String sWechatId = sender.getString("wechatId");
                if (mapMemberId.get(sWechatId) == null) {
                    continue;//不在采集配置中，跳过
                }

                //提取内容
                String sContent = extractContent(msg.getString("content"));

                String sSourceDataId = "/chatroom/" + msg.getString("wechatChatroomId") + "/" + sWechatId + "/" + msg.getString("id");
                QueryWrapper<Material> objectQueryWrapper = new QueryWrapper<>();
                objectQueryWrapper.eq("SourceDataId", sSourceDataId);
                objectQueryWrapper.last("LIMIT 1");
                Material material = getOne(objectQueryWrapper);
                if (material == null) {
                    material = new Material();
                } else {
                    continue;//已经入库了，不必重复入库
                }

                material.setSName("微信聊天群消息");

                Instant instant = Instant.ofEpochMilli(msg.getLongValue("wechatTime"));
                LocalDateTime createTime = LocalDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai"));
                material.setDNewTime(createTime);

                material.setDEditTime(LocalDateTime.now());
                material.setDCollectTime(LocalDateTime.now());
                material.setLWechatTime(msg.getLongValue("wechatTime"));
                material.setSourceId("chatroom");

                Long lTypeId = msg.getLongValue("msgType");
                if (lTypeId == 1) {
                    material.setTypeId(1);//文本
                    material.setSContent(sContent);
                } else if (lTypeId == 3) {
                    material.setTypeId(2);//图片

                    Integer AttachId = saveToAttach(sContent);
                    material.setSThumb(AttachId + "");//主图
                    material.setSPic(AttachId + "");//图片
                } else if (lTypeId == 43) {
                    //视频比较特殊，它要先下载资源，才能拿到真实的视频地址。因此，还要异步发送采集命令。
                    material.setTypeId(4);//视频
                    material.setSSourceContent(sContent);

                    try {
                        JSONObject jsonContent = JSONObject.parseObject(sContent);
                        Integer AttachId = saveToAttach(jsonContent.getString("previewImage"));
                        material.setSThumb(AttachId + "");//主图
                    } catch (RuntimeException e) {
                        Integer AttachId = saveToAttach(sContent);
                        material.setVideoId(AttachId.toString());
                    }
                } else if (lTypeId == 49) {//链接：普通链接、小程序
                    JSONObject jsonContent = JSONObject.parseObject(sContent);
                    material.setSSourceContent(sContent);

                    if (jsonContent.getString("type").equals("link")) {//普通链接
                        material.setTypeId(3);//链接
                        material.setSLink(jsonContent.getString("url"));
                        material.setSTitle(jsonContent.getString("title"));
                        material.setSContent(jsonContent.getString("desc"));

                        String sThumbUrl = jsonContent.getString("thumburl");
                        if (StrUtil.isEmpty(sThumbUrl)) {
                            sThumbUrl = jsonContent.getString("thumbPath");
                        }
                        String sFileName = extractImageName(sThumbUrl);
                        SysAttach sysAttach = new SysAttach();
                        sysAttach.setSName(sFileName);
                        sysAttach.setSObjectName("business/material");
                        sysAttach.setSFilePath(sThumbUrl);
                        sysAttach.setDNewTime(LocalDateTime.now());
                        sysAttach.setNewUserId(1);
                        sysAttach.setSCdnUrl(sThumbUrl);
                        sysAttach.setBImage(1);
                        sysAttachService.save(sysAttach);

                        material.setSThumb(sysAttach.getLID() + "");
                        material.setSPic(sysAttach.getLID() + "");//图片
                    } else if (jsonContent.getString("type").equals("miniprogram")) {//小程序
                        material.setTypeId(5);//链接

                        if (jsonContent.getString("previewImage") != null) {
                            Integer AttachId = saveToAttach(jsonContent.getString("previewImage"));
                            material.setSThumb(AttachId + "");//主图
                            material.setSPic(AttachId + "");//图片
                        }

                        String regex = "<title>(.*?)</title>";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(sContent);
                        if (matcher.find()) {
                            material.setSTitle(matcher.group(1));
                        }
                    }
                } else if (lTypeId == 822083633) {//引用
                    JSONObject jsonContent = JSONObject.parseObject(sContent);

                    material.setTypeId(1);//文本
                    material.setSContent(jsonContent.getString("content"));
                } else if (lTypeId == 90001) {//AT
                    JSONObject jsonContent = JSONObject.parseObject(sContent);
                    material.setTypeId(1);//文本
                    material.setSContent(jsonContent.getString("text"));
                } else if (lTypeId == 754974769) {//视频号
                    material.setTypeId(6);//视频号
                    material.setSSourceContent(sContent);

                    //提取主图
                    JSONObject jsonContent = JSONObject.parseObject(sContent);
                    String sThumbUrl = jsonContent.getString("thumbUrl");
                    if (StrUtil.isEmpty(sThumbUrl)) {
                        sThumbUrl = jsonContent.getString("thumbOssUrl");
                    }

                    String sFileName = extractImageName(sThumbUrl);
                    SysAttach sysAttach = new SysAttach();
                    sysAttach.setSName("");
                    sysAttach.setSObjectName("business/material");
                    sysAttach.setSFilePath(sThumbUrl);
                    sysAttach.setDNewTime(LocalDateTime.now());
                    sysAttach.setNewUserId(1);
                    sysAttach.setSCdnUrl(sThumbUrl);
                    sysAttach.setBImage(1);
                    sysAttachService.save(sysAttach);

                    material.setSThumb(sysAttach.getLID() + "");
                    material.setSPic(sysAttach.getLID() + "");//图片
                    material.setSTitle(jsonContent.getString("desc"));
                } else if (lTypeId == 12351883) {//表情
                    material.setTypeId(12351883);
                    material.setSSourceContent(sContent);
                } else if (lTypeId == 21377) {//收藏
                    material.setTypeId(21377);
                    material.setSSourceContent(sContent);
                } else {
                    material.setTypeId(lTypeId.intValue());
                    material.setSSourceContent(sContent);
                }

                material.setSourceDataId(sSourceDataId);
                material.setCollectObjectId("/chatroom/" + msg.getString("wechatChatroomId") + "/" + sWechatId);
                save(material);
                bCanClassify = true;

                if (lTypeId == 43 && StrUtil.isEmpty(material.getVideoId())) {//视频，要异步下载
                    DownloadVideoSchedule downloadVideoSchedule = new DownloadVideoSchedule();
                    downloadVideoSchedule.setLId(material.getLId());
                    downloadVideoSchedule.setSType("chatroom");
                    downloadVideoSchedule.setWechatId(msg.getInteger("wechatAccountId"));
                    downloadVideoSchedule.setChatroomId(msg.getInteger("wechatChatroomId"));
                    downloadVideoSchedule.setMessageId(msg.getInteger("id"));

                    JSONObject jsonContent = JSONObject.parseObject(sContent);
                    downloadVideoSchedule.setSTencentUrl(jsonContent.getString("tencentUrl"));
                    downloadVideoScheduleService.save(downloadVideoSchedule);
                }
            }
        }

        if (bCanClassify) {
            classify();
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
        sysAttach.setSCdnUrl(sUrl);
        sysAttach.setBImage(sysAttachService.isImage(sFileName) ? 1 : 0);
        sysAttachService.save(sysAttach);

        return sysAttach.getLID();
    }

    /**
     * 给内容进行分类：
     */
    public void classify() {

        //查询出所有的
        HashMap<String, LocalDateTime> mapTime = new HashMap<>();
        JSONArray collectObjects = materialLibService.getCollectObjects();
        for (int i = 0; i < collectObjects.size(); i++) {
            JSONObject materialCollectObject = collectObjects.getJSONObject(i);

            //在当前的内容库里，查找这个采集对象且队列=1，最新的内容，取它的发布时间，作为标尺
            //所谓的标尺，就是队列1和2的分界点
            QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("dNewTime");
            queryWrapper.eq("MaterialLibId", materialCollectObject.getIntValue("materialLibId"));
            queryWrapper.eq("CollectObjectId", materialCollectObject.getString("sCollectObjectId"));
            queryWrapper.eq("QueueId", 1);
            queryWrapper.orderByAsc("dNewTime");
            queryWrapper.last("LIMIT 1");
            Material one = getOne(queryWrapper);

            if (one == null) {//队列1找不到，再取0找
                queryWrapper = new QueryWrapper<>();
                queryWrapper.select("dNewTime");
                queryWrapper.eq("MaterialLibId", materialCollectObject.getIntValue("materialLibId"));
                queryWrapper.eq("CollectObjectId", materialCollectObject.getString("sCollectObjectId"));
                queryWrapper.eq("QueueId", 0);
                queryWrapper.orderByDesc("dNewTime");
                queryWrapper.last("LIMIT 1");
                one = getOne(queryWrapper);
                if (one != null) {
                    one.setDNewTime(one.getDNewTime().minusSeconds(1));//提早一秒，为的是排除第一条数据
                }
            }

            if (one == null) {//1,0都有没，去原始内容寻找
                queryWrapper = new QueryWrapper<>();
                queryWrapper.select("dNewTime");
                queryWrapper.isNull("MaterialLibId");
                queryWrapper.eq("CollectObjectId", materialCollectObject.getString("sCollectObjectId"));
                queryWrapper.eq("QueueId", 0);
                queryWrapper.orderByDesc("dNewTime");
                queryWrapper.last("LIMIT 1");
                one = getOne(queryWrapper);
                if (one != null) {
                    one.setDNewTime(one.getDNewTime().minusSeconds(1));//提早一秒，为的是排除第一条数据，第一条要归队列1
                }
            }

            if (one != null) {
                mapTime.put(materialCollectObject.getIntValue("materialLibId") + materialCollectObject.getString("sCollectObjectId"), one.getDNewTime());
            }


            materialCollectObject.put("startDate", null);
            materialCollectObject.put("endDate", null);

            String sTimeLimit = materialCollectObject.getString("sTimeLimit");
            if (StrUtil.isNotEmpty(sTimeLimit)) {
                String[] times = sTimeLimit.split("/");
                if (times.length > 0) {
                    if (StrUtil.isNotEmpty(times[0])) {
                        materialCollectObject.put("startDate", times[0]);
                    }

                    if (times.length == 2 && StrUtil.isNotEmpty(times[1])) {
                        materialCollectObject.put("endDate", times[1]);
                    }
                }
            }
        }

        Set<Integer> setMaterialLibId = new HashSet<>();
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bClassify", 0);
        queryWrapper.isNull("MaterialLibId");
        queryWrapper.eq("bManual", 0);
        List<Material> materialList = list(queryWrapper);
        for (Material material : materialList) {
            for (int i = 0; i < collectObjects.size(); i++) {
                JSONObject materialCollectObject = collectObjects.getJSONObject(i);
                if (material.getCollectObjectId().equals(materialCollectObject.getString("sCollectObjectId"))) {

                    String startDate = materialCollectObject.getString("startDate");
                    String endDate = materialCollectObject.getString("endDate");

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    if (startDate != null) {
                        LocalDateTime dStartDate = LocalDateTime.parse(startDate + " 00:00:00", formatter);
                        if (material.getDNewTime().isBefore(dStartDate)) {
                            continue;
                        }
                    }

                    if (endDate != null) {
                        LocalDateTime dEndDate = LocalDateTime.parse(endDate + " 23:59:59", formatter);
                        if (material.getDNewTime().isAfter(dEndDate)) {
                            continue;
                        }
                    }

                    boolean bPass = true;
                    if (StrUtil.isNotEmpty(materialCollectObject.getString("sKeyWord"))) {
                        String[] arrKeyWord = materialCollectObject.getString("sKeyWord").split(",");
                        for (String sKeyWord : arrKeyWord) {
                            if (material.getSContent().indexOf(sKeyWord) > -1) {
                                bPass = false;
                                break;
                            }
                        }
                    } else {
                        bPass = false;
                    }

                    if (bPass) {//没有匹配到关键字
                        continue;
                    }

                    try {

                        QueryWrapper<Material> objectQueryWrapper = new QueryWrapper<>();
                        objectQueryWrapper.eq("MaterialId", material.getLId());
                        objectQueryWrapper.eq("MaterialLibId", materialCollectObject.getIntValue("materialLibId"));
                        objectQueryWrapper.last("LIMIT 1");
                        Material cloneMaterial = getOne(objectQueryWrapper);
                        if (cloneMaterial == null) {
                            cloneMaterial = material.clone();
                            cloneMaterial.setLId(null);
                        }

                        cloneMaterial.setBClassify(1);
                        cloneMaterial.setMaterialId(material.getLId());
                        cloneMaterial.setMaterialLibId(materialCollectObject.getIntValue("materialLibId"));
                        cloneMaterial.setNewUserId(materialCollectObject.getIntValue("newUserId"));

                        setMaterialLibId.add(materialCollectObject.getIntValue("materialLibId"));

                        //分队列
                        LocalDateTime localDateTime = mapTime.get(materialCollectObject.getIntValue("materialLibId") + materialCollectObject.getString("sCollectObjectId"));
                        if (material.getDNewTime().compareTo(localDateTime) > 0) {
                            cloneMaterial.setQueueId(1);
                        } else {
                            cloneMaterial.setQueueId(2);
                        }

                        saveOrUpdate(cloneMaterial);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }

            material.setBClassify(1);
        }

        updateBatchById(materialList);

        for (Integer MaterialLibId : setMaterialLibId) {
            updateAmt(MaterialLibId);
        }

    }

    /**
     * 重新归类该内容库
     * 目的是新建编辑内容库后，重新把内容归类到内容库之下
     *
     * @param materialLib
     */
    public void flush(Integer MaterialLibId) {
        ArrayList<String> collectObjectIds = new ArrayList<>();
        List<MaterialCollectObject> collectObjects = materialLibService.getCollectObjects(MaterialLibId);
        for (MaterialCollectObject collectObject : collectObjects) {
            collectObjectIds.add(collectObject.getSCollectObjectId());
        }

        if (collectObjectIds.size() == 0) {
            updateAmt(MaterialLibId);
            return;
        }

        //删除已移除的采集对象的内容
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MaterialLibId", MaterialLibId);
        queryWrapper.notIn("CollectObjectId", collectObjectIds);
        queryWrapper.eq("bManual", 0);
        remove(queryWrapper);

        for (MaterialCollectObject collectObject : collectObjects) {
            QueryWrapper<Material> materialQueryWrapper = new QueryWrapper<>();
            materialQueryWrapper.eq("MaterialLibId", collectObject.getMaterialLibId());
            materialQueryWrapper.eq("CollectObjectId", collectObject.getSCollectObjectId());

            if (count(materialQueryWrapper) == 0) {//这个采集对象的内容，还未归类到该内容库下
                materialQueryWrapper = new QueryWrapper<>();
                materialQueryWrapper.eq("CollectObjectId", collectObject.getSCollectObjectId());
                materialQueryWrapper.isNull("MaterialLibId");//null表示原始内容

                String sTimeLimit = collectObject.getSTimeLimit();
                if (StrUtil.isNotEmpty(sTimeLimit)) {
                    String[] times = sTimeLimit.split("/");
                    if (times.length > 0) {
                        if (StrUtil.isNotEmpty(times[0])) {
                            materialQueryWrapper.ge("dNewTime", times[0]);
                        }

                        if (times.length == 2 && StrUtil.isNotEmpty(times[1])) {
                            materialQueryWrapper.le("dNewTime", times[1]);
                        }
                    }
                }

                materialQueryWrapper.orderByDesc("dNewTime");
                List<Material> materialList = list(materialQueryWrapper);
                for (int i = 0; i < materialList.size(); i++) {
                    Material material = materialList.get(i);

                    boolean bPass = true;
                    if (StrUtil.isNotEmpty(collectObject.getSKeyWord())) {
                        String[] arrKeyWord = collectObject.getSKeyWord().split(",");
                        for (String sKeyWord : arrKeyWord) {
                            if (material.getSContent().indexOf(sKeyWord) > -1) {
                                bPass = false;
                                break;
                            }
                        }
                    } else {
                        bPass = false;
                    }

                    if (bPass) {//没有匹配到关键字
                        continue;
                    }

                    Material cloneMaterial = null;
                    try {
                        cloneMaterial = material.clone();
                        cloneMaterial.setLId(null);
                        cloneMaterial.setBClassify(1);
                        cloneMaterial.setMaterialLibId(collectObject.getMaterialLibId());
                        cloneMaterial.setQueueId(i == 0 ? 1 : 2);
                        save(cloneMaterial);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        updateAmt(MaterialLibId);
    }

    /**
     * 处理采集对象的数据
     *
     * @param listObjectData
     */
    protected void handleListData(List<Map<String, Object>> listObjectData) {
        ArrayList<String> arrFriendId = new ArrayList<>();
        Map<String, List<String>> mapChatroom = new HashMap<>();
        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);

            String sCollectObjectId = (String) objectData.get("CollectObjectId");
            if (StrUtil.startWith(sCollectObjectId, "/moment/")) {
                arrFriendId.add(sCollectObjectId.substring(sCollectObjectId.lastIndexOf("/") + 1));
            } else if (StrUtil.startWith(sCollectObjectId, "/chatroom/")) {
                String[] split = sCollectObjectId.split("/");

                List<String> listChatMember = mapChatroom.get(split[2]);
                if (listChatMember == null) {
                    listChatMember = new ArrayList<>();
                }
                listChatMember.add(split[3]);

                mapChatroom.put(split[2], listChatMember);
            }
        }

        HashMap<String, Object> mapColectObject = new HashMap<>();
        if (!mapChatroom.isEmpty()) {
            for (String ChatRoomId : mapChatroom.keySet()) {
                QueryWrapper<WeChatRoomMember> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("WeChatRoomId", ChatRoomId);
                queryWrapper.in("sWechatId", mapChatroom.get(ChatRoomId));
                List<WeChatRoomMember> weChatRoomMembers = weChatRoomMemberService.list(queryWrapper);
                for (WeChatRoomMember weChatRoomMember : weChatRoomMembers) {
                    mapColectObject.put("/chatroom/" + ChatRoomId + "/" + weChatRoomMember.getSWechatId(), weChatRoomMember);
                }
            }
        }

        if (arrFriendId.size() > 0) {
            QueryWrapper<WeChatFriend> queryWrapper = new QueryWrapper<>();
            List<WeChatFriend> weChatFriends = weChatFriendService.listByIds(arrFriendId);
            for (WeChatFriend weChatFriend : weChatFriends) {
                mapColectObject.put("/moment/" + weChatFriend.getLId(), weChatFriend);
            }
        }

        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);
            objectData.put("CollectObjectId", mapColectObject.get(objectData.get("CollectObjectId")));
        }
    }

    protected List<JSONObject> getListInlineBtn(Map<String, Object> objectData) {
        List<JSONObject> listBtn = super.getListInlineBtn(objectData);

        Map mapTypeObject = (Map) objectData.get("TypeId");
        if (mapTypeObject != null && ((String) mapTypeObject.get("ID")).equals("5")) {
            JSONObject jsonBtn = new JSONObject();
            jsonBtn.put("ID", "addToMiniPrograme");
            jsonBtn.put("sName", "添加至小程序库");
            jsonBtn.put("handler", "handleaddToMiniPrograme");
            listBtn.add(jsonBtn);
        }

        return listBtn;
    }

    /**
     * 下载视频
     */
    public synchronized void downloadVideo() {
        QueryWrapper<DownloadVideoSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bSend", "0");
        List<DownloadVideoSchedule> downloadVideoSchedules = downloadVideoScheduleService.list(queryWrapper);
        for (DownloadVideoSchedule downloadVideoSchedule : downloadVideoSchedules) {
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("messageId", downloadVideoSchedule.getMessageId());
            mapParam.put("type", downloadVideoSchedule.getSType());
            mapParam.put("tencentUrl", downloadVideoSchedule.getSTencentUrl());
            mapParam.put("wechatAccountId", downloadVideoSchedule.getWechatId());
            customerTouchApi.downloadVideo(mapParam);
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            downloadVideoSchedule.setBSend(1);
            downloadVideoSchedule.setDSendTime(LocalDateTime.now());
            downloadVideoScheduleService.updateById(downloadVideoSchedule);
        }

        //异步拉取带有视频的消息
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bSend", "1");
        queryWrapper.eq("bDownload", "0");
        downloadVideoSchedules = downloadVideoScheduleService.list(queryWrapper);
        for (DownloadVideoSchedule downloadVideoSchedule : downloadVideoSchedules) {
            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("messageId", downloadVideoSchedule.getMessageId());
            JSONArray arrMsg = customerTouchApi.pullChatroomMessage(mapParam);
            for (int i = 0; i < arrMsg.size(); i++) {
                JSONObject msg = arrMsg.getJSONObject(i);

                //提取内容
                String sContent = extractContent(msg.getString("content"));

                try {
                    JSONObject.parseObject(sContent);//还没下载成功的，就是可以正常解析的json格式
                    continue;
                } catch (RuntimeException e) {

                }

                //
                UpdateWrapper<Material> objectUpdateWrapper = new UpdateWrapper<>();
                objectUpdateWrapper.setSql("VideoId='" + saveToAttach(sContent) + "'");
                objectUpdateWrapper.eq("lId", downloadVideoSchedule.getLId())
                        .or()
                        .eq("MaterialId", downloadVideoSchedule.getLId());
                update(objectUpdateWrapper);

                downloadVideoSchedule.setBDownload(1);
                downloadVideoScheduleService.updateById(downloadVideoSchedule);
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //重置超时下载的任务
        UpdateWrapper<DownloadVideoSchedule> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.setSql("bSend='0'");
        objectUpdateWrapper.eq("bSend", "1");
        objectUpdateWrapper.eq("bDownload", "0");
        objectUpdateWrapper.lt("dSendTime", LocalDateTime.now().minusMinutes(10));

        downloadVideoScheduleService.update(objectUpdateWrapper);
    }

    /**
     * 提取内容
     *
     * @return
     */
    private String extractContent(String sContent) {
        sContent = sContent.substring(sContent.indexOf(":") + 1);
        return StrUtil.trim(sContent);
    }

    public void updateAmt(Integer MaterialLibId) {
        QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("MaterialLibId", MaterialLibId);
        int count = count(queryWrapper);

        UpdateWrapper<MaterialLib> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.set("lAmt", count);
        objectUpdateWrapper.eq("lId", MaterialLibId);
        materialLibService.update(objectUpdateWrapper);
    }

    /**
     * 通过关键字过滤内容库里的内容
     *
     * @param id
     */
    public void fliterByKeyword(Integer id) {
        MaterialLib materialLib = materialLibService.getById(id);

        if (StrUtil.isNotEmpty(materialLib.getSKeyWord()) || StrUtil.isNotEmpty(materialLib.getSExclude())) {
            QueryWrapper<Material> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("lId", "sContent");
            queryWrapper.eq("MaterialLibId", id);
            List<Material> materialList = list(queryWrapper);

            List<Integer> listDeleterId = new ArrayList<>();
            for (Material material : materialList) {
                //包含关键字
                boolean bDelete = true;
                if (StrUtil.isNotEmpty(materialLib.getSKeyWord())) {
                    String[] arrKeyWord = materialLib.getSKeyWord().split(",");
                    for (String sKeyWord : arrKeyWord) {
                        if (material.getSContent().indexOf(sKeyWord) > -1) {
                            bDelete = false;
                            break;
                        }
                    }
                }

                if (!bDelete && StrUtil.isNotEmpty(materialLib.getSExclude())) {
                    String[] arrExclude = materialLib.getSExclude().split(",");
                    for (String sExclude : arrExclude) {
                        if (material.getSContent().indexOf(sExclude) > -1) {
                            bDelete = true;
                            break;
                        }
                    }
                }

                if (bDelete) {
                    listDeleterId.add(material.getMaterialId());
                }
            }

            removeByIds(listDeleterId);
        }
    }

    public void reGenerateAi(ListParam listParam) {
        //设置全部出来，不要分页
        listParam.setCanpage(0);

        //只要id字段
        List<String> listDispCol = new ArrayList<>();
        listDispCol.add("lId");
        listParam.setDispcol(listDispCol);

        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        List<Material> materialList = new ArrayList<>();
        for (Map<String, Object> mapObjectData : listMapObjectData) {
            Map<String, Object> data = (Map<String, Object>) mapObjectData.get("data");

            Material material = new Material();
            material.setLId((Integer) data.get("lId"));
            material.setBAIGenerated(2);

            materialList.add(material);
        }

        updateBatchById(materialList);
    }
}
