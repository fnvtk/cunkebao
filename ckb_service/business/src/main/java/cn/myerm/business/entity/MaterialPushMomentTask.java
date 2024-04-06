package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("MaterialPushMomentTask")
public class MaterialPushMomentTask extends BusinessEntity implements IMaterialPushTask {

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

    @TableField(exist = false)
    private Integer lPushDelay = 0;

    @TableField("sPeriod")
    private String sPeriod;

    @TableField("lInterval")
    private Integer lInterval;

    @TableField("MaterialLibIds")
    private String MaterialLibIds;

    @TableField("PushWechatIds")
    private String PushWechatIds;

    @TableField(exist = false)
    private String PushWechatroomIds;

    @TableField(exist = false)
    private Long JdPromotionSiteId;

    @TableField("bLoop")
    private Integer bLoop;

    @TableField("sPushSchedule")
    private String sPushSchedule;

    @TableField("TypeId")
    private Integer TypeId;

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
    private String sType = "moment";

    @TableField(exist = false)
    private Integer bImmediately = 0;
}
