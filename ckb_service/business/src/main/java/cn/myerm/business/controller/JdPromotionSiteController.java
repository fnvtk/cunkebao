package cn.myerm.business.controller;

import cn.myerm.business.entity.JdPromotionSite;
import cn.myerm.business.service.impl.JdPromotionSiteServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/jdpromotionsite")
public class JdPromotionSiteController extends BusinessController {

    private final JdPromotionSiteServiceImpl jdpromotionsiteService;

    @Autowired
    public JdPromotionSiteController(JdPromotionSiteServiceImpl jdpromotionsiteService) {
        this.businessService = jdpromotionsiteService;
        this.jdpromotionsiteService = jdpromotionsiteService;
    }

    @PostMapping("/getall")
    public MessageDTO getAll(String id) {
        QueryWrapper<JdPromotionSite> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("JdSocialMediaId", id);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", jdpromotionsiteService.list(queryWrapper));

        return success(jsonObject);
    }
}
