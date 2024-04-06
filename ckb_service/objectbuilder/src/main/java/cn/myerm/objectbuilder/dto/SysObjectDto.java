package cn.myerm.objectbuilder.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SysObjectDto {
    private String sObjectName;
    private String sName;
    private String ParentId;
    private String sParentIdFieldAs;
    private String sDbTable;
    private String SysModuleId;
    private String SysDataSourceId;
    private Boolean bWorkFlow;
    private String sIdFieldAs;
    private String sNameFieldAs;
    private List<Map<String,Object>> sOperatorJson;
    private String DataPowerTypeId;
    private String sNote;
}
