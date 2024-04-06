package cn.myerm.customertouch.douyin.cron;

import cn.myerm.customertouch.douyin.handler.Douyin;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@EnableScheduling
public class DouYinCron {
    @Resource
    private Douyin douyin;
}
