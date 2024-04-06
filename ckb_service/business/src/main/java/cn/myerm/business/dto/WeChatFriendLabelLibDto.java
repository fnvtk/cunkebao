package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class WeChatFriendLabelLibDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "FriendRequestTaskId")
    private Integer FriendRequestTaskId;

    @JSONField(name = "UpId")
    private Integer UpId;

    @JSONField(name = "ValueTypeId")
    private String ValueTypeId;

    @JSONField(name = "jListValue")
    private String jListValue;

    @JSONField(name = "bSys")
    private Integer bSys;

    @JSONField(name = "bLeaf")
    private Integer bLeaf;

}
