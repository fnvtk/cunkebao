package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class FriendRequestParam {
    /**
     * 加好友的打招呼信息
     */
    private String message;

    /**
     * 好友手机号
     */
    private String phone;

    /**
     * 好友微信ID，手机号和微信ID不能同时为空
     */
    private String wechatid;

    /**
     * 通过哪个微信号去加好友
     */
    private String accountid;

    /**
     * 打标签
     */
    private String label;

    /**
     * 好友备注
     */
    private String remark;
}
