package cn.myerm.business.controller;

import cn.myerm.business.service.impl.UserIncomeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/userincome")
public class UserIncomeController extends BusinessController {

    private final UserIncomeServiceImpl userincomeService;

    @Autowired
    public UserIncomeController(UserIncomeServiceImpl userincomeService) {
        this.businessService = userincomeService;
        this.userincomeService = userincomeService;
    }
}
