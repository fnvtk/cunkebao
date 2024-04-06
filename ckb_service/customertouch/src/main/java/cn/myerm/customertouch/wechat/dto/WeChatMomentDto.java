package cn.myerm.customertouch.wechat.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class WeChatMomentDto {
    @TableId(value = "Id")
    private String Id;

    @JSONField(name = "wechatAccountId")
    private Integer wechatAccountId;

    @JSONField(name = "wechatFriendId")
    private Integer wechatFriendId;

    @JSONField(name = "snsId")
    private String snsId;

    @JSONField(name = "type")
    private Integer type;

    @JSONField(name = "commentList")
    private String commentList;

    @JSONField(name = "likeList")
    private String likeList;

    @JSONField(name = "createTime")
    private Integer createTime;

    @JSONField(name = "content")
    private String content;

    @JSONField(name = "coverImage")
    private String coverImage;

    @JSONField(name = "title")
    private String title;

    @JSONField(name = "lat")
    private String lat;

    @JSONField(name = "lng")
    private String lng;

    @JSONField(name = "location")
    private String location;

    @JSONField(name = "urls")
    private String urls;

    @JSONField(name = "userName")
    private String userName;

    @JSONField(name = "status")
    private Integer status;
}
