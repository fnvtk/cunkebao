package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("Event")
public class Event {
    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("EventTriggerId")
    private String EventTriggerId;

    @TableField("jParam")
    private String jParam;

    @TableField("sSourcePath")
    private String sSourcePath;
}
