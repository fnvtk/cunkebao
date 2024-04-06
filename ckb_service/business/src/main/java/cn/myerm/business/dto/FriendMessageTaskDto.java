package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class FriendMessageTaskDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @JSONField(name = "FriendRequestTaskDetailId")
    private Integer FriendRequestTaskDetailId;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "dSendTime")
    private LocalDateTime dSendTime;

    @JSONField(name = "dSendTimePlan")
    private LocalDateTime dSendTimePlan;

    @JSONField(name = "TypeId")
    private Integer TypeId;

    @JSONField(name = "WeChatId")
    private Integer WeChatId;

    @JSONField(name = "WeChatFriendId")
    private Integer WeChatFriendId;

    @JSONField(name = "jMessage")
    private String jMessage;

    @JSONField(name = "StatusId")
    private Integer StatusId;

}
