package cn.myerm.customertouch.wechat.cron;

import cn.myerm.customertouch.wechat.handler.WechatHandle;
import cn.myerm.customertouch.wechat.service.impl.WeChatServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class WechatCron {
    private final WechatHandle wechatHandle;
    private final WeChatServiceImpl weChatService;

    @Autowired
    public WechatCron(WechatHandle wechatHandle, WeChatServiceImpl weChatService) {
        this.wechatHandle = wechatHandle;
        this.weChatService = weChatService;
    }

    /**
     * 防踢，每30s
     */
    @Scheduled(cron = "53,23 * * * * ?")
    private void keepAlive() {
        wechatHandle.keepAlive();
    }

    /**
     * 采集朋友圈
     */
    @Scheduled(cron = "16,46 * * * * ?")
    private void momentCollect() {
        wechatHandle.momentCollect();
    }

    /**
     * 采集朋友圈的资源，图片|视频等
     */
    @Scheduled(cron = "*/5 * * * * ?")
    private void momentResourceCollect() {
        wechatHandle.momentResourceCollect();
    }

    @Scheduled(cron = "20 * * * * ?")
    private void handleFrequent() {
        JSONObject mapParam = new JSONObject();

        mapParam.put("keyword", "");
        mapParam.put("status", "");
        mapParam.put("pageIndex", 0);
        mapParam.put("pageSize", 100);

        JSONArray arrResult = wechatHandle.addFriendByPhoneTaskList(mapParam);

        weChatService.handleFrequent(arrResult);
    }

}
