package cn.myerm.business.controller;

import cn.myerm.business.dto.MaterialPushMomentTaskDto;
import cn.myerm.business.dto.PosterDto;
import cn.myerm.business.service.impl.PosterServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/poster")
public class PosterController extends BusinessController {

    private final PosterServiceImpl posterService;

    @Autowired
    public PosterController(PosterServiceImpl posterService) {
        this.businessService = posterService;
        this.posterService = posterService;
    }

    @PostMapping("/getall")
    public MessageDTO getAll() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", BeanUtils.batchTransform(PosterDto.class, posterService.getAll()));

        return success(jsonObject);
    }

    @PostMapping("/getone")
    public MessageDTO getOne(Integer id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", BeanUtils.transform(PosterDto.class, posterService.getById(id)));
        return success(jsonObject);
    }

    @PostMapping("/newpostersave")
    public MessageDTO newPosterSave(String name, Integer thumb) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", posterService.newPosterSave(name, thumb));
        return success(jsonObject);
    }
}
