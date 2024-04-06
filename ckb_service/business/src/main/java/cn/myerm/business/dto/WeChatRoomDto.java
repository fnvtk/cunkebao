package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class WeChatRoomDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "WechatId")
    private Integer WechatId;

    @JSONField(name = "sAvatar")
    private String sAvatar;

    @JSONField(name = "sOwnerNickName")
    private String sOwnerNickName;

    @JSONField(name = "sOwnerAvatar")
    private String sOwnerAvatar;

    @JSONField(name = "sChatroomId")
    private String sChatroomId;

    @JSONField(name = "dCreateTime")
    private LocalDateTime dCreateTime;

    @JSONField(name = "dDeleteTime")
    private LocalDateTime dDeleteTime;

    @JSONField(name = "lUserNum")
    private Integer lUserNum;

    @JSONField(name = "bDeleted")
    private Integer bDeleted;

    @JSONField(name = "WeChat")
    private Object WeChat;
}
