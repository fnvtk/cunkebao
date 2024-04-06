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
@TableName("Account")
public class Account extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("UpId")
    private Integer UpId;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("EditUserId")
    private Integer EditUserId;

    @TableField("OwnerId")
    private Integer OwnerId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("dEditTime")
    private LocalDateTime dEditTime;

    @TableField("bActive")
    private Integer bActive;

    @TableField("bDel")
    private Integer bDel;

    @TableField("sManagerName")
    private String sManagerName;

    @TableField("sManagerMobile")
    private String sManagerMobile;

    @TableField("ManagerUserId")
    private Integer ManagerUserId;

    @TableField("fBalance")
    private Float fBalance;
}
