package cn.myerm.business.param;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FriendReqTaskParam {
    private Integer lId;
    private String sName;
    private String sTip;
    private String sTheme;
    private String sHello;
    private String sPeriod;
    private String TypeId;
    private Integer DouYinId;
    private Integer lRequestInterval;
    private Integer lMaxPerDayRequestAmt;
    private Boolean bEnable;
    private Integer MemoTypeId;
    private Integer PosterId;
    private String arrWechatId;
    private String MemoParamId;
    private String arrTag;
    private String arrTask;
    private String test;
    private Integer IncomeTypeId;
    private BigDecimal fIncome;
}
