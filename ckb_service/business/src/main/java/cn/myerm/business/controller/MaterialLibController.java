package cn.myerm.business.controller;

import cn.myerm.business.dto.MaterialLibDto;
import cn.myerm.business.dto.WeChatDto;
import cn.myerm.business.entity.MaterialLib;
import cn.myerm.business.entity.WeChat;
import cn.myerm.business.param.MaterialLibParam;
import cn.myerm.business.service.impl.MaterialLibServiceImpl;
import cn.myerm.business.service.impl.MaterialServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/v1/backend/business/materiallib")
public class MaterialLibController extends BusinessController {

    private final MaterialLibServiceImpl materiallibService;

    private final MaterialServiceImpl materialService;

    @Autowired
    public MaterialLibController(MaterialServiceImpl materialService, MaterialLibServiceImpl materiallibService) {
        this.materialService = materialService;
        this.businessService = materiallibService;
        this.materiallibService = materiallibService;
    }

    @PostMapping("/new/save")
    public MessageDTO newSave(MaterialLibParam materialLibParam) {
        Integer id = materiallibService.save(materialLibParam);
        materialService.flush(id);
        return success();
    }

    @PostMapping("/edit/save")
    public MessageDTO editSave(MaterialLibParam materialLibParam) {
        Integer id = materiallibService.save(materialLibParam);
        materialService.flush(id);
        return success();
    }

    @PostMapping("/getbyid")
    public MessageDTO getById(Integer id) {
        MaterialLib materialLib = materiallibService.getById(id);
        MaterialLibDto materialLibDto = BeanUtils.transform(MaterialLibDto.class, materialLib);
        materialLibDto.setConfig(JSONObject.parseObject(materialLib.getSConfigJson()));

        return success(materialLibDto);
    }

    @PostMapping("/getbyids")
    public MessageDTO getByIds(@RequestParam(name = "ids", required = true) String ids) {
        List<MaterialLib> materialLibs = materiallibService.listByIds(Arrays.asList(ids.split(",")));
        return success(BeanUtils.batchTransform(MaterialLibDto.class, materialLibs));
    }
}
