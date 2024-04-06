package cn.myerm.customertouch.wechat.controller;

import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.exception.SystemException;
import cn.myerm.customertouch.wechat.param.*;
import cn.myerm.customertouch.wechat.handler.WechatHandle;
import cn.myerm.customertouch.wechat.service.impl.WeChatMomentCollectCronServiceImpl;
import cn.myerm.customertouch.wechat.service.impl.WeChatMomentServiceImpl;
import cn.myerm.customertouch.wechat.service.impl.WeChatRoomCollectCronServiceImpl;
import cn.myerm.customertouch.wechat.service.impl.WeChatServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/customertouch/wechat")
public class WechatController extends CommonController {

    private final WechatHandle wechatHandle;

    private final WeChatServiceImpl weChatService;

    private final WeChatMomentServiceImpl weChatMomentService;

    private final WeChatMomentCollectCronServiceImpl weChatMomentCollectCronService;

    private final WeChatRoomCollectCronServiceImpl weChatRoomCollectCronService;

    @Autowired
    public WechatController(WechatHandle wechatHandle, WeChatServiceImpl weChatService, WeChatMomentServiceImpl weChatMomentService, WeChatMomentCollectCronServiceImpl weChatMomentCollectCronService, WeChatRoomCollectCronServiceImpl weChatRoomCollectCronService) {
        this.wechatHandle = wechatHandle;
        this.weChatService = weChatService;
        this.weChatMomentService = weChatMomentService;
        this.weChatMomentCollectCronService = weChatMomentCollectCronService;
        this.weChatRoomCollectCronService = weChatRoomCollectCronService;
    }

    /**
     * 加好友请求
     *
     * @param friendRequestParam
     * @return MessageDTO
     */
    @PostMapping("/addfriend")
    public MessageDTO sendFriendRequest(FriendRequestParam friendRequestParam) {
        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("Message", friendRequestParam.getMessage());
        mapParam.put("Phone", friendRequestParam.getPhone());
        mapParam.put("TargetWechatId", friendRequestParam.getWechatid());
        mapParam.put("WechatAccountId", friendRequestParam.getAccountid());
        mapParam.put("Labels", friendRequestParam.getLabel());
        mapParam.put("Remark", friendRequestParam.getRemark());

        try {
            wechatHandle.sendFriendRequest(mapParam);
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }

        return success();
    }

    /**
     * 私发消息
     *
     * @param messageParam
     * @return MessageDTO
     */
    @PostMapping("/sendmessage/tofriend")
    public MessageDTO sendMessageToFriend(SendMessageParam messageParam) {

        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("content", messageParam.getContent());
        mapParam.put("msgType", messageParam.getMsgtype());
        mapParam.put("wechatAccountId", messageParam.getAccountid());
        mapParam.put("wechatFriendId", messageParam.getReceiver());
        mapParam.put("wechatChatroomId", "0");

        try {
            wechatHandle.sendMessageToFriend(mapParam);
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }

        return success();
    }

    /**
     * 发送群消息
     *
     * @param messageParam
     * @return MessageDTO
     */
    @PostMapping("/sendmessage/tochatroom")
    public MessageDTO sendMessageToChatroom(SendMessageParam messageParam) {

        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("content", messageParam.getContent());
        mapParam.put("msgType", messageParam.getMsgtype());
        mapParam.put("wechatAccountId", messageParam.getAccountid());
        mapParam.put("wechatChatroomId", messageParam.getReceiver());
        mapParam.put("wechatFriendId", "0");

        try {
            wechatHandle.sendMessageToChatroom(mapParam);
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }

        return success();
    }


    /**
     * 好友聊天记录
     *
     * @param friendMessageParam
     * @return
     */
    @PostMapping("/friendmessage/search")
    public MessageDTO friendMessage(FriendMessageParam friendMessageParam) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("keyword", friendMessageParam.getKeyword());
        mapParam.put("msgType", friendMessageParam.getMsgtype());
        mapParam.put("count", friendMessageParam.getCount());
        mapParam.put("wechatAccountId", friendMessageParam.getAccountid());
        mapParam.put("wechatFriendId", friendMessageParam.getFriendid());
        mapParam.put("from", friendMessageParam.getFrom());
        mapParam.put("to", friendMessageParam.getTo());
        mapParam.put("olderData", friendMessageParam.getOlderdata().toString());

        try {
            return success(wechatHandle.searchFriendMessage(mapParam));
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    /**
     * 微信群聊天记录
     *
     * @param chatroomMessageParam
     * @return
     */
    @PostMapping("/chatroommessage/search")
    public MessageDTO chatroomMessage(ChatroomMessageParam chatroomMessageParam) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("keyword", chatroomMessageParam.getKeyword());
        mapParam.put("msgType", chatroomMessageParam.getMsgtype());
        mapParam.put("count", chatroomMessageParam.getCount());
        mapParam.put("wechatAccountId", chatroomMessageParam.getAccountid());
        mapParam.put("wechatChatroomId", chatroomMessageParam.getChatroomid());
        mapParam.put("messageId", chatroomMessageParam.getMessageId());
        mapParam.put("wechatId", chatroomMessageParam.getWechatId());
        mapParam.put("from", chatroomMessageParam.getFrom());
        mapParam.put("to", chatroomMessageParam.getTo());
        mapParam.put("olderData", chatroomMessageParam.getOlderdata());

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("results", wechatHandle.searchChatroomMessage(mapParam));

            return success(jsonObject);
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    /**
     * 微信列表
     *
     * @param accountListParam
     * @return MessageDTO
     */
    @PostMapping("/account/list")
    public MessageDTO accountList(AccountListParam accountListParam) {
        Map<String, Object> mapParam = new HashMap<>();

        mapParam.put("keyword", accountListParam.getKeyword());
        mapParam.put("wechatAlive", accountListParam.getIsalive() + "");
        mapParam.put("pageIndex", accountListParam.getPage() + "");
        mapParam.put("pageSize", accountListParam.getPagesize() + "");

        try {
            return success(wechatHandle.accountList(mapParam));
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    /**
     * 检测微信号是否可以发起好友申请
     * @param wechatId
     * @return
     */
    @PostMapping("/account/canreq")
    public MessageDTO accountCanReq(Integer wechatId) {
        WeChatUsageLogParam weChatUsageLogParam = new WeChatUsageLogParam();
        weChatUsageLogParam.setWeChatId(wechatId);
        weChatUsageLogParam.setTypeId(1);
        weChatUsageLogParam.setDNewTime(LocalDateTime.now());

        try {
            weChatService.checkValid(weChatUsageLogParam);
            return success();
        } catch (SystemException e) {
            return fail(e.getMessage(), 11000);
        }
    }


    @PostMapping("/friend/list")
    public MessageDTO friendList(FriendListParam friendListParam) {
        JSONObject mapParam = new JSONObject();

        mapParam.put("keyword", friendListParam.getKeyword());
        mapParam.put("allotAccountId", friendListParam.getAccountid());
        mapParam.put("isPass", friendListParam.getIspass());
        mapParam.put("pageIndex", friendListParam.getPage());
        mapParam.put("pageSize", friendListParam.getPagesize());
        mapParam.put("preFriendId", friendListParam.getPreFriendId());
        mapParam.put("wechatAccountKeyword", friendListParam.getWechatAccountKeyword());
        mapParam.put("friendKeyword", friendListParam.getFriendKeyword());
        mapParam.put("friendPhoneKeyword", friendListParam.getFriendPhoneKeyword());

        try {
            return success(wechatHandle.friendList(mapParam));
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    @PostMapping("/friend/count")
    public MessageDTO friendCount(FriendListParam friendListParam) {
        JSONObject mapParam = new JSONObject();

        mapParam.put("keyword", friendListParam.getKeyword());
        mapParam.put("allotAccountId", friendListParam.getAccountid() + "");
        mapParam.put("isPass", friendListParam.getIspass() + "");
        mapParam.put("pageIndex", friendListParam.getPage() + "");
        mapParam.put("pageSize", friendListParam.getPagesize() + "");

        try {
            return success(wechatHandle.friendCount(mapParam));
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    @PostMapping("/friend/add/list")
    public MessageDTO addFriendByPhoneTaskList(FriendAddByPhoneTaskListParam param) {
        JSONObject mapParam = new JSONObject();

        mapParam.put("keyword", param.getKeyword());
        mapParam.put("status", param.getStatus());
        mapParam.put("pageIndex", param.getPageIndex());
        mapParam.put("pageSize", param.getPageSize());

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("results", wechatHandle.addFriendByPhoneTaskList(mapParam));

            return success(jsonObject);
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    @PostMapping("/friend/add")
    public MessageDTO friendAdd(FriendAddParam friendAddParam) {
        JSONObject mapParam = new JSONObject();
        mapParam.put("message", friendAddParam.getMessage());
        mapParam.put("phone", friendAddParam.getPhone());
        mapParam.put("remark", friendAddParam.getRemark());
        mapParam.put("wechatAccountId", friendAddParam.getWechatAccountId());
        mapParam.put("labels", JSONArray.parseArray(friendAddParam.getLabels()));

        wechatHandle.friendAdd(mapParam);
        return success();
    }

    @PostMapping("/chatroom/list")
    public MessageDTO chatRoomList(ChatroomListParam chatroomListParam) {
        Map<String, Object> mapParam = new HashMap<>();

        mapParam.put("keyword", chatroomListParam.getKeyword());
        mapParam.put("allotAccountId", chatroomListParam.getAccountid());
        mapParam.put("pageIndex", chatroomListParam.getPage());
        mapParam.put("pageSize", chatroomListParam.getPagesize());

        try {
            return success(wechatHandle.chatRoomList(mapParam));
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    @PostMapping("/chatroom/listChatroomMember")
    public MessageDTO listChatroomMember(Integer wechatChatroomId) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("results", wechatHandle.listChatroomMember(wechatChatroomId));

            return success(jsonObject);
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }
    }

    @PostMapping("/chatroom/invite")
    public MessageDTO chatRoomInvite(ChatroomInviteParam chatroomInviteParam) {
        JSONObject mapParam = new JSONObject();

        mapParam.put("wechatChatroomId", chatroomInviteParam.getWechatChatroomId());
        mapParam.put("wechatFriendIds", JSONArray.parseArray(chatroomInviteParam.getWechatFriendIds()));

        try {
            wechatHandle.chatRoomInvite(mapParam);
        } catch (SystemException e) {
            return fail(e.getMessage(), e.getErrorCode().intValue());
        }

        return success();
    }

    @PostMapping("/moment/list")
    public MessageDTO momentList(MomentListParam momentListParam) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", weChatMomentService.list(momentListParam));
        return success(jsonObject);
    }

    @PostMapping("/moment/publish")
    public MessageDTO momentPublist(MomentPublishParam momentPublishParam) {
        wechatHandle.publishMoment(momentPublishParam);
        return success();
    }

    @PostMapping("/moment/publishlist")
    public MessageDTO momentPublishList(MomentPublishListParam momentPublishListParam) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", wechatHandle.publishMomentList(momentPublishListParam));
        return success(jsonObject);
    }

    @PostMapping("/moment/getdetail")
    public MessageDTO momentGetDetail(Integer id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", wechatHandle.getMomentDetail(id));
        return success(jsonObject);
    }

    /**
     * 分配客服
     * @param wechatFriendIds
     * @return
     */
    @PostMapping("/friend/multiAllocFriendToAccount")
    public MessageDTO multiAllocFriendToAccount(@RequestParam(name = "wechatFriendIds", required = true) String wechatFriendIds) {
        wechatHandle.multiAllocFriendToAccount(wechatFriendIds);
        return success();
    }

    /**
     * 采集对象的推送
     */
    @PostMapping("/collectobject/push")
    public MessageDTO collectObjectPush(@RequestParam(name = "objects", required = true) String objects) {
        JSONArray jsonArray = JSONArray.parseArray(objects);
        List<String> listFriendId = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            if (jsonObject.getString("type").equals("moment")) {
                weChatMomentCollectCronService.push(jsonObject);
                listFriendId.add(jsonObject.getString("friendid"));
            } else if (jsonObject.getString("type").equals("chatroom")) {
                weChatRoomCollectCronService.push(jsonObject);
            }
        }

        weChatMomentCollectCronService.disable(listFriendId);

        return success();
    }




    /**
     * 下载消息的视频
     * @param downloadVideoParam
     * @return
     */
    @PostMapping("/message/downloadvideo")
    public MessageDTO downloadVideo(DownloadVideoParam downloadVideoParam) {
        wechatHandle.downloadVideo(downloadVideoParam);
        return success();
    }

    @GetMapping("/unlock")
    public MessageDTO unlock() {
        wechatHandle.unlock();
        return success();
    }

    @GetMapping("/clear")
    public MessageDTO clear() {
        wechatHandle.clear();
        return success();
    }
}
