package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class WeChatRoomMemberDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "WeChatRoomId")
    private Integer WeChatRoomId;

    @JSONField(name = "sWechatId")
    private String sWechatId;

    @JSONField(name = "sAvatar")
    private String sAvatar;

    @JSONField(name = "bAdmin")
    private String bAdmin;

    @JSONField(name = "bDeleted")
    private String bDeleted;

    @JSONField(name = "dDeletedDate")
    private LocalDateTime dDeletedDate;

}
