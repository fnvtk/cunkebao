package cn.myerm.business.param;

import lombok.Data;

@Data
public class MomentTaskParam {
    private Integer lId;
    private String sName;
    private String sPeriod;
    private Integer lPushAmtPerDay;
    private Integer TypeId;
    private Boolean bEnable;
    private String arrWechatId;
    private String arrMaterialLibId;
}