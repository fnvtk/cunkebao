package cn.myerm.business.controller;

import cn.myerm.business.service.impl.MiniProgrameLibServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/miniprogramelib")
public class MiniProgrameLibController extends BusinessController {

    private final MiniProgrameLibServiceImpl miniprogramelibService;

    @Autowired
    public MiniProgrameLibController(MiniProgrameLibServiceImpl miniprogramelibService) {
        this.businessService = miniprogramelibService;
        this.miniprogramelibService = miniprogramelibService;
    }

    @PostMapping("/add")
    public MessageDTO add(@RequestParam(name = "lId", required = true) Integer lId,
                          @RequestParam(name = "sName", required = true) String sName) {
        miniprogramelibService.add(lId, sName);
        return success();
    }
}
