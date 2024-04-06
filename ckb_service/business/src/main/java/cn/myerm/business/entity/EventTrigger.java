package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("EventTrigger")
public class EventTrigger {

    @TableId(value = "sId")
    private String sId;

    @TableField("sName")
    private String sName;

    @TableField("jEventHandler")
    private String jEventHandler;
}
