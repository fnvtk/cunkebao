package cn.myerm.alertcenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ErrorTask")
public class ErrorTask {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("sDetail")
    private String sDetail;

    @TableField("sErrorCode")
    private String sErrorCode;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("LevelId")
    private Integer LevelId;

    @TableField("ChargerUserId")
    private Integer ChargerUserId;

    @TableField("ChargerUpUserId")
    private Integer ChargerUpUserId;

    @TableField("dResponseTime")
    private LocalDateTime dResponseTime;

    @TableField("dDoneTime")
    private LocalDateTime dDoneTime;

    @TableField("dLastNotifyTime")
    private LocalDateTime dLastNotifyTime;

    @TableField("fTimeUse")
    private Float fTimeUse;

    @TableField("StatusId")
    private String StatusId;

    @TableField("sRawMsg")
    private String sRawMsg;

}
