package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MaterialPushMomentTaskDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "NewUserId")
    private Integer NewUserId;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "lPushAmtPerDay")
    private Integer lPushAmtPerDay;

    @JSONField(name = "sPeriod")
    private String sPeriod;

    @JSONField(name = "lInterval")
    private Integer lInterval;

    @JSONField(name = "MaterialLibIds")
    private String MaterialLibIds;

    @JSONField(name = "PushWechatIds")
    private String PushWechatIds;

    @JSONField(name = "bLoop")
    private Integer bLoop;

    @JSONField(name = "TypeId")
    private Integer TypeId;

    @JSONField(name = "lPushQueueAmt")
    private Integer lPushQueueAmt;

    @JSONField(name = "lRound")
    private Integer lRound;

    @JSONField(name = "dLastPushTime")
    private LocalDateTime dLastPushTime;

    @JSONField(name = "bDel")
    private Integer bDel;

    @JSONField(name = "bEnable")
    private Integer bEnable;
}
