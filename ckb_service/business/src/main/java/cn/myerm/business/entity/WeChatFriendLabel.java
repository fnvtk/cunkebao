package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("WeChatFriendLabel")
public class WeChatFriendLabel extends BusinessEntity {
    @TableField("FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @TableField("FriendRequestTaskDetailId")
    private Integer FriendRequestTaskDetailId;

    @TableField("WeChatFriendId")
    private Integer WeChatFriendId;

    @TableField("LabelId")
    private Integer LabelId;

    @TableField("sValue")
    private String sValue;
}
