package cn.myerm.business.entity;

public interface IMaterialPushTask {
    public Integer getLId();
    public Integer getLRound();
    public Integer getBLoop();
    public String getSPushSchedule();
    public String getMaterialLibIds();
    public Long getJdPromotionSiteId();
    public String getSType();
    public String getSName();
    public String getPushWechatIds();
    public String getPushWechatroomIds();
    public Integer getTypeId();
    public Integer getBImmediately();
    public Integer getLPushDelay();
    public void setLRound(Integer lRound);
}
