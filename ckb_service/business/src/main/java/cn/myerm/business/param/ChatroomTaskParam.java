package cn.myerm.business.param;

import lombok.Data;

@Data
public class ChatroomTaskParam {
    private Integer lId;
    private String sName;
    private String sPeriod;
    private Integer lPushAmtPerDay;
    private Integer lPushDelay;
    private Integer TypeId;
    private Boolean bEnable;
    private Boolean bLoop;
    private Boolean bImmediately;
    private Long JdPromotionSiteId;
    private String arrWechatroomId;
    private String arrMaterialLibId;
}