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
@TableName("FriendRequestConfig")
public class FriendRequestConfig extends BusinessEntity {

    @TableField("sId")
    private String sId;

    @TableField("sName")
    private String sName;

    @TableField("sUUId")
    private String sUUId;

    @TableField("FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @TableField("TypeId")
    private Integer TypeId;

    @TableField("jCond")
    private String jCond;

    @TableField("jEffectDate")
    private String jEffectDate;

    @TableField("jEffectTime")
    private String jEffectTime;

    @TableField("jMessage")
    private String jMessage;

    @TableField("bEnable")
    private Integer bEnable;
}
