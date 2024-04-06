package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class DownloadVideoScheduleDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "sType")
    private String sType;

    @JSONField(name = "WechatId")
    private Integer WechatId;

    @JSONField(name = "ChatroomId")
    private Integer ChatroomId;

    @JSONField(name = "MessageId")
    private Integer MessageId;

    @JSONField(name = "sTencentUrl")
    private String sTencentUrl;

    @JSONField(name = "dSendTime")
    private LocalDateTime dSendTime;

    @JSONField(name = "bSend")
    private Integer bSend;

    @JSONField(name = "bDownload")
    private Integer bDownload;

}
