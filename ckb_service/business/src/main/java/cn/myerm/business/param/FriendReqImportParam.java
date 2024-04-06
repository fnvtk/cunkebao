package cn.myerm.business.param;


import lombok.Data;


@Data
public class FriendReqImportParam {
    private String file;
    private String phone;
    private String tags;
    private Integer objectid;
}
