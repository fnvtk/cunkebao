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
@TableName("MaterialPushChatroomTask")
public class MaterialPushChatroomTask extends BusinessEntity implements IMaterialPushTask {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("lPushAmtPerDay")
    private Integer lPushAmtPerDay;

    @TableField("lPushDelay")
    private Integer lPushDelay;

    @TableField("sPeriod")
    private String sPeriod;

    @TableField("lInterval")
    private Integer lInterval;

    @TableField("MaterialLibIds")
    private String MaterialLibIds;

    @TableField("PushWechatroomIds")
    private String PushWechatroomIds;

    @TableField(exist = false)
    private Long JdSocialMediaId;

    @TableField("JdPromotionSiteId")
    private Long JdPromotionSiteId;

    @TableField("sPushSchedule")
    private String sPushSchedule;

    @TableField(exist = false)
    private String PushWechatIds;

    @TableField("TypeId")
    private Integer TypeId;

    @TableField("bLoop")
    private Integer bLoop;

    @TableField("bImmediately")
    private Integer bImmediately;

    @TableField("lPushQueueAmt")
    private Integer lPushQueueAmt;

    @TableField("lRound")
    private Integer lRound;

    @TableField("dLastPushTime")
    private LocalDateTime dLastPushTime;

    @TableField("bDel")
    private Integer bDel;

    @TableField("bEnable")
    private Integer bEnable;

    @TableField(exist = false)
    private String sType = "chatroom";
}
