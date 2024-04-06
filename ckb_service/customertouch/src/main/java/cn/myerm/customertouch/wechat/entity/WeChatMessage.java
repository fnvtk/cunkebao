package cn.myerm.customertouch.wechat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("WeChatMessage")
public class WeChatMessage {
    @TableId(value = "id")
    private Integer id;

    @TableField(value = "type")
    private String type;

    @TableField(value = "wechatAccountId")
    private Integer wechatAccountId;

    @TableField(value = "wechatChatroomId")
    private Integer wechatChatroomId;

    @TableField(value = "wechatFriendId")
    private Integer wechatFriendId;

    @TableField(value = "sender")
    private String sender;

    @TableField(value = "content")
    private String content;

    @TableField(value = "msgType")
    private Integer msgType;

    @TableField(value = "msgSubType")
    private Integer msgSubType;

    @TableField(value = "isSend")
    private Integer isSend;

    @TableField(value = "createTime")
    private LocalDateTime createTime;

    @TableField(value = "isDeleted")
    private Integer isDeleted;

    @TableField(value = "sendStatus")
    private Integer sendStatus;

    @TableField(value = "wechatTime")
    private Long wechatTime;

    @TableField(value = "origin")
    private Integer origin;

    @TableField(value = "msgId")
    private Integer msgId;

    @TableField(value = "recalled")
    private Integer recalled;
}
