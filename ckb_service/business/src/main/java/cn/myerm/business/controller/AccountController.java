package cn.myerm.business.controller;

import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.business.dto.AccountDto;
import cn.myerm.business.entity.Account;
import cn.myerm.business.service.IAccountService;
import cn.myerm.business.service.impl.AccountServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import cn.myerm.system.service.impl.SysUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/backend/business/account")
public class AccountController extends BusinessController {

    private AccountServiceImpl accountService;

    @Autowired
    public AccountController(AccountServiceImpl accountService) {
        this.businessService = accountService;
        this.accountService = accountService;
    }

    @PostMapping("/newuser")
    @OperaPermission("new")
    public MessageDTO newUser(@RequestParam(name = "AccountId", required = true) String AccountId,
                              @RequestParam(name = "sName", required = true) String sName,
                              @RequestParam(name = "sMobile", required = true) String sMobile) {

        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("AccountId", AccountId);
        mapParam.put("sName", sName);
        mapParam.put("sMobile", sMobile);
        accountService.newAccountUser(mapParam);

        return success();
    }

    @PostMapping("/getall")
    public MessageDTO getAll() {
        List<Account> accounts = accountService.list();
        List<AccountDto> accountDtos = BeanUtils.batchTransform(AccountDto.class, accounts);
        return success(accountDtos);
    }

    @PostMapping("/getuserbyacc")
    public MessageDTO getUserByAcc(Integer accountid) {
        return success(accountService.getUserByAcc(accountid));
    }
}
