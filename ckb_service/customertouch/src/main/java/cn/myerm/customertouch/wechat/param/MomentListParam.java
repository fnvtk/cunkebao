package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class MomentListParam {

    /**
     * 发布时间
     */
    private String starttime;

    /**
     * 每页的条数
     */
    private int pagesize;

    /**
     * 页码
     */
    private int page;
}
