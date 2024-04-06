package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendRequestTaskDetailDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "NewUserId")
    private Integer NewUserId;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @JSONField(name = "sPhoneOrWechatId")
    private String sPhoneOrWechatId;

    @JSONField(name = "StatusId")
    private Integer StatusId;

    @JSONField(name = "dSendTime")
    private LocalDateTime dSendTime;

    @JSONField(name = "WeChatId")
    private Integer WeChatId;

    @JSONField(name = "WeChatFriendId")
    private Integer WeChatFriendId;

    @JSONField(name = "ThirdTaskId")
    private Integer ThirdTaskId;

    @JSONField(name = "dPassedTime")
    private LocalDateTime dPassedTime;

    @JSONField(name = "dFailedTime")
    private LocalDateTime dFailedTime;

    @JSONField(name = "sFailMessage")
    private String sFailMessage;

    @JSONField(name = "dSuccessTime")
    private LocalDateTime dSuccessTime;

}
