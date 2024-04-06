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
@TableName("FriendRequestTaskScan")
public class FriendRequestTaskScan extends BusinessEntity {

    @TableField("lId")
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

}
