package cn.myerm.business.controller;

import cn.myerm.business.service.impl.WeChatRoomMemberServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/wechatroommember")
public class WeChatRoomMemberController extends BusinessController {

    private final WeChatRoomMemberServiceImpl weChatRoomMemberService;

    @Autowired
    public WeChatRoomMemberController(WeChatRoomMemberServiceImpl wechatroommemberService) {
        this.businessService = wechatroommemberService;
        this.weChatRoomMemberService = wechatroommemberService;
    }

    @PostMapping("/getbyids")
    public MessageDTO getByIds(@RequestParam(name = "memberids", required = true) String sMemberIds) {
        return success(weChatRoomMemberService.getByIds(sMemberIds));
    }
}
