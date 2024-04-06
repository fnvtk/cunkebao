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
@TableName("WeChatRoomMember")
public class WeChatRoomMember extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("WeChatRoomId")
    private Integer WeChatRoomId;

    @TableField("sWechatId")
    private String sWechatId;

    @TableField("sAvatar")
    private String sAvatar;

    @TableField("bAdmin")
    private Integer bAdmin;

    @TableField("bDeleted")
    private Integer bDeleted;

    @TableField("dDeletedDate")
    private LocalDateTime dDeletedDate;

}
