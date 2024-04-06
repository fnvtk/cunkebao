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
@TableName("WeChatRoom")
public class WeChatRoom extends BusinessEntity {

    @TableId("lId")
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("WechatId")
    private Integer WechatId;

    @TableField("sAvatar")
    private String sAvatar;

    @TableField("sOwnerNickName")
    private String sOwnerNickName;

    @TableField("sOwnerAvatar")
    private String sOwnerAvatar;

    @TableField("sChatroomId")
    private String sChatroomId;

    @TableField("dCreateTime")
    private LocalDateTime dCreateTime;

    @TableField("dDeleteTime")
    private LocalDateTime dDeleteTime;

    @TableField("lUserNum")
    private Integer lUserNum;

    @TableField("bDeleted")
    private Integer bDeleted;

}
