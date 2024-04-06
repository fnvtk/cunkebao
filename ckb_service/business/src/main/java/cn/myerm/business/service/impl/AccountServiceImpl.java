package cn.myerm.business.service.impl;

import cn.myerm.business.entity.Account;
import cn.myerm.business.entity.AccountWechat;
import cn.myerm.business.mapper.AccountMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IAccountService;
import cn.myerm.business.service.IAccountWechatService;
import cn.myerm.common.exception.SystemException;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.ISysUserService;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AccountServiceImpl extends BusinessServiceImpl<AccountMapper, Account> implements IAccountService {

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private IAccountWechatService accountWechatService;

    protected void beforeObjectEditSave(SysObject sysObject, JSONObject jsonObjectData) {
        if (jsonObjectData.getIntValue("UpId") == jsonObjectData.getIntValue("lId")) {
            throw new SystemException(10001L, "不能选自己为上级客户。");
        }

        //判断负责人的手机号是否已被注册
        Account account = getById(jsonObjectData.getIntValue("lId"));
        SysUser existUser = sysUserService.getByPhone(jsonObjectData.getString("sManagerMobile"));
        if (existUser != null && !existUser.getLID().equals(account.getManagerUserId())) {
            throw new SystemException(10001L, "负责人的手机号已被使用");
        } else {
            //更新负责人的手机号
            SysUser managerUser = sysUserService.getById(account.getManagerUserId());
            managerUser.setSMobile(jsonObjectData.getString("sManagerMobile"));
            managerUser.setSLoginName(jsonObjectData.getString("sManagerMobile"));
            sysUserService.updateById(managerUser);
        }
    }

    @Transactional
    public void del(ListParam listParam) {

        //设置全部出来，不要分页
        listParam.setCanpage(0);

        //只要id和name字段
        List<String> listDispCol = new ArrayList<>();
        listDispCol.add("lId");
        listDispCol.add("sName");
        listParam.setDispcol(listDispCol);

        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        for (Map<String, Object> mapObjectData : listMapObjectData) {
            Map<String, Object> data = (Map<String, Object>) mapObjectData.get("data");
            String objectId = String.valueOf(data.get("lId"));

            UpdateWrapper<Account> objectUpdateWrapper = new UpdateWrapper<>();
            objectUpdateWrapper.set("bDel", 1);
            objectUpdateWrapper.eq("lId", objectId);
            update(objectUpdateWrapper);

            UpdateWrapper<SysUser> sysUserUpdateWrapper = new UpdateWrapper<>();
            objectUpdateWrapper.set("bActive", 0);
            objectUpdateWrapper.eq("AccountId", objectId);
            sysUserService.update(sysUserUpdateWrapper);
        }
    }

    protected void beforeObjectNewSave(SysObject sysObject, JSONObject jsonObjectData) {
        //判断负责人的手机号是否已被注册
        SysUser existUser = sysUserService.getByPhone(jsonObjectData.getString("sManagerMobile"));
        if (existUser != null) {
            throw new SystemException(10001L, "负责人的手机号已被使用");
        }
    }

    @Transactional
    protected void afterObjectNewSave(SysObject sysObject, Account account) {
        //创建客户的管理员账号
        SysUser sysUser = new SysUser();
        sysUser.setSName(account.getSManagerName());
        sysUser.setAccountId(account.getLId());
        sysUser.setDNewTime(LocalDateTime.now());
        sysUser.setDEditTime(LocalDateTime.now());
        sysUser.setSysRoleId(4);
        sysUser.setSLoginName(account.getSManagerMobile());
        sysUser.setSPassword("123456");
        sysUser.setSMobile(account.getSManagerMobile());

        sysUser.setSysSolutionId(2);
        sysUser.setBActive(1);
        sysUser.setBMain(1);//设为主账号
        sysUserService.save(sysUser);

        //绑定客户的管理员账号ID
        account.setManagerUserId(sysUser.getLID());
        updateById(account);
        updateAmt(account.getLId());
    }

    /**
     * 创建普通账户
     * @param mapParam
     */
    public void newAccountUser(Map<String, String> mapParam) {

        SysUser existUser = sysUserService.getByPhone(mapParam.get("sMobile"));
        if (existUser != null) {
            throw new SystemException(10001L, "子账号的手机号已被使用");
        }

        SysUser sysUser = new SysUser();
        sysUser.setSName(mapParam.get("sName"));
        sysUser.setAccountId(Integer.valueOf(mapParam.get("AccountId")));
        sysUser.setDNewTime(LocalDateTime.now());
        sysUser.setDEditTime(LocalDateTime.now());
        sysUser.setSysRoleId(5);
        sysUser.setSLoginName(mapParam.get("sMobile"));
        sysUser.setSPassword("123456");
        sysUser.setSMobile(mapParam.get("sMobile"));
        sysUser.setSysSolutionId(3);
        sysUser.setBActive(1);
        sysUserService.save(sysUser);

        updateAmt(Integer.parseInt(mapParam.get("AccountId")));
    }

    /**
     * 更新绑定的微信账号数量、子账号数量
     */
    public void updateAmt(int AccountId) {
        QueryWrapper<SysUser> querySysUserWrapper = new QueryWrapper<>();
        querySysUserWrapper.eq("AccountId", AccountId);
        int lUserAmt = sysUserService.count(querySysUserWrapper);

        QueryWrapper<AccountWechat> queryAccountWechatWrapper = new QueryWrapper<>();
        queryAccountWechatWrapper.eq("AccountId", AccountId);
        int lWechatAmt = accountWechatService.count(queryAccountWechatWrapper);

        UpdateWrapper<Account> updateAccountWrapper = new UpdateWrapper<>();
        updateAccountWrapper.set("lWechatAmt", lWechatAmt);
        updateAccountWrapper.set("lUserAmt", lUserAmt);
        updateAccountWrapper.eq("lId", AccountId);

        update(updateAccountWrapper);
    }

    public List<SysUser> getUserByAcc(Integer AccountId) {
        QueryWrapper<SysUser> objectQueryWrapper = new QueryWrapper<>();

        if (AccountId != null) {
            objectQueryWrapper.eq("AccountId", AccountId);
        }

        objectQueryWrapper.orderByAsc("sName");
        return sysUserService.list(objectQueryWrapper);
    }
}
