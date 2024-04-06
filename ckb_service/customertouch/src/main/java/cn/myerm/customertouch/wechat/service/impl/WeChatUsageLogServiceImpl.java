package cn.myerm.customertouch.wechat.service.impl;

import cn.myerm.customertouch.wechat.entity.WeChatUsageLog;
import cn.myerm.customertouch.wechat.mapper.WeChatUsageLogMapper;
import cn.myerm.customertouch.wechat.param.WeChatUsageLogParam;
import cn.myerm.customertouch.wechat.service.IWeChatUsageLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class WeChatUsageLogServiceImpl extends ServiceImpl<WeChatUsageLogMapper, WeChatUsageLog> implements IWeChatUsageLogService {
    public void newSave(WeChatUsageLogParam weChatUsageLogParam) {
        WeChatUsageLog weChatUsageLog = new WeChatUsageLog();
        weChatUsageLog.setWeChatId(weChatUsageLogParam.getWeChatId());
        weChatUsageLog.setTypeId(weChatUsageLogParam.getTypeId());
        weChatUsageLog.setTargetId(weChatUsageLogParam.getTargetId());
        weChatUsageLog.setDNewTime(weChatUsageLogParam.getDNewTime());
        save(weChatUsageLog);
    }
}
