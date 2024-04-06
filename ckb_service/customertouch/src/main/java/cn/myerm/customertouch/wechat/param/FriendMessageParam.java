package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class FriendMessageParam {

    /**
     * 搜索的聊天内容
     */
    private String keyword;

    /**
     * 发送内容的类型
     */
    private int msgtype;

    /**
     * 通过哪个微信号发送消息
     */
    private int accountid;

    /**
     * 接收者的ID
     */
    private int friendid;

    /**
     * 查询结果数量
     */
    private int count;

    /**
     * 起始时间，Unix时间戳
     */
    private Long from;

    /**
     * 结束时间，Unix时间戳
     */
    private Long to;

    /**
     * 是否更早的消息
     */
    private Boolean olderdata;
}
