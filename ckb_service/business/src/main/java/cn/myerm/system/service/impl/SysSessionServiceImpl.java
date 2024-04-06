package cn.myerm.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.config.Businessconfig;
import cn.myerm.common.exception.SystemException;
import cn.myerm.common.utils.IPUtils;
import cn.myerm.system.entity.SysSession;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysSessionMapper;
import cn.myerm.system.service.ISysSessionService;
import cn.myerm.system.utils.JWTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
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
 * @since 2021-04-05
 */
@Service
@EnableScheduling
public class SysSessionServiceImpl extends BusinessServiceImpl<SysSessionMapper, SysSession> implements ISysSessionService {

    private static final Logger logger = LoggerFactory.getLogger(SysSessionServiceImpl.class);

    @Resource
    private SysSessionMapper sysSessionMapper;

    @Resource
    private Businessconfig businessConfig;

    @Resource
    private SysUserServiceImpl sysUserService;

    public SysSession getById(String id) {
        return sysSessionMapper.getById(id);
    }

    /**
     * 获取当前登录的用户
     *
     * @return
     */
    public SysUser getCurrUser() {
        SysSession sysSession = getById(getToken());
        if (ObjectUtil.isEmpty(sysSession)) {
            throw new SystemException("找不到此用户");
        }

        return sysSession.getSysUser();
    }

    /**
     * 保存会话数据
     *
     * @param SysUserId 用户ID
     * @param sType     会话类型
     * @return String
     */
    @Transactional
    public String saveSession(Integer SysUserId, String sType, String sDevince) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String sSessionId = RandomUtil.randomString(32);

        SysSession sysSession = new SysSession();
        sysSession.setID(sSessionId);
        sysSession.setDLogin(LocalDateTime.now());
        sysSession.setDLastActivity(LocalDateTime.now());
        sysSession.setSysUserId(SysUserId);
        sysSession.setTypeId(sType);
        sysSession.setSIP(IPUtils.getIpAddr(request));
        sysSession.setSDevice(sDevince);
        save(sysSession);

        return sysSession.getID();
    }

    /**
     * 登录退出
     */
    public void loginout() {
        this.removeById(getToken());
    }

    @Scheduled(cron = "0 */10 * * * ?")
    @Transactional
    public void clear() {
        QueryWrapper<SysSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("dLastActivity", LocalDateTime.now().minusHours(1));
        queryWrapper.ne("sDevice", "frontend");//前端登录，不要清除
        remove(queryWrapper);

        logger.info("清除过期的Session");
    }

    public String getToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String sToken = request.getHeader("token");

        if (StrUtil.isEmpty(sToken)) {
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

        return decodedJWT.getClaim("token").asString();
    }


    /**
     * 刷新最近访问时间
     */
    public void refresh(HttpServletRequest request) {

        String sUri = request.getRequestURI();//当前的url
        if (sUri.equalsIgnoreCase("/v1/backend/system/sysuser/login")) {
            return;
        } else if (sUri.equalsIgnoreCase("/v1/objectbuilder/login")) {
            return;
        }

        UpdateWrapper<SysSession> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("ID", getToken());
        updateWrapper.set("dLastActivity", LocalDateTime.now());
        update(updateWrapper);
    }
}
