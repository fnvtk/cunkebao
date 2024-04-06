package cn.myerm.objectbuilder.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.common.exception.SystemException;
import cn.myerm.common.service.impl.CommonServiceImpl;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.entity.SysList;
import cn.myerm.objectbuilder.mapper.SysListMapper;
import cn.myerm.objectbuilder.params.ListParams;
import cn.myerm.objectbuilder.service.ISysFieldService;
import cn.myerm.objectbuilder.service.ISysListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
 * @since 2021-04-09
 */
@Service
public class SysListServiceImpl extends CommonServiceImpl<SysListMapper, SysList> implements ISysListService {
    @Autowired
    ISysFieldService sysFieldService;

    @Override
    public List<Map<String, Object>> sysListList(String sObjectName) {
        QueryWrapper<SysList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.orderByAsc("lID");

        List<SysList> sysLists = this.list(queryWrapper);

        List<Map<String, Object>> resultList = new ArrayList<>();

        Map<String, Object> result = null;
        for (SysList sysList : sysLists) {
            result = new HashMap<>();
            result.put("lID", sysList.getLID());
            result.put("sName", sysList.getSName());
            result.put("sObjectName", sysList.getSObjectName());
            result.put("TypeId", sysList.getTypeId());
            result.put("sWhereSql", sysList.getSWhereSql());
            result.put("sOrderBySql", sysList.getSOrderBySql());
            result.put("sGroupBySql", sysList.getSGroupBySql());
            result.put("bActive", sysList.getBActive());
            result.put("bDefault", sysList.getBDefault());
            result.put("bCanPage", sysList.getBCanPage());
            result.put("bCanBat", sysList.getBCanBat());
            result.put("bSingle", sysList.getBSingle());
            result.put("lPageLimit", sysList.getLPageLimit());

            List<List<Object>> permissionList = new ArrayList<>();

            List<Object> userList = new ArrayList<>();
            userList.add("sysusers");
            userList.add(1);

            List<Object> roleList = new ArrayList<>();
            roleList.add("sysuroles");
            roleList.add(1);

            List<Object> depList = new ArrayList<>();
            depList.add("sysdeps");
            depList.add(1);

            permissionList.add(userList);
            permissionList.add(roleList);
            permissionList.add(depList);

            result.put("permissionList", permissionList);

            String sPermissionJson = sysList.getSPermissionJson();
            if (StrUtil.isNotEmpty(sPermissionJson)) {
                com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(sPermissionJson);
                result.put("sPermissionJson", jsonObject);
            } else {
                com.alibaba.fastjson.JSONObject jsonObject = new JSONObject();
                jsonObject.put("sysusers", new ArrayList<>());
                jsonObject.put("sysuroles", new ArrayList<>());
                jsonObject.put("sysdeps", new ArrayList<>());
                result.put("sPermissionJson", jsonObject);
            }

            String sListSelectId = sysList.getSListSelectId();
            String sAdvancedSearchId = sysList.getSAdvancedSearchId();
            String sFastSearchId = sysList.getSFastSearchId();

            List<SysField> selectId = new ArrayList<>();
            List<SysField> advancedId = new ArrayList<>();
            List<SysField> fastSearchId = new ArrayList<>();

            if (StrUtil.isNotEmpty(sListSelectId)) {
                QueryWrapper<SysField> sysListQueryWrapper = new QueryWrapper<>();
                if (sListSelectId.contains(",")) {
                    sysListQueryWrapper.in("lID", sListSelectId.split(","));
                } else {
                    sysListQueryWrapper.eq("lID", sListSelectId);
                }

                sysListQueryWrapper.last("ORDER BY FIELD(lID, " + sListSelectId + ")");

                selectId = sysFieldService.list(sysListQueryWrapper);
            }

            if (StrUtil.isNotEmpty(sAdvancedSearchId)) {
                QueryWrapper<SysField> sysListQueryWrapper = new QueryWrapper<>();
                if (sAdvancedSearchId.contains(",")) {
                    sysListQueryWrapper.in("lID", sAdvancedSearchId.split(","));
                } else {
                    sysListQueryWrapper.eq("lID", sAdvancedSearchId);
                }

                advancedId = sysFieldService.list(sysListQueryWrapper);
            }

            if (StrUtil.isNotEmpty(sFastSearchId)) {
                QueryWrapper<SysField> sysListQueryWrapper = new QueryWrapper<>();
                if (sFastSearchId.contains(",")) {
                    sysListQueryWrapper.in("lID", sFastSearchId.split(","));
                } else {
                    sysListQueryWrapper.eq("lID", sFastSearchId);
                }

                fastSearchId = sysFieldService.list(sysListQueryWrapper);
            }

            result.put("sListSelectId", selectId);
            result.put("sAdvancedSearchId", advancedId);
            result.put("sFastSearchId", fastSearchId);

            resultList.add(result);
        }

        return resultList;
    }

    @Transactional
    @Override
    public boolean newsave(ListParams listParams) {
        SysList sysList = new SysList();
        sysList.setSName(listParams.getSname());
        sysList.setSObjectName(listParams.getSobjectname());
        String sfastSearchId = listParams.getSfastsearchid();

        sysList.setSFastSearchId(sfastSearchId);
        sysList.setSAdvancedSearchId(listParams.getSadvancedsearchid());
        sysList.setSListSelectId(listParams.getSlistselectid());
        sysList.setBActive(listParams.getBactive());
        sysList.setBCanBat(listParams.getBcanbat());
        sysList.setBCanPage(listParams.getBcanpage());
        sysList.setBDefault(listParams.getBdefault());
        sysList.setBSingle(listParams.getBsingle());
        sysList.setLPageLimit(listParams.getLpagelimit());
        sysList.setSGroupBySql(listParams.getSgroupbysql());
        sysList.setSOrderBySql(listParams.getSordersql());
        sysList.setSWhereSql(listParams.getSwheresql());
        sysList.setTypeId(listParams.getTypeid());
        String spermissionjson = listParams.getSpermissionjson();

        try {
            JSONObject permissionJson = JSONObject.parseObject(spermissionjson);
        } catch (JSONException e) {
            throw new SystemException(e.getMessage());
        }

//        if (StrUtil.isNotEmpty(spermissionjson)) {
//            int start = spermissionjson.indexOf("{");
//            int end = spermissionjson.indexOf("}");
//            spermissionjson = spermissionjson.substring(start, end + 1);
//        }
        sysList.setSPermissionJson(spermissionjson);

        boolean exist = this.isExist(listParams.getSobjectname(), listParams.getSname());
        if (exist) {
            throw new SystemException("此视图已存在");
        }
        return this.save(sysList);
    }

    @Transactional
    @Override
    public boolean editsave(ListParams listParams) {
        SysList sysList = this.getById(listParams.getId());

        sysList.setLID(listParams.getId());
        sysList.setSName(listParams.getSname());
        sysList.setSObjectName(listParams.getSobjectname());
        String sfastSearchId = listParams.getSfastsearchid();

        if (ObjectUtil.isNull(sfastSearchId)) {
            sfastSearchId = "";
        }
        sysList.setSFastSearchId(sfastSearchId);
        sysList.setSAdvancedSearchId(listParams.getSadvancedsearchid());
        //sysList.setSListSelectId(listParams.getSlistselectid());
        sysList.setBActive(listParams.getBactive());
        sysList.setBCanBat(listParams.getBcanbat());
        sysList.setBCanPage(listParams.getBcanpage());
        sysList.setBDefault(listParams.getBdefault());
        sysList.setBSingle(listParams.getBsingle());
        sysList.setLPageLimit(listParams.getLpagelimit());
        sysList.setSGroupBySql(listParams.getSgroupbysql());
        sysList.setSOrderBySql(listParams.getSordersql());
        sysList.setSWhereSql(listParams.getSwheresql());
        sysList.setTypeId(listParams.getTypeid());

        String spermissionjson = listParams.getSpermissionjson();
        try {
            JSONObject permissionJson = JSONObject.parseObject(spermissionjson);
        } catch (JSONException e) {
            System.out.println(spermissionjson);
            throw new SystemException(e.getMessage());
        }
//        if (spermissionjson.equals("[]")) {
//            Map<String, Object> permissionMap = new HashMap<>();
//            List<Object> userList = new ArrayList<>();
//            permissionMap.put("sysusers", userList);
//
//            List<Object> roleList = new ArrayList<>();
//            permissionMap.put("sysuroles", roleList);
//
//            List<Object> depList = new ArrayList<>();
//            permissionMap.put("sysdeps", depList);
//
//            spermissionjson = JSONObject.toJSONString(permissionMap);
//        }
        sysList.setSPermissionJson(spermissionjson);

        return this.saveOrUpdate(sysList);
    }

    @Override
    public boolean del(Integer id) {
        return this.removeById(id);
    }

    public void clone(Integer id) {
        SysList sysList = getById(id);
        save(sysList);
    }

    public boolean isExist(String objectname, String name) {
        QueryWrapper<SysList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", objectname);
        queryWrapper.eq("sName", name);

        List<SysList> list = this.list(queryWrapper);
        boolean exist = false;
        if (CollectionUtil.isNotEmpty(list)) {
            exist = true;
        }
        return exist;
    }

    @Override
    public boolean editlistfield(Integer id, String slistselectid) {
        SysList sysList = this.getById(id);
        sysList.setSListSelectId(slistselectid);
        return this.saveOrUpdate(sysList);
    }
}
