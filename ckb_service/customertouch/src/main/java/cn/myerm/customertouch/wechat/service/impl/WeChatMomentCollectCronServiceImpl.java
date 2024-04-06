package cn.myerm.customertouch.wechat.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.myerm.customertouch.wechat.entity.WeChatMomentCollectCron;
import cn.myerm.customertouch.wechat.mapper.WeChatMomentCollectCronMapper;
import cn.myerm.customertouch.wechat.service.IWeChatMomentCollectCronService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class WeChatMomentCollectCronServiceImpl extends ServiceImpl<WeChatMomentCollectCronMapper, WeChatMomentCollectCron> implements IWeChatMomentCollectCronService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatMomentCollectCronServiceImpl.class);

    /**
     * 运算
     *
     * @param weChatMomentCollectCron
     */
    public void calculateNextExecTime(WeChatMomentCollectCron weChatMomentCollectCron) {

        LocalDateTime dLastTime = weChatMomentCollectCron.getDLastCollectTime();
        if (dLastTime == null) {
            dLastTime = LocalDateTime.now();
        }

        //如果还没全量采集，则1至5分钟一次，尽快全量采集一遍
        if (weChatMomentCollectCron.getBFullCollected() == null || weChatMomentCollectCron.getBFullCollected().equals(0)) {
            Random random = new Random();
            int randomNumber = random.nextInt(5) + 1;
            weChatMomentCollectCron.setDNextCollectTime(dLastTime.plusMinutes(randomNumber));
            return;
        }

        //根据上一次执行的时间，计算下次执行的时间
        String sInterval = weChatMomentCollectCron.getSCollectInterval();
        if (StrUtil.isEmpty(sInterval)) {
            sInterval = "30-60";//缺省是30-60分钟采集一次朋友圈
        }

        String[] arrInterval = sInterval.split("-");
        int minValue = Integer.parseInt(arrInterval[0]);
        int maxValue = Integer.parseInt(arrInterval[1]);

        Random random = new Random();
        int randomNumber = random.nextInt(maxValue - minValue + 1) + minValue;
        weChatMomentCollectCron.setDNextCollectTime(dLastTime.plusMinutes(randomNumber));//计算下一次执行的时间

        if (StrUtil.isNotEmpty(weChatMomentCollectCron.getSCollectPeriod())) {//如果有设置可允许的运行期间
            String[] arrPeriod = weChatMomentCollectCron.getSCollectPeriod().split("-");

            int startValue = Integer.parseInt(arrPeriod[0]);
            int endValue = Integer.parseInt(arrPeriod[1]);

            while (true) {//无限循环，直至符合运行期间为止
                LocalDateTime dNextTime = weChatMomentCollectCron.getDNextCollectTime();
                int lHour = dNextTime.getHour();

                if (startValue < endValue) {//表示当天
                    if (lHour >= startValue && lHour <= endValue) {
                        break;
                    }
                } else {//表示有跨天
                    if (lHour <= endValue || lHour >= startValue) {
                        break;
                    }
                }

                randomNumber = random.nextInt(maxValue - minValue + 1) + minValue;
                weChatMomentCollectCron.setDNextCollectTime(dNextTime.plusMinutes(randomNumber));
            }
        }
    }

    /**
     * 全量推送采集对象
     *
     * @param jsonObject
     */
    public void push(JSONObject jsonObject) {
        String Id = jsonObject.getString("accountid") + "-" + jsonObject.getString("friendid");

        WeChatMomentCollectCron weChatMomentCollectCron = getById(Id);
        if (weChatMomentCollectCron == null) {
            weChatMomentCollectCron = new WeChatMomentCollectCron();
            weChatMomentCollectCron.setBFullCollected(0);
        }

        weChatMomentCollectCron.setSId(Id);
        weChatMomentCollectCron.setWeChatId(jsonObject.getInteger("accountid"));
        weChatMomentCollectCron.setWeChatFriendId(jsonObject.getInteger("friendid"));
        weChatMomentCollectCron.setSUserName(jsonObject.getString("username"));
        weChatMomentCollectCron.setBEnable(jsonObject.getInteger("enable"));

        calculateNextExecTime(weChatMomentCollectCron);

        saveOrUpdate(weChatMomentCollectCron);
    }

    /**
     * 把已删除，已失效的采集任务设置为禁用
     */
    public void disable(List<String> listFriendId) {
        UpdateWrapper<WeChatMomentCollectCron> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.notIn("WeChatFriendId", listFriendId);
        objectUpdateWrapper.set("bEnable", 0);
        update(objectUpdateWrapper);
    }
}
