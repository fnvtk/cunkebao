package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class PosterDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "NewUserId")
    private Integer NewUserId;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "sThumb")
    private Integer sThumb;

    @JSONField(name = "sUrl")
    private String sUrl;
}
