package cn.myerm.business.controller;

import cn.myerm.business.dto.MaterialPushMomentTaskDto;
import cn.myerm.business.entity.MaterialPushMomentTask;
import cn.myerm.business.param.MomentTaskParam;
import cn.myerm.business.service.impl.MaterialPushMomentTaskServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/materialpushmomenttask")
public class MaterialPushMomentTaskController extends BusinessController {

    private final MaterialPushMomentTaskServiceImpl materialpushmomenttaskService;

    @Autowired
    public MaterialPushMomentTaskController(MaterialPushMomentTaskServiceImpl materialpushmomenttaskService) {
        this.businessService = materialpushmomenttaskService;
        this.materialpushmomenttaskService = materialpushmomenttaskService;
    }

    @PostMapping("/new/save")
    public MessageDTO newSave(MomentTaskParam momentTaskParam) {
        materialpushmomenttaskService.save(momentTaskParam);
        return success();
    }

    @PostMapping("/edit/save")
    public MessageDTO editSave(MomentTaskParam momentTaskParam) {
        materialpushmomenttaskService.save(momentTaskParam);
        return success();
    }

    @PostMapping("/getbyid")
    public MessageDTO getOne(Integer id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", BeanUtils.transform(MaterialPushMomentTaskDto.class, materialpushmomenttaskService.getById(id)));

        return success(jsonObject);
    }
}
