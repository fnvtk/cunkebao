package cn.myerm.customertouch.wechat.service.impl;

import cn.myerm.customertouch.wechat.entity.WeChatMessage;
import cn.myerm.customertouch.wechat.mapper.WeChatMessageMapper;
import cn.myerm.customertouch.wechat.service.IWeChatMessageService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class WeChatMessageServiceImpl extends ServiceImpl<WeChatMessageMapper, WeChatMessage> implements IWeChatMessageService {

    /**
     * 处理消息
     * @param jsonMessage JSONObject
     */
    public void handleMsg(String sType, JSONObject jsonMessage) {
        WeChatMessage weChatMessage = new WeChatMessage();
        weChatMessage.setId(jsonMessage.getIntValue("id"));
        weChatMessage.setType(sType);
        weChatMessage.setWechatAccountId(jsonMessage.getIntValue("wechatAccountId"));
        weChatMessage.setWechatChatroomId(jsonMessage.getIntValue("wechatChatroomId"));
        weChatMessage.setSender(jsonMessage.getString("sender"));
        weChatMessage.setContent(jsonMessage.getString("content"));
        weChatMessage.setMsgType(jsonMessage.getIntValue("msgType"));
        weChatMessage.setMsgSubType(jsonMessage.getIntValue("msgSubType"));
        weChatMessage.setIsSend(jsonMessage.getBoolean("isSend") ? 1 : 0);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        LocalDateTime createTime = LocalDateTime.parse(jsonMessage.getString("createTime"), formatter);
        weChatMessage.setCreateTime(createTime);

        weChatMessage.setIsDeleted(jsonMessage.getBoolean("isDeleted") ? 1 : 0);
        weChatMessage.setSendStatus(jsonMessage.getIntValue("sendStatus"));
        weChatMessage.setWechatTime(jsonMessage.getLongValue("wechatTime"));
        weChatMessage.setOrigin(jsonMessage.getIntValue("origin"));
        weChatMessage.setMsgId(jsonMessage.getIntValue("msgId"));
        weChatMessage.setRecalled(jsonMessage.getBooleanValue("recalled") ? 1 : 0);
        saveOrUpdate(weChatMessage);
    }
}
