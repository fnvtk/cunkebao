package cn.myerm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.service.impl.CommonServiceImpl;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.system.entity.SysDep;
import cn.myerm.system.entity.SysDep;
import cn.myerm.system.mapper.SysDepMapper;
import cn.myerm.system.service.ISysDepService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-04-05
 */
@Service
public class SysDepServiceImpl extends BusinessServiceImpl<SysDepMapper, SysDep> implements ISysDepService {
    /**
     * @param sysObject 对象
     * @param sysDep   角色
     */
    protected void afterObjectEditSave(SysObject sysObject, SysDep sysDep) {
        QueryWrapper<SysDep> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("UpId");
        List<SysDep> listDep = list(queryWrapper);
        for (int i = 0; i < listDep.size(); i++) {
            SysDep subDep = listDep.get(i);

            subDep.setSPathId("/" + subDep.getLID() + "/");
            saveOrUpdate(subDep);

            updatePathId(subDep.getLID(), subDep.getSPathId());
        }
    }

    protected void afterObjectNewSave(SysObject sysObject, SysDep sysDep) {
        afterObjectEditSave(sysObject, sysDep);
    }

    private void updatePathId(Integer UpId, String sPathId) {
        QueryWrapper<SysDep> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UpId", UpId);
        List<SysDep> listDep = list(queryWrapper);
        for (int i = 0; i < listDep.size(); i++) {
            SysDep subDep = listDep.get(i);

            subDep.setSPathId(sPathId + subDep.getLID() + "/");
            saveOrUpdate(subDep);

            updatePathId(subDep.getLID(), subDep.getSPathId());
        }
    }
}
