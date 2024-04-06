package cn.myerm.system.service.impl;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.utils.IPUtils;
import cn.myerm.system.entity.SysLoginLog;
import cn.myerm.system.mapper.SysLoginLogMapper;
import cn.myerm.system.service.ISysLoginLogService;
import cn.myerm.system.service.ISysSessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-04-20
 */
@Service
public class SysLoginLogServiceImpl extends BusinessServiceImpl<SysLoginLogMapper, SysLoginLog> implements ISysLoginLogService {

    @Resource
    private ISysSessionService sysSessionService;

    @Transactional
    public void saveLog(Integer SysUserId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        SysLoginLog sysLoginLog = new SysLoginLog();
        sysLoginLog.setSIP(IPUtils.getIpAddr(request));
        sysLoginLog.setSName(sysLoginLog.getSIP());
        sysLoginLog.setSysUserId(SysUserId);
        sysLoginLog.setDNewTime(LocalDateTime.now());

        try {
            UserAgent userAgent = UserAgentUtil.parse(request.getHeader("User-Agent"));
            sysLoginLog.setSDevice(userAgent.getBrowser().toString() + "/" + userAgent.getVersion().toString());
        } catch (NullPointerException e) {

        }

        save(sysLoginLog);
    }
}
