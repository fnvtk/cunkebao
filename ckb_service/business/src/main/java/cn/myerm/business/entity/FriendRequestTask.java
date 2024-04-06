package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("FriendRequestTask")
public class FriendRequestTask extends BusinessEntity {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("AccountId")
    private Integer AccountId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("dEditTime")
    private LocalDateTime dEditTime;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("EditUserId")
    private Integer EditUserId;

    @TableField("TypeId")
    private String TypeId;

    @TableField("dLastExecTime")
    private LocalDateTime dLastExecTime;

    @TableField("dNextExecTime")
    private LocalDateTime dNextExecTime;

    @TableField("WeChatId")
    private String WeChatId;

    @TableField("PosterId")
    private Integer PosterId;

    @TableField("MemoTypeId")
    private Integer MemoTypeId;

    @TableField("IncomeTypeId")
    private Integer IncomeTypeId;

    @TableField("fIncome")
    private BigDecimal fIncome;

    @TableField("MemoParamId")
    private String MemoParamId;

    @TableField("sKey")
    private String sKey;

    @TableField("sHello")
    private String sHello;

    @TableField("sTheme")
    private String sTheme;

    @TableField("sTip")
    private String sTip;

    @TableField("jTask")
    private String jTask;

    @TableField("sPeriod")
    private String sPeriod;

    @TableField("lRequestInterval")
    private Integer lRequestInterval;

    @TableField("lMaxPerDayRequestAmt")
    private Integer lMaxPerDayRequestAmt;

    @TableField("lTargetAmt")
    private Integer lTargetAmt;

    @TableField("lNoExecAmt")
    private Integer lNoExecAmt;

    @TableField("lSuccessAmt")
    private Integer lSuccessAmt;

    @TableField("lPassAmt")
    private Integer lPassAmt;

    @TableField("lScanAmt")
    private Integer lScanAmt;

    @TableField("bEnable")
    private Integer bEnable;
}
