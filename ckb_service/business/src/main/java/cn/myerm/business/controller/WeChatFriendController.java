package cn.myerm.business.controller;

import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.business.service.impl.WeChatFriendServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/wechatfriend")
public class WeChatFriendController extends BusinessController {

    private final WeChatFriendServiceImpl weChatFriendService;

    @Autowired
    public WeChatFriendController(WeChatFriendServiceImpl weChatFriendService, WeChatFriendServiceImpl wechatfriendService) {
        this.weChatFriendService = weChatFriendService;
        this.businessService = wechatfriendService;
    }

    @PostMapping("/sync")
    @OperaPermission("sync")
    public MessageDTO sync() {
        weChatFriendService.setSync();
        return success("同步任务已提交后台执行，该任务数据量庞大，请稍后查看。");
    }

    @PostMapping("/getbyids")
    public MessageDTO getByIds(@RequestParam(name = "friendids", required = true) String sFrindIds) {
        return success(weChatFriendService.getByIds(sFrindIds));
    }

}
