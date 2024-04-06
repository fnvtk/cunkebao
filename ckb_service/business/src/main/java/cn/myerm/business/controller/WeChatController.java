package cn.myerm.business.controller;

import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.business.dto.WeChatDto;
import cn.myerm.business.entity.WeChat;
import cn.myerm.business.service.impl.WeChatServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1/backend/business/wechat")
public class WeChatController extends BusinessController {

    private final WeChatServiceImpl weChatService;

    @Autowired
    public WeChatController(WeChatServiceImpl weChatService, WeChatServiceImpl wechatService) {
        this.weChatService = weChatService;
        this.businessService = wechatService;
    }

    @PostMapping("/sync")
    @OperaPermission("sync")
    public MessageDTO sync() {
        weChatService.setSync();
        return success("同步任务已提交后台执行，请一分钟后查看。");
    }

    @PostMapping("/getbyids")
    public MessageDTO getByIds(@RequestParam(name = "ids", required = true) String ids) {
        List<WeChat> weChats = weChatService.listByIds(Arrays.asList(ids.split(",")));

        for (WeChat weChat : weChats) {
            if (weChatService.isAlive(weChat)) {
                weChat.setBOnline(1);
            } else {
                weChat.setBOnline(0);
            }
        }

        return success(BeanUtils.batchTransform(WeChatDto.class, weChats));
    }


}
