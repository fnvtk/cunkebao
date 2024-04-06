package cn.myerm.customertouch.wechat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("WeChat")
public class WeChat {
    @TableId(value = "lId")
    private Integer lId;

    @TableField(value = "lPerDayAmt")
    private Integer lPerDayAmt;

    @TableField(value = "lPerDayAmtDefault")
    private Integer lPerDayAmtDefault;

    @TableField("dLastFriendRequestFrequentlyTime")
    private LocalDateTime dLastFriendRequestFrequentlyTime;

    @TableField("dLastFriendRequestTime")
    private LocalDateTime dLastFriendRequestTime;
}
