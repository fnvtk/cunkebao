package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import cn.myerm.business.entity.BusinessEntity;
import cn.myerm.common.entity.CommonEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author Mars
 * @since 2021-04-19
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysOperatorPermission")
public class SysOperatorPermission extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("TypeId")
    private String TypeId;

    @TableField("ObjectId")
    private String ObjectId;

    @TableField("sOperator")
    private String sOperator;


}
