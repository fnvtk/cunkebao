package cn.myerm.objectbuilder.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class FieldParams {
    private Integer id;
    @NotBlank(message = "对象名不能为空")
    private String sobjectname;
    @NotBlank(message = "属性名称不能为空")
    private String name;
    @NotBlank(message = "字段名不能为空")
    private String field;
    @NotBlank(message = "属性类型不能为空")
    private String type;
    private String uitype;
    private String tip;
    private Boolean required;
    private String defaultvalue;
    private Integer length;
    private Integer width;
    private Integer decilength;
    private Boolean enablerte;
    private String refkey;
    private String enumoption;
    private String refobj;
    private Integer refobjfieldid;
    private Integer reffield;
    private Boolean readonly;
    private Boolean disabled;
    private Boolean isautoincrement;
    private Boolean sumfield;
    private Boolean multi;
    private Boolean isPk;
    private Boolean isattach;
}
