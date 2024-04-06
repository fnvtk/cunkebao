package cn.myerm.objectbuilder.dto;

import lombok.Data;

@Data
public class SysFieldDto {
    private Boolean bDisabled;
    private Boolean bEnableRTE;
    private Boolean bMulti;
    private Boolean bPrimaryKey;
    private Boolean bReadOnly;
    private Boolean bRequired;
    private Boolean bSumField;
    private Boolean lDeciLength;
    private Integer lID;
    private Integer lLength;
    private Boolean refSysFieldId;
    private Integer referenceField;
    private String sDataType;
    private String sDefaultValue;
    private String sEnumOption;
    private String sFieldAs;
    private String sName;
    private String sObjectName;
    private String sParamJson;
    private String sPrimartKeyType;
    private String sRefFieldAs;
    private String sRefIdFieldAs;
    private String sRefKey;
    private String sRefNameFieldAs;
    private String sTip;
    private String sUIType;
    private String sUITypeTxt;
}
