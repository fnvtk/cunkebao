package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class MomentPublishListParam {
    private int pageSize;
    private String from;
    private String to;
    private int pageIndex;
}
