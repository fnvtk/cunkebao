package cn.myerm.system.controller;


import cn.myerm.business.controller.BusinessController;
import cn.myerm.system.service.ISysAttachService;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mars
 * @since 2021-04-16
 */
@RestController
@RequestMapping("/system/sys-attach")
public class SysAttachController extends BusinessController {
    @Resource
    private ISysAttachService sysAttachService;

    public SysAttachController(final SysAttachServiceImpl sysAttachService) {
        this.sysAttachService = sysAttachService;
        this.businessService = sysAttachService;
    }
}
