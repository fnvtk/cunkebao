package cn.myerm.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.exception.SystemException;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.service.impl.SysObjectServiceImpl;
import cn.myerm.system.entity.SysApprove;
import cn.myerm.system.entity.SysApproveConfig;
import cn.myerm.system.entity.SysApproveDetail;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysApproveMapper;
import cn.myerm.system.service.ISysApproveService;
import org.apache.poi.util.StringUtil;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@Service
public class SysApproveServiceImpl extends BusinessServiceImpl<SysApproveMapper, SysApprove> implements ISysApproveService {

    @Resource
    private SysObjectServiceImpl sysObjectService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private SysSessionServiceImpl sysSessionService;

    @Resource
    private SysApproveConfigServiceImpl sysApproveConfigService;

    @Resource
    private SysApproveConfigDetailServiceImpl sysApproveConfigDetailService;

    @Resource
    private SysUserServiceImpl sysUserService;

    @Resource
    private SysApproveDetailServiceImpl sysApproveDetailService;

    /**
     * 保存草稿
     *
     * @param sObjectName 对象名
     * @param ObjectId    数据ID
     */
    @Transactional
    public void draft(String sObjectName, String ObjectId) {
        SysObject sysObject = sysObjectService.getById(sObjectName);
        if (sysObject.getBWorkFlow()) {
            jdbcTemplate.execute("UPDATE `" + sysObject.getSDbTable() + "` SET ApproveStatusID='draft' WHERE " + sysObject.getSIdFieldAs() + "='" + ObjectId + "'");
        } else {
            throw new SystemException("该对象没有开启审批功能");
        }
    }

    /**
     * 提交审批
     *
     * @param sObjectName 对象名
     * @param ObjectId    数据ID
     */
    @Transactional
    public SysApprove submit(String sObjectName, String ObjectId) {
        SysObject sysObject = sysObjectService.getById(sObjectName);
        if (sysObject.getBWorkFlow()) {
            //当前用户
            SysUser sysCurrUser = sysSessionService.getCurrUser();

            Map<String, Object> mapObjectData = jdbcTemplate.queryForMap("SELECT " + sysObject.getSNameFieldAs() + ", OwnerId FROM `" + sysObject.getSDbTable() + "` WHERE " + sysObject.getSIdFieldAs() + "='" + ObjectId + "'");

            SysApprove sysApprove = new SysApprove();
            sysApprove.setSName(mapObjectData.get(sysObject.getSNameFieldAs()) + "");
            sysApprove.setDNewTime(LocalDateTime.now());
            sysApprove.setNewUserId(sysCurrUser.getLID());
            sysApprove.setStatusId("approving");
            sysApprove.setSObjectName(sObjectName);
            sysApprove.setObjectId(ObjectId);
            sysApprove.setLCurrIndex(0);
            save(sysApprove);

            SysApproveConfig sysApproveConfig = sysApproveConfigService.getById(sObjectName);
            List<Map<String, Object>> listApproveCfgDetail = sysApproveConfigService.queryAll(sObjectName);
            for (int i = 0; i < listApproveCfgDetail.size(); i++) {
                Set<Integer> setApproveUserId = new HashSet<>();
                Map<String, Object> mapCfgDetail = listApproveCfgDetail.get(i);
                String sApprover = (String) mapCfgDetail.get("sApprover");
                if (sApprover.equals("admin")) {
                    List<SysUser> listSysUser = sysUserService.queryByRole(2);
                    for (int j = 0; j < listSysUser.size(); j++) {
                        setApproveUserId.add(listSysUser.get(j).getLID());
                    }
                } else if (sApprover.equals("manager")) {
                    SysUser ownerUser = sysUserService.getById((String) mapObjectData.get("OwnerId"));
                    if (ownerUser.getUpId() != null) {
                        setApproveUserId.add(ownerUser.getUpId());
                    } else {//没有设置上级主管，则由管理员替代
                        List<SysUser> listSysUser = sysUserService.queryByRole(2);
                        for (int j = 0; j < listSysUser.size(); j++) {
                            setApproveUserId.add(listSysUser.get(j).getLID());
                        }
                    }
                } else if (sApprover.equals("fixed")) {
                    JSONArray arrFixedUser = (JSONArray) mapCfgDetail.get("FixedApproverUserId");
                    for (int j = 0; j < arrFixedUser.size(); j++) {
                        setApproveUserId.add(arrFixedUser.getIntValue(j));
                    }
                }

                for (Integer ApproveUserId : setApproveUserId) {
                    SysApproveDetail sysApproveDetail = new SysApproveDetail();
                    sysApproveDetail.setSName(sysApprove.getSName());
                    sysApproveDetail.setApproveId(sysApprove.getLID());
                    sysApproveDetail.setLIndex(i + 1);
                    sysApproveDetail.setStatusId("pending");
                    sysApproveDetail.setBLastApprove(false);
                    sysApproveDetail.setApproveUserId(ApproveUserId);
                    sysApproveDetail.setBJoint(((String) mapCfgDetail.get("ModeId")).equals("joint"));

                    if (listApproveCfgDetail.size() == i + 1) {
                        sysApproveDetail.setBLastApprove(true);
                    }

                    sysApproveDetailService.save(sysApproveDetail);
                }
            }

            next(sysApprove);

            return sysApprove;
        } else {
            throw new SystemException("该对象没有开启审批功能");
        }
    }

    /**
     * 移动到下一个节点
     */
    @Transactional
    public void next(SysApprove sysApprove) {
        SysObject sysObject = sysObjectService.getById(sysApprove.getSObjectName());

        QueryWrapper<SysApproveDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ApproveId", sysApprove.getLID());
        queryWrapper.eq("lIndex", sysApprove.getLCurrIndex() + 1);
        List<SysApproveDetail> sysApproveDetailList = sysApproveDetailService.list(queryWrapper);
        if (sysApproveDetailList.size() > 0) {
            Set<Integer> setApproveUserId = new HashSet<>();
            for (SysApproveDetail sysApproveDetail : sysApproveDetailList) {
                setApproveUserId.add(sysApproveDetail.getApproveUserId());
            }

            //其他待审批的节点，都设置成已过时的状态
            sysApproveDetailService.setPast(sysApprove.getLID(), sysApprove.getLCurrIndex());

            sysApprove.setCurrApproveUserId(";" + StringUtil.join(setApproveUserId.toArray(), ";") + ";");
            sysApprove.setDLastApproved(LocalDateTime.now());
            sysApprove.setLCurrIndex(sysApprove.getLCurrIndex() + 1);
            sysApprove.setStatusId("approving");
            saveOrUpdate(sysApprove);

            sysApproveDetailService.setApproveing(sysApprove.getLID(), sysApprove.getLCurrIndex());
            jdbcTemplate.execute("UPDATE `" + sysObject.getSDbTable() + "` SET ApproveStatusId='approving', ApproveId='" + sysApprove.getLID() + "' WHERE " + sysObject.getSIdFieldAs() + "='" + sysApprove.getObjectId() + "'");
        } else if (sysApprove.getLCurrIndex() == 0) {//表示没有设置任何审核人
            sysApprove.setStatusId("submitted");
            sysApprove.setCurrApproveUserId(null);
            sysApprove.setLastApproveUserId(null);
            sysApprove.setDLastApproved(LocalDateTime.now());
            saveOrUpdate(sysApprove);

            jdbcTemplate.execute("UPDATE `" + sysObject.getSDbTable() + "` SET ApproveStatusId='submitted', ApproveId='" + sysApprove.getLID() + "' WHERE " + sysObject.getSIdFieldAs() + "='" + sysApprove.getObjectId() + "'");
        } else {
            sysApprove.setStatusId("passed");
            sysApprove.setLastApproveUserId(sysApprove.getCurrApproveUserId());
            sysApprove.setCurrApproveUserId(null);
            saveOrUpdate(sysApprove);

            jdbcTemplate.execute("UPDATE `" + sysObject.getSDbTable() + "` SET ApproveStatusId='passed', ApproveId='" + sysApprove.getLID() + "' WHERE " + sysObject.getSIdFieldAs() + "='" + sysApprove.getObjectId() + "'");
        }
    }

    /**
     * 是否可以废弃
     *
     * @return
     */
    public Boolean canDeprecate(SysApprove sysApprove) {

        if (ObjectUtil.isEmpty(sysApprove)) {
            return false;
        }

        SysObject sysObject = sysObjectService.getById(sysApprove.getSObjectName());
        SysUser sysCurrUser = sysSessionService.getCurrUser();

        if (sysApprove.getStatusId().equals("deprecated")) {
            return false;
        }

        Map<String, Object> mapObjectData = jdbcTemplate.queryForMap("SELECT OwnerId FROM `" + sysObject.getSDbTable() + "` WHERE " + sysObject.getSIdFieldAs() + "='" + sysApprove.getObjectId() + "'");
        if (ObjectUtil.isEmpty(mapObjectData.get("OwnerId")) || sysCurrUser.getLID() == (int) mapObjectData.get("OwnerId") || sysCurrUser.getSysRoleId() < 3) {
            return true;
        }

        return false;
    }

    /**
     * 是否可以编辑
     *
     * @param sysApprove
     * @return Boolean
     */
    public Boolean canEdit(SysApprove sysApprove) {
        if (sysApprove.getStatusId().equals("deprecated") || sysApprove.getStatusId().equals("rejected") || sysApprove.getStatusId().equals("draft")) {
            return true;
        }

        return false;
    }

    /**
     * 是否可以审批
     *
     * @param sysApprove
     * @return
     */
    public Boolean canApprove(SysApprove sysApprove) {

        if (ObjectUtil.isEmpty(sysApprove)) {
            return false;
        }

        if (!sysApprove.getStatusId().equals("approving")) {
            return false;
        }

        if (!isCurrApprover(sysApprove)) {
            return false;
        }

        SysUser sysCurrUser = sysSessionService.getCurrUser();
        QueryWrapper<SysApproveDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ApproveId", sysApprove.getLID());
        queryWrapper.eq("lIndex", sysApprove.getLCurrIndex());
        queryWrapper.eq("StatusId", "approving");
        queryWrapper.eq("ApproveUserId", sysCurrUser.getLID());
        if (sysApproveDetailService.count(queryWrapper) > 0) {
            return true;
        }

        return false;
    }

    public Boolean canView(SysApprove sysApprove) {
        SysUser sysCurrUser = sysSessionService.getCurrUser();
        QueryWrapper<SysApproveDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ApproveId", sysApprove.getLID());
        queryWrapper.eq("ApproveUserId", sysCurrUser.getLID());

        return sysApproveDetailService.count(queryWrapper) > 0;
    }

    /**
     * 是否当前的审批人员
     *
     * @param sysApprove
     * @return
     */
    public Boolean isCurrApprover(SysApprove sysApprove) {
        SysUser sysCurrUser = sysSessionService.getCurrUser();

        return sysApprove.getCurrApproveUserId().contains(";" + sysCurrUser.getLID() + ";");
    }

    /**
     * 获取指定的审批对象
     *
     * @return
     */
    public SysApprove getApprove(String sObjectName, String objectId) {
        SysObject sysObject = sysObjectService.getById(sObjectName);

        if (!sysObject.getBWorkFlow()) {
            return null;
        }

        Map<String, Object> mapObjectData = jdbcTemplate.queryForMap("SELECT ApproveId FROM `" + sysObject.getSDbTable() + "` WHERE " + sysObject.getSIdFieldAs() + "='" + objectId + "'");
        if (ObjectUtil.isEmpty(mapObjectData) || ObjectUtil.isEmpty(mapObjectData.get("ApproveId"))) {
            return null;
        } else {
            return getById((int) mapObjectData.get("ApproveId"));
        }
    }

    /**
     * 审批通过
     *
     * @param sysApprove
     */
    @Transactional
    public void pass(SysApprove sysApprove) {
        SysUser sysCurrUser = sysSessionService.getCurrUser();

        QueryWrapper<SysApproveDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ApproveId", sysApprove.getLID());
        queryWrapper.eq("lIndex", sysApprove.getLCurrIndex());
        queryWrapper.eq("ApproveUserId", sysCurrUser.getLID());
        queryWrapper.last("LIMIT 1");

        SysApproveDetail sysApproveDetail = sysApproveDetailService.getOne(queryWrapper);
        if (!ObjectUtil.isEmpty(sysApproveDetail)) {
            sysApproveDetail.setStatusId("passed");
            sysApproveDetail.setDApproved(LocalDateTime.now());
            sysApproveDetailService.saveOrUpdate(sysApproveDetail);

            sysApprove.setDLastApproved(LocalDateTime.now());
            saveOrUpdate(sysApprove);

            if (sysApproveDetail.getBJoint()) {//不是会签，通过
                next(sysApprove);
            } else {
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("ApproveId", sysApprove.getLID());
                queryWrapper.eq("lIndex", sysApprove.getLCurrIndex());
                queryWrapper.eq("StatusId", "approving");
                List<SysApproveDetail> sysApproveDetailList = sysApproveDetailService.list(queryWrapper);
                if (ObjectUtil.isEmpty(sysApproveDetailList)) {//如果全部审核通过了
                    next(sysApprove);
                } else {
                    Set<Integer> setApproverId = new HashSet<>();
                    for (int i = 0; i < sysApproveDetailList.size(); i++) {
                        setApproverId.add(sysApproveDetailList.get(i).getApproveUserId());
                    }
                    sysApprove.setCurrApproveUserId(";" + StringUtil.join(";", setApproverId.toArray()) + ";");
                    saveOrUpdate(sysApprove);
                }
            }
        } else {
            throw new SystemException("您无权审核该节点");
        }
    }

    /**
     * 废弃
     *
     * @param sysApprove
     */
    @Transactional
    public void deprecate(SysApprove sysApprove) {
        SysObject sysObject = sysObjectService.getById(sysApprove.getSObjectName());
        SysUser sysCurrUser = sysSessionService.getCurrUser();

        Map<String, Object> mapObjectData = jdbcTemplate.queryForMap("SELECT NewUserId, ApproveId FROM `" + sysObject.getSDbTable() + "` WHERE " + sysObject.getSIdFieldAs() + "='" + sysApprove.getObjectId() + "'");
        if (!ObjectUtil.isEmpty(mapObjectData)) {
            if (canDeprecate(sysApprove)) {
                sysApprove.setStatusId("deprecated");
                sysApprove.setDLastApproved(LocalDateTime.now());
                sysApprove.setCurrApproveUserId(";" + sysCurrUser.getLID() + ";");
                saveOrUpdate(sysApprove);

                jdbcTemplate.execute("UPDATE `" + sysObject.getSDbTable() + "` SET ApproveStatusId='deprecated' WHERE " + sysObject.getSIdFieldAs() + "='" + sysApprove.getObjectId() + "'");
            } else {
                throw new SystemException("您无权废弃该对象");
            }
        }
    }

    /**
     * 拒绝驳回
     *
     * @param sysApprove
     */
    @Transactional
    public void reject(SysApprove sysApprove) {
        SysObject sysObject = sysObjectService.getById(sysApprove.getSObjectName());
        SysUser sysCurrUser = sysSessionService.getCurrUser();

        QueryWrapper<SysApproveDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ApproveId", sysApprove.getLID());
        queryWrapper.eq("lIndex", sysApprove.getLCurrIndex());
        queryWrapper.eq("ApproveUserId", sysCurrUser.getLID());
        queryWrapper.last("LIMIT 1");
        SysApproveDetail sysApproveDetail = sysApproveDetailService.getOne(queryWrapper);
        if (!ObjectUtil.isEmpty(sysApproveDetail)) {
            sysApproveDetail.setStatusId("rejected");
            sysApproveDetail.setDApproved(LocalDateTime.now());
            sysApproveDetailService.saveOrUpdate(sysApproveDetail);

            sysApprove.setDLastApproved(LocalDateTime.now());
            sysApprove.setStatusId("rejected");
            sysApprove.setCurrApproveUserId(";" + sysCurrUser.getLID() + ";");
            saveOrUpdate(sysApprove);

            jdbcTemplate.execute("UPDATE `" + sysObject.getSDbTable() + "` SET ApproveStatusId='rejected' WHERE " + sysObject.getSIdFieldAs() + "='" + sysApprove.getObjectId() + "'");
        } else {
            throw new SystemException("您无权审核该节点");
        }
    }
}
