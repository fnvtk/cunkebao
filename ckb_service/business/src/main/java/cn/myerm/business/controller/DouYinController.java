package cn.myerm.business.controller;

import cn.myerm.business.service.impl.DouYinServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/backend/business/douyin")
public class DouYinController extends BusinessController {

    private final DouYinServiceImpl douyinService;

    @Autowired
    public DouYinController(DouYinServiceImpl douyinService) {
        this.businessService = douyinService;
        this.douyinService = douyinService;
    }

    @PostMapping("/getDouYinList")
    public MessageDTO getWebMerchantList() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("results", douyinService.getDouYinList());
        return success(mapParam);
    }

    @PostMapping("/getbindqrcode")
    public MessageDTO getBindQrcode() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("qrcode", douyinService.getBindQrcode());
        return success(mapParam);
    }
}
