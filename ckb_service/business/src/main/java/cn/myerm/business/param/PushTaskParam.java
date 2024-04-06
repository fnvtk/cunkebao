package cn.myerm.business.param;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PushTaskParam {
    Integer TaskId;
    Integer lPushedAmt;
    LocalDateTime dLastPushTime;
}
