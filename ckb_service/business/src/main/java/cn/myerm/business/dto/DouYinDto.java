package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class DouYinDto {

    @JSONField(name = "lId")
    private BigInteger lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "AccountId")
    private Integer AccountId;

    @JSONField(name = "sAvatar")
    private String sAvatar;

    @JSONField(name = "sRaw")
    private String sRaw;
}
