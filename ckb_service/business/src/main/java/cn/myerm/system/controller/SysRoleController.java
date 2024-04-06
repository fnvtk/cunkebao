package cn.myerm.system.controller;

import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.entity.SysRole;
import cn.myerm.system.service.ISysRoleService;
import cn.myerm.system.service.impl.SysRoleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/v1/backend/system/sysrole")
public class SysRoleController extends BusinessController {
    @Resource
    private ISysRoleService sysRoleService;

    @Autowired
    public SysRoleController(final SysRoleServiceImpl sysRoleService) {
        this.sysRoleService = sysRoleService;
        this.businessService = sysRoleService;
    }

    @PostMapping("/roleview")
    public MessageDTO view(@RequestParam(required = true) Integer id) {
        SysRole sysRole = sysRoleService.getById(id);
        return this.success(sysRole);
    }
}
