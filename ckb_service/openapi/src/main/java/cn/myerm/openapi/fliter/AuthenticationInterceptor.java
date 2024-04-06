package cn.myerm.openapi.fliter;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import cn.myerm.common.utils.IPUtils;
import cn.myerm.system.entity.SysSession;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.impl.SysOperaLogServiceImpl;
import cn.myerm.system.service.impl.SysSessionServiceImpl;
import cn.myerm.system.utils.JWTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AuthenticationInterceptor implements HandlerInterceptor {
    private final static Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Autowired
    private SysSessionServiceImpl sysSessionService;

    @Autowired
    private SysOperaLogServiceImpl sysOperaLogService;

    /**
     * 前置处理器
     *
     * @param httpServletRequest  request
     * @param httpServletResponse response
     * @param object              对象
     * @return boolean
     */
    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) {

        String sToken = httpServletRequest.getHeader("token");// 从 http 请求头中取出 token

        if (sToken == null || sToken.length() == 0) {
            sToken = httpServletRequest.getParameter("token");
        }

        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }

        //如果是登录，跳过检查
        String sUri = httpServletRequest.getRequestURI();
        if (sUri.equalsIgnoreCase("/v1/backend/system/sysuser/login")) {
            return true;
        } else if (sUri.equalsIgnoreCase("/v1/objectbuilder/login")) {
            return true;
        } else if (sUri.startsWith("/v1/frontend/business/")) {//前端接口不验证
            return true;
        }

        if (sToken == null || sToken.length() == 0) {
            fail(httpServletResponse, 10001, "请传入token参数");
            return false;
        }

        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWTUtils.verify(sToken);
        } catch (TokenExpiredException e) {
            fail(httpServletResponse, 10001, "token过期");
            return false;
        } catch (Exception e) {
            fail(httpServletResponse, 10001, "token无效");
            return false;
        }

        sToken = decodedJWT.getClaim("token").asString();

        //是否登录
        SysSession sysSession = sysSessionService.getById(sToken);
        if (sysSession == null) {
            fail(httpServletResponse, 10030, "您还未登录");
            return false;
        }

        SysUser currUser = sysSession.getSysUser();
        if (currUser == null) {
            fail(httpServletResponse, 10030, "您还未登录");
            return false;
        }

        if (StrUtil.hasEmpty(sysSession.getSIP()) || !sysSession.getSIP().equals(IPUtils.getIpAddr(httpServletRequest))) {
            fail(httpServletResponse, 10031, "非法调用");
            return false;
        }

        //如果是对象管理器，操作者必须是超管角色
        if (sUri.contains("/v1/objectbuilder/") && currUser.getSysRoleId() > 1) {
            fail(httpServletResponse, 10001, "只有超管有权限操作对象管理器");
            return false;
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        //操作日志
        //sysOperaLogService.saveLog(httpServletRequest);
        sysSessionService.refresh(httpServletRequest);
    }

    private void fail(HttpServletResponse httpServletResponse, int errorCode, String message) {
        httpServletResponse.setCharacterEncoding("UTF-8");
        httpServletResponse.setContentType("application/json; charset=UTF-8");
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "DNT,X-Mx-ReqToken,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since,Cache-Control,Content-Type,Authorization,Cookie");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", errorCode);
        jsonObject.put("message", message);
        jsonObject.put("data", null);

        try {
            httpServletResponse.getWriter().print(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
