package cn.myerm.objectbuilder.entity;

import cn.hutool.core.util.StrUtil;
import cn.myerm.common.entity.CommonEntity;
import cn.myerm.common.exception.SystemException;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
@TableName("SysField")
public class SysField extends CommonEntity implements Cloneable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("sFieldAs")
    private String sFieldAs;

    @TableField("sTip")
    private String sTip;

    @TableField("bPrimaryKey")
    private Integer bPrimaryKey;

    @TableField("sPrimartKeyType")
    private String sPrimartKeyType;

    @TableField("sDataType")
    private String sDataType;

    @TableField("sUIType")
    private String sUIType;

    @TableField("RefSysFieldId")
    private Integer RefSysFieldId;

    @TableField("sRefFieldAs")
    private String sRefFieldAs;

    @TableField("sRefKey")
    private String sRefKey;

    @TableField("sRefIdFieldAs")
    private String sRefIdFieldAs;

    @TableField("sRefNameFieldAs")
    private String sRefNameFieldAs;

    @TableField("sEnumOption")
    private String sEnumOption;

    @TableField("bSumField")
    private Boolean bSumField;

    @TableField("lLength")
    private Integer lLength;

    @TableField("lDeciLength")
    private Integer lDeciLength;

    @TableField("lWidth")
    private Integer lWidth;

    @TableField("bRequired")
    private Boolean bRequired;

    @TableField("bReadOnly")
    private Boolean bReadOnly;

    @TableField("bDisabled")
    private Boolean bDisabled;

    @TableField("bEnableRTE")
    private Boolean bEnableRTE;

    @TableField("bMulti")
    private Boolean bMulti;

    @TableField("sDefaultValue")
    private String sDefaultValue;

    @TableField("sParamJson")
    private String sParamJson;

    @TableField(exist = false)
    private String sUITypeTxt;

    @TableField(exist = false)
    private int lIndex;

    @TableField("sAlign")
    private String sAlign;

    /**
     * 引用的属性
     */
    @TableField(exist = false)
    private SysField referenceField;

    /**
     * 引用的源属性
     */
    @TableField(exist = false)
    private SysField referenceSourceField;

    /**
     * 列表型的选项
     */
    @TableField(exist = false)
    private List<Map<String, Object>> arrEnumOption;

    public String getOptionValue(String sKey) {
        if (!StrUtil.hasEmpty(sEnumOption)) {
            Properties prop = new Properties();
            try {
                prop.load(new StringReader(sEnumOption));
                return prop.getProperty(sKey);
            } catch (IOException e) {
                throw new SystemException(e.getMessage());
            }
        } else {
            return null;
        }
    }

    @Override
    public SysField clone() throws CloneNotSupportedException {
        return (SysField) super.clone();
    }
}

