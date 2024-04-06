package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class ChatroomMessageParam {

    /**
     * 搜索的聊天内容
     */
    private String keyword;

    /**
     * 发送内容的类型
     */
    private Integer msgtype;

    /**
     * 通过哪个微信号发送消息
     */
    private Integer accountid;

    /**
     * 微信群ID
     */
    private Integer chatroomid;

    /**
     * 信息的ID
     */
    private Integer messageId;

    /**
     * 群成员的微信ID
     */
    private String wechatId;

    /**
     * 查询结果数量
     */
    private Integer count;

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
