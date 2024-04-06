package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AccountDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "UpId")
    private Integer UpId;

    @JSONField(name = "NewUserId")
    private Integer NewUserId;

    @JSONField(name = "EditUserId")
    private Integer EditUserId;

    @JSONField(name = "OwnerId")
    private Integer OwnerId;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "dEditTime")
    private LocalDateTime dEditTime;

    @JSONField(name = "bActive")
    private Integer bActive;

}
