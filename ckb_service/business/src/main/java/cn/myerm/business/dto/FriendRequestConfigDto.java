package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class FriendRequestConfigDto {

    @JSONField(name = "sId")
    private String sId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @JSONField(name = "TypeId")
    private Integer TypeId;

    @JSONField(name = "jCond")
    private String jCond;

    @JSONField(name = "jEffectDate")
    private String jEffectDate;

    @JSONField(name = "jEffectTime")
    private String jEffectTime;

    @JSONField(name = "jMessage")
    private String jMessage;
}
