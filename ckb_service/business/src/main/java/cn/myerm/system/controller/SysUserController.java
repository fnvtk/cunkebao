package cn.myerm.system.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.exception.SystemException;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.ISysRoleService;
import cn.myerm.system.service.ISysSessionService;
import cn.myerm.system.service.impl.SysSessionServiceImpl;
import cn.myerm.system.service.impl.SysUserServiceImpl;
import cn.myerm.system.utils.JWTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/backend/system/sysuser")
public class SysUserController extends BusinessController {
    private static final Logger logger = LoggerFactory.getLogger(SysUserController.class);

    private final SysUserServiceImpl sysUserService;

    private final ISysSessionService sysSessionService;

    private final ISysRoleService sysRoleService;

    @Autowired
    public SysUserController(final SysUserServiceImpl sysUserService, SysSessionServiceImpl sysSessionService, ISysRoleService sysRoleService) {
        this.sysUserService = sysUserService;
        this.businessService = sysUserService;
        this.sysSessionService = sysSessionService;
        this.sysRoleService = sysRoleService;
    }

    /**
     * 业务后台登录
     *
     * @param username 用户名
     * @param password 密码
     * @return MessageDTO
     */
    @PostMapping("/login")
    public MessageDTO backendLogin(String username, String password) {
        String sToken = sysUserService.backendLogin(username, password, "backend");

        Map<String, Object> result = new HashMap<>();
        sToken = JWTUtils.getToken(sToken);
        result.put("token", sToken);
        return success(result);
    }

    /**
     * 业务后台退出
     */
    @PostMapping("/loginout")
    public MessageDTO backendLogout() {
        sysUserService.backendLogout();
        return success();
    }

    /**
     * 获取用户个人信息
     */
    @PostMapping("/profile/info")
    public MessageDTO profileInfo() {
        SysUser currUser = sysSessionService.getCurrUser();
        return success(currUser);
    }

    /**
     * 保存用户个人信息
     */
    @PostMapping("/profile/editsave")
    public MessageDTO profileInfoSave(@RequestParam(name = "name", required = true) String sName,
                                      @RequestParam(name = "mobile", required = true) String sMobile,
                                      @RequestParam(name = "email", required = true) String sEMail) {
        HashMap<String, String> mapParam = new HashMap<>();
        mapParam.put("sName", sName);
        mapParam.put("sMobile", sMobile);
        mapParam.put("sEMail", sEMail);
        sysUserService.saveProfile(mapParam);

        return success();
    }

    /**
     * 保存用户个人信息
     */
    @PostMapping("/password/editsave")
    public MessageDTO passwdSave(@RequestParam(name = "oldpassword", required = true) String sOldPass,
                                 @RequestParam(name = "newpassword", required = true) String sNewPass,
                                 @RequestParam(name = "newpassword2", required = true) String sNewPass2) {
        HashMap<String, String> mapParam = new HashMap<>();
        mapParam.put("sOldPass", sOldPass);
        mapParam.put("sNewPass", sNewPass);
        mapParam.put("sNewPass2", sNewPass2);
        sysUserService.savePasswd(mapParam);

        return success();
    }

    /**
     * 获取左边栏的菜单
     */
    @PostMapping("/sidebar")
    public MessageDTO sidebar() {
        return success(sysUserService.getSideBarCfgJson());
    }

    /**
     * 获取所有的人员
     *
     * @return
     */
    @PostMapping("/all")
    public MessageDTO all() {
        return success(sysUserService.queryAll());
    }

    @PostMapping("/token")
    public MessageDTO token() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String sToken = request.getHeader("token");

        if (sToken == null || sToken.length() == 0) {
            sToken = request.getParameter("token");
        }

        DecodedJWT decodedJWT = null;
        try {
            decodedJWT = JWTUtils.verify(sToken);
        } catch (SignatureVerificationException e) {
            throw new SystemException("无效签名");
        } catch (TokenExpiredException e) {
            throw new SystemException("token过期");
        } catch (AlgorithmMismatchException e) {
            throw new SystemException("token算法不一致");
        } catch (Exception e) {
            throw new SystemException("token无效" + sToken);
        }

        sToken = decodedJWT.getClaim("token").asString();


        HashMap<String, String> mapParam = new HashMap<>();
        mapParam.put("token", sToken);

        return success(mapParam);
    }

    @PostMapping("/user/info")
    public MessageDTO userinfo() {
        HashMap<String, Object> mapParam = new HashMap<>();

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(new JSONObject());

        mapParam.put("roles", jsonArray);
        mapParam.put("name", null);
        mapParam.put("avatar", null);
        mapParam.put("introduction", null);

        return success(mapParam);
    }
}
