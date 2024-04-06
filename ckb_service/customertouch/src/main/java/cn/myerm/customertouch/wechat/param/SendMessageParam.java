package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class SendMessageParam {

    /**
     * 发送内容
     */
    private String content;

    /**
     * 发送内容的类型
     */
    private String msgtype;

    /**
     * 通过哪个微信号发送消息
     */
    private String accountid;

    /**
     * 接收者的ID
     */
    private String receiver;
}
