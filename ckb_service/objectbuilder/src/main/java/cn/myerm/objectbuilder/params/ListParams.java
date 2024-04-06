package cn.myerm.objectbuilder.params;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ListParams {
    private Integer id;
    private String sname;
    private String sobjectname;
    private String typeid;
    private String swheresql;
    private String sordersql;
    private String sgroupbysql;
    private Boolean bactive;
    private Boolean bdefault;
    private Boolean bcanpage;
    private Boolean bcanbat;
    private Boolean bsingle;
    private Integer lpagelimit;
    private String slistselectid;
    private String sadvancedsearchid;
    private String sfastsearchid;
    private String spermissionjson;
}
