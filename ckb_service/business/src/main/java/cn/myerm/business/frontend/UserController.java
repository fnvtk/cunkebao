package cn.myerm.business.frontend;

import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.impl.SysUserServiceImpl;
import cn.myerm.system.utils.JWTUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/frontend/business/user")
public class UserController extends BusinessController {
    private final SysUserServiceImpl sysUserService;

    public UserController(SysUserServiceImpl sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/login")
    public MessageDTO login(String username, String password) {
        String sToken = sysUserService.frontendLogin(username, password, "frontend");

        Map<String, Object> result = new HashMap<>();
        sToken = JWTUtils.getToken(sToken);
        result.put("token", sToken);
        return success(result);
    }
}