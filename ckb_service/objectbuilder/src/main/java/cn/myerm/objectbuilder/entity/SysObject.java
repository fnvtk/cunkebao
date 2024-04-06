package cn.myerm.objectbuilder.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonProperty;
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
 * @since 2021-04-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysObject")
public class SysObject extends CommonEntity {

    private static final long serialVersionUID = 1L;

    @TableId("sObjectName")
    private String sObjectName;

    @TableField("sName")
    private String sName;

    @TableField("ParentId")
    @JSONField(name = "ParentId")
    private String ParentId;

    @TableField("sParentIdFieldAs")
    private String sParentIdFieldAs;

    @TableField("sDbTable")
    private String sDbTable;

    @TableField("SysModuleId")
    @JSONField(name = "SysModuleId")
    private String SysModuleId;

    @TableField("SysDataSourceId")
    @JSONField(name = "SysDataSourceId")
    private String SysDataSourceId;

    @TableField("bWorkFlow")
    private Boolean bWorkFlow;

    @TableField("sIdFieldAs")
    private String sIdFieldAs;

    @TableField("sNameFieldAs")
    private String sNameFieldAs;

    @TableField("sOperatorJson")
    private String sOperatorJson;

    @TableField("DataPowerTypeId")
    @JSONField(name = "DataPowerTypeId")
    private String DataPowerTypeId;

    @TableField("sNote")
    private String sNote;


}

