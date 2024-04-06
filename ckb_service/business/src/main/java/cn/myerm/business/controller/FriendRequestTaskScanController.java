package cn.myerm.business.controller;

import cn.myerm.business.service.impl.FriendRequestTaskScanServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/friendrequesttaskscan")
public class FriendRequestTaskScanController extends BusinessController {

    private final FriendRequestTaskScanServiceImpl friendrequesttaskscanService;

    @Autowired
    public FriendRequestTaskScanController(FriendRequestTaskScanServiceImpl friendrequesttaskscanService) {
        this.businessService = friendrequesttaskscanService;
        this.friendrequesttaskscanService = friendrequesttaskscanService;
    }
}
