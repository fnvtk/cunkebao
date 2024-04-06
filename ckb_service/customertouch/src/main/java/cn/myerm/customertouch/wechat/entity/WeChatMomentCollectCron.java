package cn.myerm.customertouch.wechat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("WeChatMomentCollectCron")
public class WeChatMomentCollectCron {
    @TableId(value = "sId")
    private String sId;

    @TableField("WeChatId")
    private Integer WeChatId;

    @TableField("WeChatFriendId")
    private Integer WeChatFriendId;

    @TableField("sUserName")
    private String sUserName;

    @TableField("lTryTime")
    private Integer lTryTime;

    @TableField("sPrevSnsId")
    private String sPrevSnsId;

    @TableField("sCollectInterval")
    private String sCollectInterval;

    @TableField("sCollectPeriod")
    private String sCollectPeriod;

    @TableField("dNextCollectTime")
    private LocalDateTime dNextCollectTime;

    @TableField("dLastCollectTime")
    private LocalDateTime dLastCollectTime;

    @TableField("bEnable")
    private Integer bEnable;

    @TableField("bFull")
    private Integer bFull;

    @TableField("bFullCollected")
    private Integer bFullCollected;

    @TableField("bVerifyUserName")
    private Integer bVerifyUserName;
}
