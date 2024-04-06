package cn.myerm.system.service;

import cn.myerm.business.service.IBusinessService;
import cn.myerm.common.service.ICommonService;
import cn.myerm.system.entity.SysSession;
import cn.myerm.system.entity.SysUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Mars
 * @since 2021-04-05
 */
public interface ISysSessionService extends IBusinessService<SysSession> {
    public String saveSession(Integer SysUserId, String sType, String sDevince);
    public void loginout();
    public SysUser getCurrUser();
}
