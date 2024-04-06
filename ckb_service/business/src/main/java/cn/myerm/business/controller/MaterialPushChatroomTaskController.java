package cn.myerm.business.controller;

import cn.myerm.business.dto.MaterialPushChatroomTaskDto;
import cn.myerm.business.entity.JdPromotionSite;
import cn.myerm.business.entity.MaterialPushChatroomTask;
import cn.myerm.business.param.ChatroomTaskParam;
import cn.myerm.business.service.impl.JdPromotionSiteServiceImpl;
import cn.myerm.business.service.impl.MaterialPushChatroomTaskServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/materialpushchatroomtask")
public class MaterialPushChatroomTaskController extends BusinessController {

    private final MaterialPushChatroomTaskServiceImpl materialpushchatroomtaskService;
    private final JdPromotionSiteServiceImpl jdSocialMediaService;

    @Autowired
    public MaterialPushChatroomTaskController(JdPromotionSiteServiceImpl jdSocialMediaService, MaterialPushChatroomTaskServiceImpl materialpushchatroomtaskService) {
        this.jdSocialMediaService = jdSocialMediaService;
        this.businessService = materialpushchatroomtaskService;
        this.materialpushchatroomtaskService = materialpushchatroomtaskService;
    }

    @PostMapping("/new/save")
    public MessageDTO newSave(ChatroomTaskParam chatroomTaskParam) {
        materialpushchatroomtaskService.save(chatroomTaskParam);
        return success();
    }

    @PostMapping("/edit/save")
    public MessageDTO editSave(ChatroomTaskParam chatroomTaskParam) {
        materialpushchatroomtaskService.save(chatroomTaskParam);
        return success();
    }

    @PostMapping("/getbyid")
    public MessageDTO getOne(Integer id) {

        MaterialPushChatroomTask chatroomTask = materialpushchatroomtaskService.getById(id);
        if (chatroomTask.getJdPromotionSiteId() != null) {
            JdPromotionSite jdPromotionSite = jdSocialMediaService.getById(chatroomTask.getJdPromotionSiteId());
            chatroomTask.setJdSocialMediaId(jdPromotionSite.getJdSocialMediaId());
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", BeanUtils.transform(MaterialPushChatroomTaskDto.class, chatroomTask));

        return success(jsonObject);
    }
}
