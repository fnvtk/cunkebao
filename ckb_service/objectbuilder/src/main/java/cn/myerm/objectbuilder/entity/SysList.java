package cn.myerm.objectbuilder.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2021-04-09
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysList")
public class SysList extends CommonEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("TypeId")
    private String TypeId;

    @TableField("sWhereSql")
    private String sWhereSql;

    @TableField("sOrderBySql")
    private String sOrderBySql;

    @TableField("sGroupBySql")
    private String sGroupBySql;

    @TableField("bActive")
    private Boolean bActive;

    @TableField("bDefault")
    private Boolean bDefault;

    @TableField("bCanPage")
    private Boolean bCanPage;

    @TableField("bShowOpera")
    private Boolean bShowOpera;

    @TableField("bCanBat")
    private Boolean bCanBat;

    @TableField("bSingle")
    private Boolean bSingle;

    @TableField("lPageLimit")
    private Integer lPageLimit;

    @TableField("sListSelectId")
    private String sListSelectId;

    @TableField("sAdvancedSearchId")
    private String sAdvancedSearchId;

    @TableField("sFastSearchId")
    private String sFastSearchId;

    @TableField("sPermissionJson")
    private String sPermissionJson;

    @TableField("lOperaColumnWidth")
    private Integer lOperaColumnWidth;

    @TableField("lHeight")
    private Integer lHeight;
}

