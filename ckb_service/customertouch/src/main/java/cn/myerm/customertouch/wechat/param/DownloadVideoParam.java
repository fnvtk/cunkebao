package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class DownloadVideoParam {
    private String messageId;
    private String type;
    private String tencentUrl;
    private String receive;
    private Integer wechatAccountId;
}
