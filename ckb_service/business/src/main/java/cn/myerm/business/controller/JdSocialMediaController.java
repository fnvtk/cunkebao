package cn.myerm.business.controller;

import cn.myerm.business.param.MaterialLibParam;
import cn.myerm.business.service.impl.JdSocialMediaServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/jdsocialmedia")
public class JdSocialMediaController extends BusinessController {

    private final JdSocialMediaServiceImpl jdsocialmediaService;

    @Autowired
    public JdSocialMediaController(JdSocialMediaServiceImpl jdsocialmediaService) {
        this.businessService = jdsocialmediaService;
        this.jdsocialmediaService = jdsocialmediaService;
    }

    @PostMapping("/importsave")
    public MessageDTO importSave(String data) {
        jdsocialmediaService.importSave(data);
        return success();
    }

    @PostMapping("/getall")
    public MessageDTO getAll() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("results", jdsocialmediaService.list());

        return success(jsonObject);
    }
}
