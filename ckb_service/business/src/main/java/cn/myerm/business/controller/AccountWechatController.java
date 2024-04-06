package cn.myerm.business.controller;

import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.impl.AccountServiceImpl;
import cn.myerm.business.service.impl.AccountWechatServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/accountwechat")
public class AccountWechatController extends BusinessController {

    private AccountWechatServiceImpl accountWechatService;

    @Autowired
    public AccountWechatController(AccountWechatServiceImpl accountwechatService) {
        this.businessService = accountwechatService;
        this.accountWechatService = accountwechatService;
    }


    @PostMapping("/bind")
    @OperaPermission("new")
    public MessageDTO bind(@Validated ListParam listParam) {
        accountWechatService.bind(listParam);
        return success();
    }
}
