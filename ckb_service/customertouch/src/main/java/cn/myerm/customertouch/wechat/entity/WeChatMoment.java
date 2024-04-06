package cn.myerm.customertouch.wechat.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("WeChatMoment")
public class WeChatMoment {
    @TableId(value = "Id")
    private String Id;

    @TableField("wechatAccountId")
    private Integer wechatAccountId;

    @TableField("wechatFriendId")
    private Integer wechatFriendId;

    @TableField("snsId")
    private String snsId;

    @TableField("type")
    private Integer type;

    @TableField("commentList")
    private String commentList;

    @TableField("likeList")
    private String likeList;

    @TableField("createTime")
    private Integer createTime;

    @TableField("collectTime")
    private LocalDateTime collectTime;

    @TableField("content")
    private String content;

    @TableField("coverImage")
    private String coverImage;

    @TableField("title")
    private String title;

    @TableField("lat")
    private String lat;

    @TableField("lng")
    private String lng;

    @TableField("location")
    private String location;

    @TableField("urls")
    private String urls;

    @TableField("urlsorig")
    private String urlsorig;

    @TableField("userName")
    private String userName;

    @TableField("status")
    private Integer status;

    @TableField("trytime")
    private Integer trytime;

    @TableField(value = "sendTime", updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime sendTime;
}
