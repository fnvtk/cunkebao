package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class ChatroomListParam {

    /**
     * 关键词
     */
    private String keyword;

    /**
     * 归属客服
     */
    private Integer accountid;

    /**
     * 页码
     */
    private int page;

    /**
     * 每页多少条数据
     */
    private int pagesize;
}
