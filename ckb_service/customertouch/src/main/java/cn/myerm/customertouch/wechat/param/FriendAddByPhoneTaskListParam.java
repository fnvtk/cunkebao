package cn.myerm.customertouch.wechat.param;

import lombok.Data;

@Data
public class FriendAddByPhoneTaskListParam {
    private String keyword;
    private Integer status;
    private Integer pageIndex;
    private Integer pageSize;
}
