package cn.myerm.business.aop;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.common.exception.SystemException;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.service.impl.SysObjectServiceImpl;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.impl.SysOperatorPermissionServiceImpl;
import cn.myerm.system.service.impl.SysSessionServiceImpl;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Configuration
@Aspect
public class OperaPermissionAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperaPermissionAspect.class);

    @Resource
    private SysSessionServiceImpl sysSessionService;

    @Resource
    private SysObjectServiceImpl sysObjectService;

    @Resource
    private SysOperatorPermissionServiceImpl sysOperatorPermissionService;

    @Before("@annotation(operaPermissionAnno)")
    public void before(JoinPoint joinPoint, OperaPermission operaPermissionAnno) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String sUri = request.getRequestURI();

        String[] arrUriPart = sUri.split("/");
        String sObjectName = arrUriPart[3] + "/" + arrUriPart[4];
        SysObject sysObject = sysObjectService.getById(sObjectName);

        if (ObjectUtil.isEmpty(sysObject)) {
            return;
        }

        SysUser currUser = sysSessionService.getCurrUser();

        //判断是否有此操作项的配置
        Boolean hasOpera = false;
        String sOperaName = "";
        if (!StrUtil.hasEmpty(sysObject.getSOperatorJson())) {
            JSONArray arrJsonOpera = JSONObject.parseArray(sysObject.getSOperatorJson());
            for (Object jsonOpera : arrJsonOpera) {
                if (((JSONObject) jsonOpera).getString("ID").equalsIgnoreCase(operaPermissionAnno.value())) {
                    hasOpera = true;
                    sOperaName = ((JSONObject) jsonOpera).getString("sName");
                    break;
                }
            }
        }

        //如果有操作权限项，判断当前用户是否有权限
        if (hasOpera) {
            try {
                sysOperatorPermissionService.hasPermission(sObjectName, operaPermissionAnno.value(), currUser);
            } catch (SystemException e) {
                throw new SystemException(e.getErrorCode(), e.getMessage().replace("{1}", sOperaName).replace("{0}", sysObject.getSName()));
            }
        }
    }
}
