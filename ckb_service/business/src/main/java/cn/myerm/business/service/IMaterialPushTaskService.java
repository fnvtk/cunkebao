package cn.myerm.business.service;

import cn.myerm.business.entity.IMaterialPushTask;
import cn.myerm.business.param.PushTaskParam;

import java.util.List;

public interface IMaterialPushTaskService<T> extends IBusinessService<T> {
    public List<IMaterialPushTask> getEnableList(boolean bImmediately);
    public void updateById(IMaterialPushTask materialPushTask);
    public void updateStatus(PushTaskParam param);
}