package cn.myerm.business.controller;

import cn.myerm.business.dto.MaterialPushMomentTaskDto;
import cn.myerm.business.service.impl.MaterialPushLogServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/materialpushlog")
public class MaterialPushLogController extends BusinessController {

    private final MaterialPushLogServiceImpl materialpushlogService;

    @Autowired
    public MaterialPushLogController(MaterialPushLogServiceImpl materialpushlogService) {
        this.businessService = materialpushlogService;
        this.materialpushlogService = materialpushlogService;
    }

    @PostMapping("/getmomentstatus")
    public MessageDTO getMomentStatus(Integer id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", materialpushlogService.getMomentStatus(id));

        return success(jsonObject);
    }
}
