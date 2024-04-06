package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class AccountListParam {

    /**
     * 是否在线
     */
    private Boolean isalive;

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 页码
     */
    private int page;

    /**
     * 每页多少条数据
     */
    private int pagesize;
}
