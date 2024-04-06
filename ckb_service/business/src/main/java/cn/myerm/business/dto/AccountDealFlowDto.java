package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class AccountDealFlowDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "AccountId")
    private Integer AccountId;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "fBalance")
    private BigDecimal fBalance;

    @JSONField(name = "sObjectName")
    private String sObjectName;

    @JSONField(name = "ObjectId")
    private String ObjectId;

}
