package cn.myerm.business.dto;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class WeChatFriendDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "WeChatId")
    private Integer WeChatId;

    @JSONField(name = "sWechatId")
    private String sWechatId;

    @JSONField(name = "sAlias")
    private String sAlias;

    @JSONField(name = "sPhone")
    private String sPhone;

    @JSONField(name = "sConRemark")
    private String sConRemark;

    @JSONField(name = "sPyInitial")
    private String sPyInitial;

    @JSONField(name = "sQuanPin")
    private String sQuanPin;

    @JSONField(name = "sAvatar")
    private String sAvatar;

    @JSONField(name = "GenderId")
    private Integer GenderId;

    @JSONField(name = "sCountry")
    private String sCountry;

    @JSONField(name = "sProvince")
    private String sProvince;

    @JSONField(name = "sCity")
    private String sCity;

    @JSONField(name = "lAddFrom")
    private Integer lAddFrom;

    @JSONField(name = "sSignature")
    private String sSignature;

    @JSONField(name = "sLabels")
    private String sLabels;

    @JSONField(name = "dCreateTime")
    private LocalDateTime dCreateTime;

    @JSONField(name = "dDeleteTime")
    private LocalDateTime dDeleteTime;

    @JSONField(name = "bDeleted")
    private Integer bDeleted;

    @JSONField(name = "bPassed")
    private Integer bPassed;

    @JSONField(name = "WeChat")
    private Object WeChat;
}
