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
@TableName("MaterialPushLog")
public class MaterialPushLog extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("dTimingTime")
    private LocalDateTime dTimingTime;

    @TableField("dNewMaterialTime")
    private LocalDateTime dNewMaterialTime;

    @TableField("TypeId")
    private String TypeId;

    @TableField("TaskId")
    private Integer TaskId;

    @TableField("MaterialId")
    private Integer MaterialId;

    @TableField("WeChatIds")
    private String WeChatIds;

    @TableField("ChatRoomIds")
    private String ChatRoomIds;

    @TableField("RemoteId")
    private Integer RemoteId;

    @TableField("sMd5")
    private String sMd5;

    @TableField("sMd5Json")
    private String sMd5Json;

    @TableField("sContent")
    private String sContent;

    @TableField("sPushScheduleTime")
    private String sPushScheduleTime;

    @TableField("lRound")
    private Integer lRound;

    @TableField("StatusId")
    private Integer StatusId;

}
