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
@TableName("WeChatFriendLabelLib")
public class WeChatFriendLabelLib extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @TableField("UpId")
    private Integer UpId;

    @TableField("ValueTypeId")
    private String ValueTypeId;

    @TableField("jListValue")
    private String jListValue;

    @TableField("bSys")
    private Integer bSys;

    @TableField("bLeaf")
    private Integer bLeaf;

}
