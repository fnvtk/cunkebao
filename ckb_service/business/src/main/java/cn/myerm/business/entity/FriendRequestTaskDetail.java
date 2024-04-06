package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("FriendRequestTaskDetail")
public class FriendRequestTaskDetail extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @TableField("sPhoneOrWechatId")
    private String sPhoneOrWechatId;

    @TableField("StatusId")
    private Integer StatusId;

    @TableField("dSendTime")
    private LocalDateTime dSendTime;

    @TableField("WeChatId")
    private Integer WeChatId;

    @TableField("WeChatFriendId")
    private Integer WeChatFriendId;

    @TableField("ThirdTaskId")
    private Integer ThirdTaskId;

    @TableField("dPassedTime")
    private LocalDateTime dPassedTime;

    @TableField("dFailedTime")
    private LocalDateTime dFailedTime;

    @TableField("sFailMessage")
    private String sFailMessage;

    @TableField("dSuccessTime")
    private LocalDateTime dSuccessTime;

}
