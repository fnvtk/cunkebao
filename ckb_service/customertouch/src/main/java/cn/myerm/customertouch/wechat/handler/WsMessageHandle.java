package cn.myerm.customertouch.wechat.handler;

import cn.myerm.customertouch.wechat.entity.WeChatMoment;
import cn.myerm.customertouch.wechat.entity.WeChatMomentCollectCron;
import cn.myerm.customertouch.wechat.service.impl.WeChatMessageServiceImpl;
import cn.myerm.customertouch.wechat.service.impl.WeChatMomentCollectCronServiceImpl;
import cn.myerm.customertouch.wechat.service.impl.WeChatMomentServiceImpl;
import cn.myerm.customertouch.wechat.service.impl.WeChatRoomCollectCronServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Component
public class WsMessageHandle {

    private static final Logger LOGGER = LoggerFactory.getLogger(WsMessageHandle.class);

    private final WeChatMomentServiceImpl weChatMomentService;
    private final WeChatMomentCollectCronServiceImpl weChatMomentCollectCronService;
    private final WeChatMessageServiceImpl weChatMessageService;
    private final WeChatRoomCollectCronServiceImpl weChatRoomCollectCronService;

    @Autowired
    public WsMessageHandle(WeChatMomentServiceImpl weChatMomentService, WeChatMomentCollectCronServiceImpl weChatMomentCollectCronService, WeChatMessageServiceImpl weChatMessageService, WeChatRoomCollectCronServiceImpl weChatRoomCollectCronService) {
        this.weChatMomentService = weChatMomentService;
        this.weChatMomentCollectCronService = weChatMomentCollectCronService;
        this.weChatMessageService = weChatMessageService;
        this.weChatRoomCollectCronService = weChatRoomCollectCronService;
    }

    /**
     * 执行消息处理
     *
     * @param jsonMsg JSONObject
     */
    public void exec(JSONObject jsonMsg) {
        String sCmdType = jsonMsg.getString("cmdType");
        switch (sCmdType) {
            //朋友圈采集结果
            case "CmdFetchMomentResult":

                ArrayList<WeChatMoment> listMoment = new ArrayList<>();

                JSONArray arrResult = jsonMsg.getJSONArray("result");
                WeChatMomentCollectCron collectCfg = weChatMomentCollectCronService.getById(jsonMsg.getIntValue("wechatAccountId") + "-" + jsonMsg.getIntValue("wechatFriendId"));
                for (int i = 0; i < arrResult.size(); i++) {
                    JSONObject jsonMoment = arrResult.getJSONObject(i);

                    WeChatMoment weChatMoment = weChatMomentService.getById(jsonMsg.getString("wechatAccountId") + jsonMoment.getString("snsId"));
                    if (weChatMoment == null) {
                        weChatMoment = new WeChatMoment();
                    } else {
                        if (weChatMoment.getStatus() >= 2) {
                            continue;
                        }
                    }

                    JSONObject jsonEntity = jsonMoment.getJSONObject("momentEntity");

                    if (collectCfg.getBVerifyUserName() == 1 && !collectCfg.getSUserName().contains(jsonEntity.getString("userName"))) {
                        continue;//因为经常采集的不是这个号的朋友圈内容，所以加用户名的判断
                    }

                    weChatMoment.setId(jsonMsg.getString("wechatAccountId") + jsonMoment.getString("snsId"));
                    weChatMoment.setWechatAccountId(jsonMsg.getIntValue("wechatAccountId"));
                    weChatMoment.setWechatFriendId(jsonMsg.getIntValue("wechatFriendId"));
                    weChatMoment.setCommentList(jsonMoment.getString("commentList"));
                    weChatMoment.setLikeList(jsonMoment.getString("likeList"));
                    weChatMoment.setType(jsonMoment.getIntValue("type"));

                    weChatMoment.setSnsId(jsonEntity.getString("snsId"));
                    weChatMoment.setCreateTime(jsonEntity.getIntValue("createTime"));
                    weChatMoment.setContent(jsonEntity.getString("content"));
                    weChatMoment.setCoverImage(jsonEntity.getString("coverImage"));
                    weChatMoment.setTitle(jsonEntity.getString("title"));
                    weChatMoment.setLat(jsonEntity.getString("lat"));
                    weChatMoment.setLng(jsonEntity.getString("lng"));
                    weChatMoment.setLocation(jsonEntity.getString("location"));
                    weChatMoment.setUrlsorig(jsonEntity.getString("urls"));
                    weChatMoment.setUserName(jsonEntity.getString("userName"));
                    weChatMoment.setCollectTime(LocalDateTime.now());
                    weChatMoment.setTrytime(0);

                    if (weChatMoment.getType() == 1) {//图文
                        if (weChatMoment.getStatus() == null) {
                            weChatMoment.setStatus(0);
                        }
                    } else if (weChatMoment.getType() == 15) {//视频暂时还不支持
                        weChatMoment.setStatus(3);
                    } else {
                        if (weChatMoment.getType() == 3) {
                            weChatMoment.setUrls(jsonEntity.getString("urls"));
                        }
                        weChatMoment.setStatus(2);
                    }

                    if (weChatMoment.getUrls() == null && weChatMoment.getType() != 1) {
                        weChatMoment.setUrls(jsonEntity.getString("urls"));
                    }

                    listMoment.add(weChatMoment);
                }

                weChatMomentService.saveOrUpdateBatch(listMoment);

                WeChatMomentCollectCron weChatMomentCollectCron = weChatMomentCollectCronService.getById(jsonMsg.getString("wechatAccountId") + "-" + jsonMsg.getString("wechatFriendId"));

                if (weChatMomentCollectCron == null) {
                    weChatMomentCollectCron = new WeChatMomentCollectCron();
                }

                weChatMomentCollectCron.setSId(jsonMsg.getString("wechatAccountId") + "-" + jsonMsg.getString("wechatFriendId"));


                if (arrResult.size() == 0) {//采集不到，说明全部采集完了
                    weChatMomentCollectCron.setBFullCollected(1);
                }

                //是否已全部集采完了，如果是，以后只采集第一页数据
                if (weChatMomentCollectCron.getBFullCollected().equals(1)) {
                    weChatMomentCollectCron.setSPrevSnsId("0");
                } else {
                    weChatMomentCollectCron.setSPrevSnsId(arrResult.getJSONObject(arrResult.size() - 1).getString("snsId"));//设置最后一条，为了下一页朋友圈采集做准备
                }

                //如果不是全量采集
                if (weChatMomentCollectCron.getBFull().equals(0)) {
                    weChatMomentCollectCron.setSPrevSnsId("0");
                    weChatMomentCollectCron.setBFullCollected(1);
                }

                weChatMomentCollectCron.setLTryTime(0);
                weChatMomentCollectCronService.updateById(weChatMomentCollectCron);
                break;
            case "CmdDownloadMomentImagesResult":
                WeChatMoment weChatMoment = weChatMomentService.getById(jsonMsg.getString("wechatAccountId") + jsonMsg.getString("snsId"));

                JSONArray urls = JSONArray.parseArray(jsonMsg.getString("urls"));
                JSONArray urlsorig = JSONArray.parseArray(weChatMoment.getUrlsorig());
                if (urls.size() < urlsorig.size() && weChatMoment.getTrytime() < 2) {
                    weChatMoment.setStatus(0);
                    weChatMoment.setSendTime(null);
                    weChatMoment.setUrls(jsonMsg.getString("urls"));
                    weChatMomentService.updateById(weChatMoment);

                    break;//图片采集经常会漏，所以要重新采集图片
                } else {
                    weChatMoment.setUrls(jsonMsg.getString("urls"));
                    weChatMoment.setStatus(2);
                    weChatMoment.setSendTime(null);
                    weChatMoment.setTrytime(0);
                    weChatMoment.setCollectTime(LocalDateTime.now());

                    weChatMomentService.updateById(weChatMoment);
                    break;
                }
                /*
            case "CmdNewMessage":
                JSONObject chatroomMessage = jsonMsg.getJSONObject("chatroomMessage");
                if (chatroomMessage != null) {//群消息
                    JSONObject sender = chatroomMessage.getJSONObject("sender");
                    String sId = chatroomMessage.getString("wechatChatroomId") + "-" + sender.getString("wechatId");

                    WeChatRoomCollectCron weChatRoomCollectCron = weChatRoomCollectCronService.getById(sId);
                    if (weChatRoomCollectCron != null && weChatRoomCollectCron.getBEnable().equals(1)) {//有启用采集
                        weChatMessageService.handleMsg("chatroomMessage", chatroomMessage);
                    }
                }

                break;*/
            default:
                break;
        }
    }
}
