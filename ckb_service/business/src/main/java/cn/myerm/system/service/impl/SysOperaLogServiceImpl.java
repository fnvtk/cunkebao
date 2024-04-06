package cn.myerm.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.utils.IPUtils;
import cn.myerm.system.entity.SysOperaLog;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysOperaLogMapper;
import cn.myerm.system.service.ISysOperaLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Enumeration;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-04-20
 */
@Service
public class SysOperaLogServiceImpl extends BusinessServiceImpl<SysOperaLogMapper, SysOperaLog> implements ISysOperaLogService {

    private final static Logger logger = LoggerFactory.getLogger(SysOperaLogServiceImpl.class);

    @Autowired
    private SysSessionServiceImpl sysSessionService;

    /**
     * 保存操作日志
     *
     * @param request
     */
    public void saveLog(HttpServletRequest request) {

        String sUri = request.getRequestURI();//当前的url
        if (sUri.equalsIgnoreCase("/v1/backend/system/sysuser/login")) {
            return;
        } else if (sUri.equalsIgnoreCase("/v1/objectbuilder/login")) {
            return;
        }

        logger.info(sUri);
        SysUser currUser = sysSessionService.getCurrUser();//当前用户

        String[] arrUriPart = sUri.split("/");
        String sObjectName = arrUriPart[3] + "/" + arrUriPart[4];
        String sAction = arrUriPart[arrUriPart.length - 1];

        SysOperaLog sysOperaLog = new SysOperaLog();
        sysOperaLog.setSysUserId(currUser.getLID());
        sysOperaLog.setDNewTime(LocalDateTime.now());
        sysOperaLog.setSAction(sAction);
        sysOperaLog.setSObjectName(sObjectName);
        sysOperaLog.setSIP(IPUtils.getIpAddr(request));
        sysOperaLog.setSUri(sUri);

        JSONObject jsonParam = new JSONObject();
        Enumeration pNames = request.getParameterNames();//获取具有name属性的控件/对象
        while (pNames.hasMoreElements()) {                          //遍历该控件/对象
            String name = (String) pNames.nextElement();         //获得第 i 个控件/对象的name属性值，element是pNames对象的name属性值，
            String value = request.getParameter(name);         //根据name属性值获取该控件的value值，与前端类似。
            jsonParam.put(name, value);
        }
        sysOperaLog.setSParamJson(jsonParam.toJSONString());

        JSONObject jsonHeader = new JSONObject();
        Enumeration enumHeader = request.getHeaderNames();
        while (enumHeader.hasMoreElements()) {
            String sHeaderName = (String) enumHeader.nextElement();
            String sHaderValue = request.getHeader(sHeaderName);
            jsonHeader.put(sHeaderName, sHaderValue);
        }
        sysOperaLog.setSHeaderJson(jsonHeader.toJSONString());

//        try {
//            UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
//            sysOperaLog.setSDevice(userAgent.getBrowser().toString() + "/" + userAgent.getVersion().toString());
//        } catch (NullPointerException e) {
//
//        }
//       // sysOperaLog.setSRespone(sResult);
        save(sysOperaLog);
    }
}
