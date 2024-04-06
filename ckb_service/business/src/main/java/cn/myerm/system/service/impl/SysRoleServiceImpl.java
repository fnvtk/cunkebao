package cn.myerm.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.exception.SystemException;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.system.entity.SysRole;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysRoleMapper;
import cn.myerm.system.service.ISysRoleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-04-05
 */
@Service
public class SysRoleServiceImpl extends BusinessServiceImpl<SysRoleMapper, SysRole> implements ISysRoleService {

    @Resource
    private SysUserServiceImpl sysUserService;

    /**
     * @param sysObject 对象
     * @param sysRole   角色
     */
    protected void afterObjectEditSave(SysObject sysObject, SysRole sysRole) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNull("UpId");
        List<SysRole> listRole = list(queryWrapper);
        for (int i = 0; i < listRole.size(); i++) {
            SysRole subRole = listRole.get(i);

            subRole.setSPathId("/" + subRole.getLID() + "/");
            saveOrUpdate(subRole);

            updatePathId(subRole.getLID(), subRole.getSPathId());
        }
    }

    protected void afterObjectNewSave(SysObject sysObject, SysRole sysRole) {
        afterObjectEditSave(sysObject, sysRole);
    }

    private void updatePathId(Integer UpId, String sPathId) {
        QueryWrapper<SysRole> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UpId", UpId);
        List<SysRole> listRole = list(queryWrapper);
        for (int i = 0; i < listRole.size(); i++) {
            SysRole subRole = listRole.get(i);

            subRole.setSPathId(sPathId + subRole.getLID() + "/");
            saveOrUpdate(subRole);

            updatePathId(subRole.getLID(), subRole.getSPathId());
        }
    }

    protected void beforeDel(SysObject sysObject, String objectId) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SysRoleId", objectId);
        if (sysUserService.count(queryWrapper) > 0) {
            SysRole sysRole = getById(objectId);
            throw new SystemException(sysRole.getSName() + "之下还有人员，不能被删除。");
        }
    }

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();
        btn.put("ID", "new");
        btn.put("sName", "新建");
        btn.put("handler", "handleNew");
        btn.put("icon", "el-icon-plus");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "del");
        btn.put("sName", "删除");
        btn.put("handler", "handleDel");
        btn.put("icon", "el-icon-delete");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "opera");
        btn.put("sName", "操作权限");
        btn.put("handler", "handleOpera");
        btn.put("icon", "el-icon-s-operation");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "refresh");
        btn.put("sName", "刷新");
        btn.put("handler", "handleRefresh");
        btn.put("icon", "el-icon-refresh");
        arrBtn.add(btn);

        return arrBtn;
    }
}
