package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("UserIncome")
public class UserIncome extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("fIncome")
    private BigDecimal fIncome;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("ObjectId")
    private Integer ObjectId;

}
