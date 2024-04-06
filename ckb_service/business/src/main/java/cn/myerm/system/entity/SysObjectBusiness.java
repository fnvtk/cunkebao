package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import cn.myerm.business.entity.BusinessEntity;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("SysObject")
public class SysObjectBusiness extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId("sObjectName")
    private String sObjectName;

    @TableField("sName")
    private String sName;

    @TableField("ParentId")
    private String ParentId;

    @TableField("sParentIdFieldAs")
    private String sParentIdFieldAs;

    @TableField("sDbTable")
    private String sDbTable;

    @TableField("SysModuleId")
    private String SysModuleId;

    @TableField("SysDataSourceId")
    private String SysDataSourceId;

    @TableField("bWorkFlow")
    private Integer bWorkFlow;

    @TableField("sIdFieldAs")
    private String sIdFieldAs;

    @TableField("sNameFieldAs")
    private String sNameFieldAs;

    @TableField("sOperatorJson")
    private String sOperatorJson;

    @TableField("DataPowerTypeId")
    private String DataPowerTypeId;

    @TableField("sNote")
    private String sNote;


}
