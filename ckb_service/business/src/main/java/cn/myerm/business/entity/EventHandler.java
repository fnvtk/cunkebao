package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("EventHandler")
public class EventHandler {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField(value = "EventId")
    private Integer EventId;

    @TableField("EventHandlerId")
    private String EventHandlerId;

    @TableField("dDoneTime")
    private LocalDateTime dDoneTime;

    @TableField("StatusId")
    private Integer StatusId;

    @TableField("sFailReason")
    private String sFailReason;
}
