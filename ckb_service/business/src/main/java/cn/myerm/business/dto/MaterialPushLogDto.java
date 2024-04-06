package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class MaterialPushLogDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "dTimingTime")
    private LocalDateTime dTimingTime;

    @JSONField(name = "dNewMaterialTime")
    private LocalDateTime dNewMaterialTime;

    @JSONField(name = "TypeId")
    private Integer TypeId;

    @JSONField(name = "TaskId")
    private Integer TaskId;

    @JSONField(name = "MaterialId")
    private Integer MaterialId;

    @JSONField(name = "WeChatIds")
    private String WeChatIds;

    @JSONField(name = "RemoteId")
    private Integer RemoteId;

    @JSONField(name = "sMd5")
    private String sMd5;

    @JSONField(name = "sMd5Json")
    private String sMd5Json;

    @JSONField(name = "sPushScheduleTime")
    private String sPushScheduleTime;

    @JSONField(name = "lRound")
    private String lRound;

    @JSONField(name = "StatusId")
    private Integer StatusId;

}
