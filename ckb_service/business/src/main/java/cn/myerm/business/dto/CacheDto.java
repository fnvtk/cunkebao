package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class CacheDto {

    @JSONField(name = "sKey")
    private String sKey;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "sValue")
    private String sValue;

}
