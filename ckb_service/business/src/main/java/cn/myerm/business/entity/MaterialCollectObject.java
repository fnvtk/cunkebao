package cn.myerm.business.entity;

import lombok.Data;

@Data
public class MaterialCollectObject {
    private Integer MaterialLibId;
    private String sTimeLimit;
    private String sKeyWord;
    private String sExclude;
    private Integer NewUserId;
    private String sCollectObjectId;
    private Integer WeChatId;
    private Integer ChatroomId;
    private String sWechatId;
    private String sAvatar;
    private String sNickName;
}
