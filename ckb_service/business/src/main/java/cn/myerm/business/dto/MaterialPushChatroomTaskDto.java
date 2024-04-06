package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MaterialPushChatroomTaskDto {

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

    @JSONField(name = "lPushDelay")
    private Integer lPushDelay;

    @JSONField(name = "sPeriod")
    private String sPeriod;

    @JSONField(name = "lInterval")
    private Integer lInterval;

    @JSONField(name = "MaterialLibIds")
    private String MaterialLibIds;

    @JSONField(name = "PushWechatroomIds")
    private String PushWechatroomIds;

    @JSONField(name = "JdSocialMediaId")
    private Long JdSocialMediaId;

    @JSONField(name = "JdPromotionSiteId")
    private Long JdPromotionSiteId;

    @JSONField(name = "TypeId")
    private Integer TypeId;

    @JSONField(name = "bLoop")
    private Integer bLoop;

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

    @JSONField(name = "bImmediately")
    private Integer bImmediately;
}
