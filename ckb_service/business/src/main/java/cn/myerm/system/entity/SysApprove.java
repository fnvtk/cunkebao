package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import cn.myerm.business.entity.BusinessEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysApprove")
public class SysApprove extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("LastApproveUserId")
    private String LastApproveUserId;

    @TableField("CurrApproveUserId")
    private String CurrApproveUserId;

    @TableField("lCurrIndex")
    private Integer lCurrIndex;

    @TableField("dLastApproved")
    private LocalDateTime dLastApproved;

    @TableField("StatusId")
    private String StatusId;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("ObjectId")
    private String ObjectId;
}
