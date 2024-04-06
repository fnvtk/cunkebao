package cn.myerm.business.controller;

import cn.myerm.business.service.impl.FriendRequestTaskDetailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/friendrequesttaskdetail")
public class FriendRequestTaskDetailController extends BusinessController {

    private final FriendRequestTaskDetailServiceImpl friendrequesttaskdetailService;

    @Autowired
    public FriendRequestTaskDetailController(FriendRequestTaskDetailServiceImpl friendrequesttaskdetailService) {
        this.businessService = friendrequesttaskdetailService;
        this.friendrequesttaskdetailService = friendrequesttaskdetailService;
    }
}
