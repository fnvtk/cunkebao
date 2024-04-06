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
@TableName("FriendMessageTask")
public class FriendMessageTask extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("sUUId")
    private String sUUId;

    @TableField("FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @TableField("FriendRequestTaskDetailId")
    private Integer FriendRequestTaskDetailId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("dSendTimePlan")
    private LocalDateTime dSendTimePlan;

    @TableField("dSendTime")
    private LocalDateTime dSendTime;

    @TableField("TypeId")
    private Integer TypeId;

    @TableField("WeChatId")
    private Integer WeChatId;

    @TableField("WeChatFriendId")
    private Integer WeChatFriendId;

    @TableField("jMessage")
    private String jMessage;

    @TableField("StatusId")
    private Integer StatusId;

}
