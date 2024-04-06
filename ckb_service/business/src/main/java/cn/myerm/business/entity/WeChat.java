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
@TableName("WeChat")
public class WeChat extends BusinessEntity {

    @TableId(value = "lId")
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("AccountId")
    private String AccountId;

    @TableField("sWechatId")
    private String sWechatId;

    @TableField("sAlias")
    private String sAlias;

    @TableField("bOnline")
    private Integer bOnline;

    @TableField("sAvatar")
    private String sAvatar;

    @TableField("sMobile")
    private String sMobile;

    @TableField("lTotalFriend")
    private Integer lTotalFriend;

    @TableField("dLastFriendRequestFrequentlyTime")
    private LocalDateTime dLastFriendRequestFrequentlyTime;

    @TableField("dLimitTime")
    private LocalDateTime dLimitTime;

}
