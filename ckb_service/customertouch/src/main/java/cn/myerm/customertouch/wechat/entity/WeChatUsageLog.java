package cn.myerm.customertouch.wechat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("WeChatUsageLog")
public class WeChatUsageLog {
    @TableField("WeChatId")
    private Integer WeChatId;

    @TableField("TypeId")
    private Integer TypeId;

    @TableField("TargetId")
    private String TargetId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;
}
