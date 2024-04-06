package cn.myerm.customertouch.wechat.service.impl;

import cn.myerm.customertouch.wechat.entity.WeChatRoomCollectCron;
import cn.myerm.customertouch.wechat.mapper.WeChatRoomCollectCronMapper;
import cn.myerm.customertouch.wechat.service.IWeChatRoomCollectCronService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class WeChatRoomCollectCronServiceImpl extends ServiceImpl<WeChatRoomCollectCronMapper, WeChatRoomCollectCron> implements IWeChatRoomCollectCronService {
    public void push(JSONObject jsonObject) {

        //清空所有的配置
        QueryWrapper<WeChatRoomCollectCron> queryWrapper = new QueryWrapper<>();
        remove(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("WeChatRoomId", jsonObject.getString("chatroomid"));
        remove(queryWrapper);

        JSONArray memberwechatids = jsonObject.getJSONArray("memberwechatids");
        for (int i = 0; i < memberwechatids.size(); i++) {
            String sMemberWechatId = memberwechatids.getString(i);
            String Id = jsonObject.getString("chatroomid") + "-" + sMemberWechatId;

            WeChatRoomCollectCron weChatRoomCollectCron = new WeChatRoomCollectCron();
            weChatRoomCollectCron.setSId(Id);
            weChatRoomCollectCron.setWeChatRoomId(jsonObject.getIntValue("chatroomid"));
            weChatRoomCollectCron.setSWeChatId(sMemberWechatId);
            weChatRoomCollectCron.setBEnable(jsonObject.getBoolean("enable") ? 1 : 0);
            saveOrUpdate(weChatRoomCollectCron);
        }
    }
}
