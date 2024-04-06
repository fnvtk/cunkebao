package cn.myerm.customertouch.douyin.controller;

import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.customertouch.douyin.handler.Douyin;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/customertouch/douyin")
public class DouyinController extends CommonController {

    private final Douyin douyin;

    @Autowired
    public DouyinController(Douyin douyin) {
        this.douyin = douyin;
    }

    /**
     * 获取抖音授权的URL
     * @return MessageDTO
     */
    @PostMapping("/getWxBindDyUrl")
    public MessageDTO getWxBindDyUrl() {
        Map<String, Object> mapParam = new HashMap<>();

        JSONObject wxBindDyUrl = douyin.getWxBindDyUrl();
        mapParam.put("url", wxBindDyUrl.getString("url"));
        mapParam.put("id", wxBindDyUrl.getString("id"));
        return success(mapParam);
    }

    /**
     * 获取超级管家的商户列表
     * @return MessageDTO
     */
    @PostMapping("/getWebMerchantList")
    public MessageDTO getWebMerchantList() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("results", douyin.getWebMerchantList());
        return success(mapParam);
    }

    /**
     * 登录抖音账号
     * @return MessageDTO
     */
    @PostMapping("/login")
    public MessageDTO login(String merchantId) {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("token", douyin.login(merchantId));
        return success(mapParam);
    }

}
