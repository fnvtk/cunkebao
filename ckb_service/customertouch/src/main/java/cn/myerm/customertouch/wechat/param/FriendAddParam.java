package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class FriendAddParam {
    /**
     * 加好友的打招呼信息
     */
    private String message;

    /**
     * 好友手机号
     */
    private String phone;

    private String remark;

    private String wechatAccountId;

    /**
     * 打标签
     */
    private String labels;
}
