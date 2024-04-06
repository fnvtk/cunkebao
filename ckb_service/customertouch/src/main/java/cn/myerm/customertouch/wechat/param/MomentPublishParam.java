package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class MomentPublishParam {
    private String beginTime;
    private String endTime;
    private String text;
    private Integer momentContentType;
    private String picUrlList;
    private String link;
    private String videoUrl;
    private String jobPublishWechatMomentsItems;
}
