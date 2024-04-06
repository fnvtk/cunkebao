package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("DouYin")
public class DouYin extends BusinessEntity {

    @TableId(value = "lId")
    private BigInteger lId;

    @TableField("sName")
    private String sName;

    @TableField("AccountId")
    private Integer AccountId;

    @TableField("sAvatar")
    private String sAvatar;

    @TableField("sRaw")
    private String sRaw;

}
