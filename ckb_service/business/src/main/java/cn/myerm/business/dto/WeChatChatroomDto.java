package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class WeChatChatroomDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "AccountWechatId")
    private Integer AccountWechatId;

    @JSONField(name = "sWechatAccountId")
    private String sWechatAccountId;

    @JSONField(name = "sAvatar")
    private String sAvatar;

    @JSONField(name = "sChatroomId")
    private String sChatroomId;

    @JSONField(name = "dCreateTime")
    private LocalDateTime dCreateTime;

    @JSONField(name = "dDeleteTime")
    private LocalDateTime dDeleteTime;

    @JSONField(name = "lUserNum")
    private Integer lUserNum;

}
