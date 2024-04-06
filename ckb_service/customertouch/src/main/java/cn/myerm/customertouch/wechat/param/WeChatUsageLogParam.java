package cn.myerm.customertouch.wechat.param;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WeChatUsageLogParam {
    private Integer WeChatId;
    private Integer TypeId;
    private String TargetId;
    private LocalDateTime dNewTime;
}
