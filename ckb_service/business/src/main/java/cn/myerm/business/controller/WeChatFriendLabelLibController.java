package cn.myerm.business.controller;

import cn.myerm.business.dto.WeChatFriendLabelLibDto;
import cn.myerm.business.entity.WeChatFriendLabelLib;
import cn.myerm.business.service.impl.WeChatFriendLabelLibServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/backend/business/wechatfriendlabellib")
public class WeChatFriendLabelLibController extends BusinessController {

    private final WeChatFriendLabelLibServiceImpl wechatfriendlabellibService;

    @Autowired
    public WeChatFriendLabelLibController(WeChatFriendLabelLibServiceImpl wechatfriendlabellibService) {
        this.businessService = wechatfriendlabellibService;
        this.wechatfriendlabellibService = wechatfriendlabellibService;
    }

    @PostMapping("/alltag")
    public MessageDTO getAllTag(@RequestParam(name = "taskid", required = false) Integer taskid) {
        List<WeChatFriendLabelLib> allTag = wechatfriendlabellibService.getAllTag(taskid);
        return success(BeanUtils.batchTransform(WeChatFriendLabelLibDto.class, allTag));
    }
}
