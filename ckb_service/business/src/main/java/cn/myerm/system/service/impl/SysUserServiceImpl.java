package cn.myerm.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.exception.SystemException;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.system.entity.SysRole;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysUserMapper;
import cn.myerm.system.service.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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
public class SysUserServiceImpl extends BusinessServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    private static final int SUPERADMINROLE = 1;
    @Resource
    ISysSessionService sessionService;
    @Resource
    ISysDepService depService;
    @Resource
    ISysRoleService roleService;
    @Resource
    ISysLoginLogService sysLoginLogService;
    @Resource
    SysSolutionServiceImpl sysSolutionService;

    @Override
    public Map<String, Object> login(String username, String password) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sLoginName", username);

        SysUser sysUser = this.getOne(queryWrapper);
        String id = null;
        if (ObjectUtil.isEmpty(sysUser)) {
            this.loginfailhandler();
        } else {
            String sPassword = sysUser.getSPassword();
            if (!password.equals(sPassword)) {
                this.loginfailhandler();
            }

            Integer roleId = sysUser.getSysRoleId();
            if (roleId != SUPERADMINROLE) {
                throw new SystemException(10003l, "非超级管理员不能登录");
            }

            //保存会话
            id = sessionService.saveSession(sysUser.getLID(), "Objectbuilder", "Objectbuilder");
            //登录日志
            sysLoginLogService.saveLog(sysUser.getLID());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", id);
        return result;
    }

    public void loginfailhandler() {
        try {
            Thread.sleep(3 * 1000);
        } catch (InterruptedException e) {
            throw new SystemException("系统异常");
        } finally {
            throw new SystemException(10003l, "用户名或密码不匹配");
        }
    }

    /**
     * 前端登录
     * @param sUserName
     * @param sPassword
     * @return
     */
    public String frontendLogin(String sUserName, String sPassword, String sDevince) {
        return backendLogin(sUserName, sPassword, sDevince);
    }

    /**
     * 业务后台登录
     *
     * @param sUserName 用户名
     * @param sPassword 密码
     * @return
     */
    public String backendLogin(String sUserName, String sPassword, String sDevince) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sLoginName", sUserName);

        SysUser sysUser = this.getOne(queryWrapper);
        if (ObjectUtil.isEmpty(sysUser)) {
            throw new SystemException(10003L, "用户名或密码错误");
        } else if (!sysUser.getSPassword().equals(sPassword)) {
            throw new SystemException(10003L, "用户名或密码错误");
        }

        //保存会话
        String sToken = sessionService.saveSession(sysUser.getLID(), "Business", sDevince);

        //登录日志
        sysLoginLogService.saveLog(sysUser.getLID());

        return sToken;
    }

    /**
     * 业务后台退出
     */
    public void backendLogout() {
        sessionService.loginout();
    }

    @Override
    public Map<String, Object> sysorglist() {
        Map<String, Object> result = new HashMap<>();
        result.put("sysusers", this.list());
        result.put("sysdeps", depService.list());
        result.put("sysroles", roleService.list());

        return result;
    }

    /**
     * 获取指定用户下的所有下级用户
     *
     * @param upSysUser
     * @return
     */
    public List<SysUser> getDownlineSysUser(SysUser upSysUser) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();

        queryWrapper.select("lID", "sName");
        queryWrapper.likeRight("sPathId", upSysUser.getSPathId());
        return list(queryWrapper);
    }

    /**
     * 获取所有下级角色的用户
     *
     * @param sysRole
     * @return
     */
    public List<SysUser> getDownlineSysUser(SysRole sysRole) {
        String sPathId = sysRole.getSPathId();
        if (StrUtil.isEmpty(sPathId)) {
            return null;
        }
        QueryWrapper<SysRole> roleQueryWrapper = new QueryWrapper<>();
        roleQueryWrapper.likeRight("sPathId", sPathId);
        List<SysRole> listDownlineSysRole = roleService.list(roleQueryWrapper);

        List<Integer> listSysRoleId = new ArrayList<>();
        for (SysRole downlineSysRole : listDownlineSysRole) {
            listSysRoleId.add(downlineSysRole.getLID());
        }

        QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("SysRoleId", listSysRoleId);

        return list(userQueryWrapper);
    }

    /**
     * 获取菜单栏的配置信息
     */
    public Map<String, Object> getSideBarCfgJson() {
        //首先获取当前的用户
        SysUser currUser = getCurrUser();

        Map<String, Object> mapResult = new HashMap<>();
        if (currUser.getSysSolutionId() != null) {//如果用户已经设置了工作台方案，则优先取它。
            mapResult = sysSolutionService.getDetailData(currUser.getSysSolutionId());
        } else {
            SysRole sysRole = roleService.getById(currUser.getSysRoleId());
            mapResult = sysSolutionService.getDetailData(sysRole.getSysSolutionId());
        }

        return mapResult;
    }

    protected void fieldValidate(SysField sysField, String sFieldValue, String ObjectId) throws Exception {
        if (sysField.getSFieldAs().equals("sLoginName")) {
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sLoginName", sFieldValue);

            if (!StrUtil.hasEmpty(ObjectId)) {
                queryWrapper.ne("lID", ObjectId);
            }

            if (count(queryWrapper) > 0) {
                throw new Exception("登录名不能重复");
            }
        } else if (sysField.getSFieldAs().equals("sMobile")) {
            QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sMobile", sFieldValue);

            if (!StrUtil.hasEmpty(ObjectId)) {
                queryWrapper.ne("lID", ObjectId);
            }

            if (count(queryWrapper) > 0) {
                throw new Exception("手机号已被使用");
            }
        }
    }

    /**
     * 保存个人信息
     */
    @Transactional
    public void saveProfile(HashMap<String, String> mapParam) {
        SysUser currUser = sessionService.getCurrUser();
        currUser.setSName(mapParam.get("sName"));
        currUser.setSEMail(mapParam.get("sEMail"));
        currUser.setSMobile(mapParam.get("sMobile"));

        saveOrUpdate(currUser);
    }

    /**
     * 保存密码
     */
    @Transactional
    public void savePasswd(HashMap<String, String> mapParam) {
        SysUser currUser = getCurrUser();

        if (!currUser.getSPassword().equals(mapParam.get("sOldPass"))) {
            throw new SystemException("当前密码不匹配");
        }

        if (!mapParam.get("sNewPass").equals(mapParam.get("sNewPass2"))) {
            throw new SystemException("两次新密码输入不一致");
        }

        currUser.setSPassword(mapParam.get("sNewPass"));

        saveOrUpdate(currUser);
    }

    /**
     * 新建对象后的处理
     *
     * @param sysObject 对象
     * @param sysUser   对象实体
     */
    protected void afterObjectNewSave(SysObject sysObject, SysUser sysUser) {
        if (sysUser.getUpId() != null && sysUser.getUpId() > 0) {
            SysUser upUser = getById(sysUser.getUpId());
            sysUser.setSPathId(upUser.getSPathId() + sysUser.getLID() + "/");
        } else {
            sysUser.setSPathId("/" + sysUser.getLID() + "/");
        }

        SysUser currUser = getCurrUser();
        if (currUser.getAccountId() != null) {
            sysUser.setSysSolutionId(4);
            sysUser.setBActive(1);
            sysUser.setSysRoleId(5);
            sysUser.setAccountId(currUser.getAccountId());
        }

        saveOrUpdate(sysUser);

        super.afterObjectNewSave(sysObject, sysUser);
    }

    protected void beforeObjectEditSave(SysObject sysObject, JSONObject jsonObjectData) {
        String objectId = jsonObjectData.getString(sysObject.getSIdFieldAs());
        SysUser sysUser = getById(objectId);

        if (jsonObjectData.getString("UpId") != null && !sysUser.getUpId().toString().equals(jsonObjectData.getString("UpId"))) {
            if (StrUtil.isNotEmpty(jsonObjectData.getString("UpId"))) {
                SysUser upUser = getById(jsonObjectData.getString("UpId"));
                jsonObjectData.put("sPathId", upUser.getSPathId() + objectId + "/");
            } else {
                jsonObjectData.put("UpId", null);
                jsonObjectData.put("sPathId", "/" + objectId + "/");
            }

            updatePathID(objectId, jsonObjectData.getString("sPathId"));
        } else {
            jsonObjectData.put("sPathId", "/" + objectId + "/");
        }

        SysUser currUser = getCurrUser();
        if (currUser.getAccountId() != null) {//客户的主账号操作
            //非主账号不能操作
            if (!currUser.getBMain().equals(1) && (currUser.getSysRoleId() == 4 || currUser.getSysRoleId() == 5)) {
                throw new SystemException("只有主账号才能编辑账号");
            }

            SysUser existUser = getByPhone(jsonObjectData.getString("sMobile"));
            if (existUser != null && !existUser.getLID().equals(jsonObjectData.getIntValue("lID"))) {
                throw new SystemException(10001L, "手机号已被使用");
            }

            existUser = getByPhone(jsonObjectData.getString("sLoginName"));
            if (existUser != null && !existUser.getLID().equals(jsonObjectData.getIntValue("lID"))) {
                throw new SystemException(10001L, "登录用户名已被使用");
            }
        }


        super.beforeObjectEditSave(sysObject, jsonObjectData);
    }

    @Transactional
    public void updatePathID(String objectId, String sPathId) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("UpId", objectId);
        List<SysUser> listUser = list(queryWrapper);

        for (int i = 0; i < listUser.size(); i++) {
            SysUser subUser = listUser.get(i);

            subUser.setSPathId(sPathId + subUser.getLID() + "/");
            saveOrUpdate(subUser);

            updatePathID(objectId, subUser.getSPathId());
        }
    }

    /**
     * 获取所有的人员
     *
     * @return
     */
    public List<SysUser> queryAll() {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bActive", 1);

        return list(queryWrapper);
    }

    public List<SysUser> queryByRole(int RoleId) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("SysRoleId", RoleId);
        queryWrapper.eq("bActive", 1);

        return list(queryWrapper);
    }

    /**
     * 获取视图列表的按钮
     *
     * @return JSONArray
     */
    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        if (mapParam.get("SysRelatedListId") != null && mapParam.get("SysRelatedListId").equals("1")) {//在客户管理里创建子账号
            //判断当前的账号是否该公司的
            JSONObject btn = new JSONObject();
            btn.put("ID", "new");
            btn.put("sName", "创建子账号");
            btn.put("handler", "handleNewAccountUser");
            btn.put("icon", "el-icon-plus");
            arrBtn.add(btn);
        } else {
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
        }

        return arrBtn;
    }

    /**
     * 通过手机号获得人员
     *
     * @param sPhone
     * @return
     */
    public SysUser getByPhone(String sPhone) {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sMobile", sPhone);

        SysUser sysUser = getOne(queryWrapper);
        if (sysUser != null) {
            return sysUser;
        }

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sLoginName", sPhone);
        return getOne(queryWrapper);
    }

    /**
     * 拼接sql，二次开发
     *
     * @param listParam 客户端传值
     * @return
     */
    public String appendWhereSql(ListParam listParam) {

        if (listParam.getListid() == 1) {
            return "AccountId='" + getCurrUser().getAccountId() + "'";
        }

        return null;
    }

    protected void beforeDel(SysObject sysObject, String objectId) {
        SysUser currUser = getCurrUser();
        if (currUser.getAccountId() != null) {//客户的主账号操作
            SysUser delUser = getById(objectId);
            if (delUser.getBMain() != null && delUser.getBMain().equals(1)) {
                throw new SystemException(10001L, "不能删除主账号");
            }
        }
    }
}
