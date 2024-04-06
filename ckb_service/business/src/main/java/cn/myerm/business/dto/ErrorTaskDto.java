package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class ErrorTaskDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "sErrorCode")
    private String sErrorCode;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "LevelId")
    private Integer LevelId;

    @JSONField(name = "ChargerUserId")
    private Integer ChargerUserId;

    @JSONField(name = "ChargerUpUserId")
    private Integer ChargerUpUserId;

    @JSONField(name = "dResponseTime")
    private LocalDateTime dResponseTime;

    @JSONField(name = "dDoneTime")
    private LocalDateTime dDoneTime;

    @JSONField(name = "dLastNotifyTime")
    private LocalDateTime dLastNotifyTime;

    @JSONField(name = "fTimeUse")
    private Float fTimeUse;

    @JSONField(name = "StatusId")
    private String StatusId;

    @JSONField(name = "sRawMsg")
    private String sRawMsg;

}
