package cn.myerm.system.controller;

import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.service.ISysUserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/v1/backend/system")
public class SystemController extends BusinessController {
    @Resource
    private ISysUserService sysUserService;

    @PostMapping("/sysorg/list")
    public MessageDTO sysorglist() {
        Map result = sysUserService.sysorglist();
        return this.success(result);
    }
}
