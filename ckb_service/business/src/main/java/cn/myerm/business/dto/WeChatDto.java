package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class WeChatDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "AccountId")
    private String AccountId;

    @JSONField(name = "sWechatId")
    private String sWechatId;

    @JSONField(name = "sAlias")
    private String sAlias;

    @JSONField(name = "bOnline")
    private Integer bOnline;

    @JSONField(name = "sAvatar")
    private String sAvatar;

    @JSONField(name = "sMobile")
    private String sMobile;
}
