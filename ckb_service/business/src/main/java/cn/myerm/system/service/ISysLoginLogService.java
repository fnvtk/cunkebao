package cn.myerm.system.service;

import cn.myerm.business.service.IBusinessService;
import cn.myerm.system.entity.SysLoginLog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Mars
 * @since 2021-04-20
 */
public interface ISysLoginLogService extends IBusinessService<SysLoginLog> {
    public void saveLog(Integer SysUserId);
}
