package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class UserIncomeDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "NewUserId")
    private Integer NewUserId;

    @JSONField(name = "fIncome")
    private BigDecimal fIncome;

    @JSONField(name = "sObjectName")
    private String sObjectName;

    @JSONField(name = "ObjectId")
    private Integer ObjectId;

}
