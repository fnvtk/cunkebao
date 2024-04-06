package cn.myerm.business.param;

import lombok.Data;

@Data
public class WechatFriendLabelParam {
    private Integer FriendRequestTaskId;
    private Integer FriendRequestTaskDetailId;
    private Integer WeChatFriendId;
    private Integer LabelId;
    private String sValue;

    //如果true，表名这个标签的值只能有一个
    private Boolean isUnique = true;
}
