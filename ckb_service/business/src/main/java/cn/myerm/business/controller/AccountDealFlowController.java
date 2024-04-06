package cn.myerm.business.controller;

import cn.myerm.business.service.impl.AccountDealFlowServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/business/business/accountdealflow")
public class AccountDealFlowController extends BusinessController {
    @Autowired
    public AccountDealFlowController(AccountDealFlowServiceImpl accountdealflowService) {
        this.businessService = accountdealflowService;
    }
}
