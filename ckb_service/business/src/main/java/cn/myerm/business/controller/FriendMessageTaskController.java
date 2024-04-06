package cn.myerm.business.controller;

import cn.myerm.business.service.impl.FriendMessageTaskServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/friendmessagetask")
public class FriendMessageTaskController extends BusinessController {

    private final FriendMessageTaskServiceImpl friendmessagetaskService;

    @Autowired
    public FriendMessageTaskController(FriendMessageTaskServiceImpl friendmessagetaskService) {
        this.businessService = friendmessagetaskService;
        this.friendmessagetaskService = friendmessagetaskService;
    }
}
