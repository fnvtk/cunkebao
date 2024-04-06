package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class FriendListParam {

    /**
     * 是否通过好友。true已是好友，false待通过好友
     */
    private Boolean ispass;

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

    private Integer preFriendId;

    private String wechatAccountKeyword;
    private String friendKeyword;
    private String friendPhoneKeyword;
}
