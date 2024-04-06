package cn.myerm.system.controller;

import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.entity.SysDep;
import cn.myerm.system.service.ISysDepService;
import cn.myerm.system.service.impl.SysDepServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/backend/system/sysdep")
public class SysDepController extends BusinessController {
    @Resource
    private ISysDepService sysDepService;

    @Autowired
    public SysDepController(final SysDepServiceImpl sysDepService) {
        this.sysDepService = sysDepService;
        this.businessService = sysDepService;
    }

    @PostMapping("/depview")
    public MessageDTO view(@RequestParam(required = true) Integer id) {
        SysDep sysDep = sysDepService.getById(id);
        return this.success(sysDep);
    }
}
