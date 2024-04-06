package cn.myerm.system.controller;

import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.service.impl.SysUserServiceImpl;
import cn.myerm.system.utils.JWTUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/objectbuilder")
public class ObjectbuilderUserController extends CommonController {

    private final SysUserServiceImpl sysUserService;

    @Autowired
    public ObjectbuilderUserController(SysUserServiceImpl sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/login")
    public MessageDTO login(String username, String password) {
        Map result = sysUserService.login(username, password);
        String token = (String) result.get("token");
        String stoken = JWTUtils.getToken(token);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", stoken);
        return success(resultMap);
    }
}
