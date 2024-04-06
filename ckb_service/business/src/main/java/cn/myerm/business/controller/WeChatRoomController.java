package cn.myerm.business.controller;

import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.business.service.impl.WeChatRoomServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/wechatroom")
public class WeChatRoomController extends BusinessController {

    private final WeChatRoomServiceImpl weChatRoomService;

    @Autowired
    public WeChatRoomController(WeChatRoomServiceImpl wechatroomService) {
        this.weChatRoomService = wechatroomService;
        this.businessService = wechatroomService;
    }

    @PostMapping("/sync")
    @OperaPermission("sync")
    public MessageDTO sync() {
        weChatRoomService.setSync();
        return success("同步任务已提交后台执行，请稍后查看。");
    }

    @PostMapping("/getbyids")
    public MessageDTO getByIds(@RequestParam(name = "chatroomids", required = true) String sChatroomIds) {
        return success(weChatRoomService.getByIds(sChatroomIds));
    }
}
