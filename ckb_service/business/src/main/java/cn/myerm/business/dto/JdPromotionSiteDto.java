package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class JdPromotionSiteDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "JdSocialMediaId")
    private Integer JdSocialMediaId;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

}
