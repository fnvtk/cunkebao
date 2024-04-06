package cn.myerm.business.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FriendRequestTaskDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "AccountId")
    private Integer AccountId;

    @JSONField(name = "IncomeTypeId")
    private Integer IncomeTypeId;

    @JSONField(name = "fIncome")
    private BigDecimal fIncome;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "dEditTime")
    private LocalDateTime dEditTime;

    @JSONField(name = "NewUserId")
    private Integer NewUserId;

    @JSONField(name = "EditUserId")
    private Integer EditUserId;

    @JSONField(name = "TypeId")
    private String TypeId;

    @JSONField(name = "dLastExecTime")
    private LocalDateTime dLastExecTime;

    @JSONField(name = "dNextExecTime")
    private LocalDateTime dNextExecTime;

    @JSONField(name = "arrWechatId")
    private List<String> arrWechatId;

    @JSONField(name = "PosterId")
    private Integer PosterId;

    @JSONField(name = "MemoTypeId")
    private Integer MemoTypeId;

    @JSONField(name = "MemoParamId")
    private List<String> MemoParamId;

    @JSONField(name = "sPeriod")
    private String sPeriod;

    @JSONField(name = "sHello")
    private String sHello;

    @JSONField(name = "sTheme")
    private String sTheme;

    @JSONField(name = "sTip")
    private String sTip;

    @JSONField(name = "jTask")
    private String jTask;

    @JSONField(name = "arrTask")
    private JSONArray arrTask;

    @JSONField(name = "lRequestInterval")
    private Integer lRequestInterval;

    @JSONField(name = "lMaxPerDayRequestAmt")
    private Integer lMaxPerDayRequestAmt;

    @JSONField(name = "lTargetAmt")
    private Integer lTargetAmt;

    @JSONField(name = "lNoExecAmt")
    private Integer lNoExecAmt;

    @JSONField(name = "lSuccessAmt")
    private Integer lSuccessAmt;

    @JSONField(name = "bEnable")
    private Integer bEnable;

}
