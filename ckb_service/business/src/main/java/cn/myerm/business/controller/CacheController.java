package cn.myerm.business.controller;

import cn.myerm.business.service.impl.CacheServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/cache")
public class CacheController extends BusinessController {
    @Autowired
    public CacheController(CacheServiceImpl cacheService) {
        this.businessService = cacheService;
    }
}
