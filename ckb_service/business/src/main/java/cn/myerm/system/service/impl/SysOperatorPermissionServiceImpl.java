package cn.myerm.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.exception.SystemException;
import cn.myerm.system.entity.SysOperatorPermission;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysOperatorPermissionMapper;
import cn.myerm.system.service.ISysOperatorPermissionService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-04-19
 */
@Service
public class SysOperatorPermissionServiceImpl extends BusinessServiceImpl<SysOperatorPermissionMapper, SysOperatorPermission> implements ISysOperatorPermissionService {
    public void hasPermission(String sObjectName, String sOperator, SysUser sysUser) {

        if (sysUser.getSysRoleId() == 1) {//超管，不用判断
            return;
        }

        //先检查人员是否有权限
        QueryWrapper<SysOperatorPermission> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.eq("TypeId", "SysUser");
        queryWrapper.eq("ObjectId", sysUser.getLID());
        queryWrapper.eq("sOperator", sOperator);
        if (getOne(queryWrapper) != null) {
            return;
        }

        //第二检查人员所在的角色是否有权限
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.eq("TypeId", "SysRole");
        queryWrapper.eq("ObjectId", sysUser.getSysRoleId());
        queryWrapper.eq("sOperator", sOperator);
        if (getOne(queryWrapper) != null) {
            return;
        }

        throw new SystemException(10009L, "对不起，您没有{0}的{1}权限");
    }

    public List<SysOperatorPermission> queryAll(String sObjectName, String objectId) {
        QueryWrapper<SysOperatorPermission> queryWrapper = new QueryWrapper<>();

        if (sObjectName.equalsIgnoreCase("system/sysuser")) {
            queryWrapper.eq("TypeId", "SysUser");
        } else {
            queryWrapper.eq("TypeId", "SysRole");
        }
        queryWrapper.eq("ObjectId", objectId);

        return list(queryWrapper);
    }

    public void updateSave(String sObjectName, String objectId, String sPermission) {

        QueryWrapper<SysOperatorPermission> queryWrapper = new QueryWrapper<>();

        if (sObjectName.equalsIgnoreCase("system/sysuser")) {
            queryWrapper.eq("TypeId", "SysUser");
        } else {
            queryWrapper.eq("TypeId", "SysRole");
        }
        queryWrapper.eq("ObjectId", objectId);
        remove(queryWrapper);

        List<SysOperatorPermission> sysOperatorPermissionList = new ArrayList<>();
        JSONObject jsonPermission = JSONObject.parseObject(sPermission);
        for (String sOperaObjectName : jsonPermission.keySet()) {
            for (String sOperator : jsonPermission.getJSONObject(sOperaObjectName).keySet()) {

                if (jsonPermission.getJSONObject(sOperaObjectName).getBoolean(sOperator)) {
                    SysOperatorPermission sysOperatorPermission = new SysOperatorPermission();
                    sysOperatorPermission.setSObjectName(sOperaObjectName);
                    sysOperatorPermission.setSOperator(sOperator);
                    sysOperatorPermission.setObjectId(objectId);

                    if (sObjectName.equalsIgnoreCase("system/sysuser")) {
                        sysOperatorPermission.setTypeId("SysUser");
                    } else {
                        sysOperatorPermission.setTypeId("SysRole");
                    }

                    sysOperatorPermissionList.add(sysOperatorPermission);
                }
            }
        }

        saveBatch(sysOperatorPermissionList);
    }
}
