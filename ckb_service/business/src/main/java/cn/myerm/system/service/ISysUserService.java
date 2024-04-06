package cn.myerm.system.service;

import cn.myerm.business.service.IBusinessService;
import cn.myerm.system.entity.SysRole;
import cn.myerm.system.entity.SysUser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Mars
 * @since 2021-04-05
 */
public interface ISysUserService extends IBusinessService<SysUser> {
    Map<String, Object> login(String username, String password);

    String backendLogin(String username, String password, String devince);

    void backendLogout();

    Map<String, Object> sysorglist();

    List<SysUser> getDownlineSysUser(SysUser upSysUser);

    List<SysUser> getDownlineSysUser(SysRole sysRole);

    Map<String, Object> getSideBarCfgJson();

    void saveProfile(HashMap<String, String> mapParam);

    void savePasswd(HashMap<String, String> mapParam);

    SysUser getByPhone(String sPhone);
}
