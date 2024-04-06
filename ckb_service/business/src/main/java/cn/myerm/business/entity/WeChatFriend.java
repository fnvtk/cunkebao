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
@TableName("WeChatFriend")
public class WeChatFriend extends BusinessEntity {

    @TableId(value = "lId")
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("WeChatId")
    private Integer WeChatId;

    @TableField("sWechatId")
    private String sWechatId;

    @TableField("sAlias")
    private String sAlias;

    @TableField("sPhone")
    private String sPhone;

    @TableField("sConRemark")
    private String sConRemark;

    @TableField("sPyInitial")
    private String sPyInitial;

    @TableField("sQuanPin")
    private String sQuanPin;

    @TableField("sAvatar")
    private String sAvatar;

    @TableField("GenderId")
    private Integer GenderId;

    @TableField("sRegion")
    private String sRegion;

    @TableField("sCountry")
    private String sCountry;

    @TableField("sProvince")
    private String sProvince;

    @TableField("sCity")
    private String sCity;

    @TableField("lAddFrom")
    private Integer lAddFrom;

    @TableField("sSignature")
    private String sSignature;

    @TableField("sLabels")
    private String sLabels;

    @TableField("dCreateTime")
    private LocalDateTime dCreateTime;

    @TableField("dDeleteTime")
    private LocalDateTime dDeleteTime;

    @TableField("bDeleted")
    private Integer bDeleted;

    @TableField("bPassed")
    private Integer bPassed;

}
