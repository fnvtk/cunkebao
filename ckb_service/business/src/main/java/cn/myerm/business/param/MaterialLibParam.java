package cn.myerm.business.param;

import lombok.Data;

@Data
public class MaterialLibParam {
    private Integer id;
    private String sName;
    private Boolean bEnable;
    private Boolean bAIEnable;
    private String arrFriendId;
    private String arrChatroomId;
    private String chatroomMember;
    private String sTimeLimit;
    private String sKeyWord;
    private String sExclude;
    private String sAIRequire;
}
