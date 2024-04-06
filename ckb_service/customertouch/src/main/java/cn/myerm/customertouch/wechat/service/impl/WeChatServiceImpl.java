package cn.myerm.customertouch.wechat.service.impl;

import cn.myerm.common.exception.SystemException;
import cn.myerm.customertouch.wechat.entity.WeChat;
import cn.myerm.customertouch.wechat.entity.WeChatUsageLog;
import cn.myerm.customertouch.wechat.mapper.WeChatMapper;
import cn.myerm.customertouch.wechat.param.WeChatUsageLogParam;
import cn.myerm.customertouch.wechat.service.IWeChatService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class WeChatServiceImpl extends ServiceImpl<WeChatMapper, WeChat> implements IWeChatService {

    private final WeChatUsageLogServiceImpl weChatUsageLogService;

    @Autowired
    public WeChatServiceImpl(WeChatUsageLogServiceImpl weChatUsageLogService) {
        this.weChatUsageLogService = weChatUsageLogService;
    }

    /**
     * 处理频繁
     */
    public void handleFrequent(JSONArray arrResult) {
        for (int i = 0; i < arrResult.size(); i++) {
            JSONObject jResult = arrResult.getJSONObject(i);

            if (jResult.getIntValue("status") == 2) {//执行失败
                if (jResult.getString("extra") != null && jResult.getString("extra").equals("操作过于频繁，请稍后再试")) {
                    WeChat wechat = getById(jResult.getIntValue("wechatAccountId"));

                    LocalDateTime time1 = LocalDateTime.parse(jResult.getString("createTime"));
                    LocalDateTime time2 = wechat.getDLastFriendRequestFrequentlyTime();

                    //判断与最近一次频繁的时间是否超过了72小时
                    //如果最近一次频繁时间是空，也判定为超过了72小时
                    //每日请求数=基准-1
                    if (time2 == null || Duration.between(time2, time1).toHours() >= 72) {
                        wechat.setDLastFriendRequestFrequentlyTime(time1);
                        wechat.setLPerDayAmt(wechat.getLPerDayAmtDefault() - 1);
                        updateById(wechat);
                    } else if (Duration.between(time2, time1).toHours() >= 24 && Duration.between(time2, time1).toHours() < 72) {
                        //距离上一次频繁24小时-72小时，那基准就要减1
                        wechat.setLPerDayAmtDefault(wechat.getLPerDayAmtDefault() - 1);
                        wechat.setLPerDayAmt(wechat.getLPerDayAmtDefault() - 1);
                        updateById(wechat);
                    }
                }
            }
        }

        //把超过72小时频繁的微信号，恢复到正常
        UpdateWrapper<WeChat> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.setSql("lPerDayAmt=lPerDayAmtDefault");
        objectUpdateWrapper.lt("dLastFriendRequestFrequentlyTime", LocalDateTime.now().minusHours(72));
        update(objectUpdateWrapper);
    }

    /**
     * 检查该微信号的状态是否合法
     * 规则：
     * 1、24小时之内只能加20个好友
     * 2、
     *
     * @param weChatUsageLogParam
     */
    public void checkValid(WeChatUsageLogParam weChatUsageLogParam) {
        WeChat weChat = getById(weChatUsageLogParam.getWeChatId());
        if (weChat == null) {
            weChat = new WeChat();
            weChat.setLId(weChatUsageLogParam.getWeChatId());
            weChat.setLPerDayAmt(20);
            weChat.setLPerDayAmtDefault(20);
            save(weChat);
        }

        QueryWrapper<WeChatUsageLog> objectQueryWrapper = new QueryWrapper<>();
        if (weChatUsageLogParam.getTypeId() == 1) {//加人，每天每个号只能允许20个
            objectQueryWrapper.eq("WeChatId", weChatUsageLogParam.getWeChatId());
            objectQueryWrapper.eq("TypeId", 1);
            objectQueryWrapper.gt("dNewTime", weChatUsageLogParam.getDNewTime().minusHours(24));
            int lCnt = weChatUsageLogService.count(objectQueryWrapper);
            if (lCnt >= weChat.getLPerDayAmt()) {
                throw new SystemException(11000L, "每个号24小时内只允许添加" + weChat.getLPerDayAmt() + "个好友，请稍后再试。");
            }

            if (weChat.getDLastFriendRequestFrequentlyTime() != null) {//如果有频繁的时间，就要判断是否在24小时之内
                LocalDateTime time2 = weChat.getDLastFriendRequestFrequentlyTime();
                LocalDateTime time1 = LocalDateTime.now();
                if (Duration.between(time2, time1).toHours() < 24) {
                    throw new SystemException(11000L, "因频繁被限制的微信号，在24小时之内禁止加好友。");
                }
            }

            if (weChat.getDLastFriendRequestTime() != null) {
                if (Duration.between(weChat.getDLastFriendRequestTime(), LocalDateTime.now()).toMinutes() < 2) {
                    throw new SystemException(11000L, "距离上次好友申请还不到一分钟");
                }
            }
        }
    }
}
