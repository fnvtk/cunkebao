package cn.myerm.customertouch.wechat.controller;

import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.customertouch.wechat.handler.WechatHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/customertouch/wechat")
public class TestController extends CommonController {

    private final WechatHandle wechatHandle;

    @Autowired
    public TestController(WechatHandle wechatHandle) {
        this.wechatHandle = wechatHandle;
    }

    @PostMapping("/test")
    public MessageDTO test(String msg) {

        Map<String, String> map = new HashMap<>();
        map.put("content", msg);
        map.put("wechatAccountId", "17674261");
        map.put("wechatChatroomId", "687416");
        map.put("msgType", "1");

        wechatHandle.sendMessageToChatroom(map);

        return success();
    }
}
