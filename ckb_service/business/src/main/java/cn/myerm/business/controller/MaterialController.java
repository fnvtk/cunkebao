package cn.myerm.business.controller;

import cn.myerm.business.dto.MaterialDto;
import cn.myerm.business.entity.Material;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.param.MaterialParam;
import cn.myerm.business.service.impl.MaterialServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/v1/backend/business/material")
public class MaterialController extends BusinessController {

    private final MaterialServiceImpl materialService;

    private final SysAttachServiceImpl sysAttachService;

    @Autowired
    public MaterialController(MaterialServiceImpl materialService, SysAttachServiceImpl sysAttachService) {
        this.materialService = materialService;
        this.sysAttachService = sysAttachService;
        this.businessService = materialService;
    }

    /**
     * 新建保存
     *
     * @param materialParam
     * @return
     */
    @PostMapping("/newmaterialsave")
    public MessageDTO newSave(@Validated MaterialParam materialParam) {
        return success(materialService.newSave(materialParam));
    }

    /**
     * 编辑保存
     *
     * @param materialParam
     * @return
     */
    @PostMapping("/editmaterialsave")
    public MessageDTO editSave(@Validated MaterialParam materialParam) {
        materialService.editSave(materialParam);
        return success("操作成功");
    }

    /**
     * 编辑
     *
     * @param id
     * @return
     */
    @PostMapping("/get")
    public MessageDTO edit(Integer id) {
        Material material = materialService.getById(id);

        MaterialDto materialDto = JSON.parseObject(JSONObject.toJSONString(material), MaterialDto.class);

        if (material.getSPic() != null) {
            materialDto.setArrPic(sysAttachService.getByIds(material.getSPic().split(",")));
        }

        if (material.getTypeId() == 4) {
            materialDto.setVideo(sysAttachService.getById(material.getVideoId()));
        }

        return success(materialDto);
    }

    @PostMapping("/getminiprogramename")
    public MessageDTO getMPName(Integer id) {
        Material material = materialService.getById(id);

        String regex = "<title>(.*?)</title>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(material.getSSourceContent());

        if (matcher.find()) {
            return success(matcher.group(1));
        }

        return success("");
    }

    @PostMapping("/regenerateai")
    public MessageDTO regenerateai(@Validated ListParam listParam) {
        materialService.reGenerateAi(listParam);
        return success("");
    }
}
