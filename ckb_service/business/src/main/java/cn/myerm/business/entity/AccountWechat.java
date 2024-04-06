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
@TableName("AccountWechat")
public class AccountWechat extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("AccountId")
    private Integer AccountId;

    @TableField("WeChatId")
    private Integer WeChatId;

}
