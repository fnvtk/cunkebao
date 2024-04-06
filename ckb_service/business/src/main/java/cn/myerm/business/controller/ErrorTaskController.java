package cn.myerm.business.controller;

import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.impl.ErrorTaskServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/errortask")
public class ErrorTaskController extends BusinessController {

    private ErrorTaskServiceImpl errorTaskService;

    @Autowired
    public ErrorTaskController(ErrorTaskServiceImpl errortaskService) {
        this.businessService = errortaskService;
        this.errorTaskService = errortaskService;
    }

    /**
     * 响应
     *
     * @return MessageDTO
     */
    @PostMapping("/response")
    @OperaPermission("response")
    public MessageDTO response(@Validated ListParam listParam) {
        errorTaskService.handle(listParam, "response");
        return success("");
    }

    /**
     * 响应
     *
     * @return MessageDTO
     */
    @PostMapping("/done")
    @OperaPermission("done")
    public MessageDTO done(@Validated ListParam listParam) {
        errorTaskService.handle(listParam, "done");
        return success("");
    }

}
