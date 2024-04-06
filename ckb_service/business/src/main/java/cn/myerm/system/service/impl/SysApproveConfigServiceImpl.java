package cn.myerm.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.system.entity.SysApproveConfig;
import cn.myerm.system.entity.SysApproveConfigDetail;
import cn.myerm.system.mapper.SysApproveConfigMapper;
import cn.myerm.system.service.ISysApproveConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@Service
public class SysApproveConfigServiceImpl extends BusinessServiceImpl<SysApproveConfigMapper, SysApproveConfig> implements ISysApproveConfigService {

    @Resource
    private SysApproveConfigDetailServiceImpl sysApproveConfigDetailService;

    /**
     * 新增审批流程
     *
     * @param sObjectName
     * @param sApproveFormData
     */
    @Transactional
    public void createCfgSave(String sObjectName, String sApproveFormData) {

        JSONObject jsonApproveData = JSONObject.parseObject(sApproveFormData);

        //如果主体配置数据还没，就新建
        SysApproveConfig sysApproveConfig = getById(sObjectName);
        if (ObjectUtil.isEmpty(sysApproveConfig)) {
            sysApproveConfig = new SysApproveConfig();
            sysApproveConfig.setSObjectName(sObjectName);
            save(sysApproveConfig);
        }

        //查找最后一个节点
        QueryWrapper<SysApproveConfigDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.orderByDesc("lIndex");
        queryWrapper.last("limit 1");
        SysApproveConfigDetail lastConfigDetail = sysApproveConfigDetailService.getOne(queryWrapper);

        //新建节点数据
        SysApproveConfigDetail sysApproveConfigDetail = new SysApproveConfigDetail();
        sysApproveConfigDetail.setSObjectName(sObjectName);
        sysApproveConfigDetail.setSApprover(jsonApproveData.getString("sApprover"));

        if (jsonApproveData.getBoolean("isJoint")) {
            sysApproveConfigDetail.setModeId("joint");
        } else {
            sysApproveConfigDetail.setModeId("single");
        }

        if (ObjectUtil.isEmpty(lastConfigDetail)) {
            sysApproveConfigDetail.setLIndex(0);
        } else {
            sysApproveConfigDetail.setLIndex(lastConfigDetail.getLIndex() + 1);
        }

        sysApproveConfigDetail.setFixedApproverUserId(jsonApproveData.getJSONArray("FixedApproverUserId").toJSONString());
        sysApproveConfigDetailService.save(sysApproveConfigDetail);
        updateIndex(sObjectName);
    }

    /**
     * 编辑流程
     *
     * @param sObjectName
     * @param sApproveFormData
     */
    @Transactional
    public void editCfgSave(String sObjectName, String sApproveFormData) {
        JSONObject jsonApproveData = JSONObject.parseObject(sApproveFormData);
        SysApproveConfigDetail sysApproveConfigDetail = sysApproveConfigDetailService.getById(jsonApproveData.getInteger("lID"));
        sysApproveConfigDetail.setSApprover(jsonApproveData.getString("sApprover"));
        sysApproveConfigDetail.setFixedApproverUserId(jsonApproveData.getJSONArray("FixedApproverUserId").toJSONString());

        sysApproveConfigDetail.setModeId("single");
        if (jsonApproveData.getBoolean("isJoint")) {
            sysApproveConfigDetail.setModeId("joint");
        }

        sysApproveConfigDetailService.saveOrUpdate(sysApproveConfigDetail);
        updateIndex(sObjectName);
    }

    public void updateIndex(String sObjectName) {
        QueryWrapper<SysApproveConfigDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.orderByAsc("lIndex");
        List<SysApproveConfigDetail> sysApproveConfigDetailList = sysApproveConfigDetailService.list(queryWrapper);

        SysApproveConfigDetail sysApproveConfigDetail = null;
        for (int i = 0; i < sysApproveConfigDetailList.size(); i++) {
            sysApproveConfigDetail = sysApproveConfigDetailList.get(i);
            sysApproveConfigDetail.setLIndex(i);
            sysApproveConfigDetailService.saveOrUpdate(sysApproveConfigDetail);
        }
    }

    /**
     * 删除流程
     */
    @Transactional
    public void removeCfg(Integer lID) {
        sysApproveConfigDetailService.removeById(lID);
    }

    /**
     * 查询某个对象的所有节点
     *
     * @param sObjectName
     * @return
     */
    public List<Map<String, Object>> queryAll(String sObjectName) {
        QueryWrapper<SysApproveConfigDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.orderByAsc("lIndex");
        List<Map<String, Object>> listMaps = sysApproveConfigDetailService.listMaps(queryWrapper);

        for (int i = 0; i < listMaps.size(); i++) {
            String sFixedApproverUserId = (String) listMaps.get(i).get("FixedApproverUserId");
            listMaps.get(i).put("FixedApproverUserId", JSONArray.parseArray(sFixedApproverUserId));
        }

        return listMaps;
    }
}
