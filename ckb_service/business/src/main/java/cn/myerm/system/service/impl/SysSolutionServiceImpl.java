package cn.myerm.system.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.objectbuilder.entity.SysNavItem;
import cn.myerm.objectbuilder.service.impl.SysNavItemServiceImpl;
import cn.myerm.system.entity.SysSolution;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysSolutionMapper;
import cn.myerm.system.service.ISysSolutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-09-08
 */
@Service
public class SysSolutionServiceImpl extends BusinessServiceImpl<SysSolutionMapper, SysSolution> implements ISysSolutionService {
    @Autowired
    private SysNavItemServiceImpl sysNavItemService;

    public Map<String, Object> getDetailData(int ObjectId) {
        SysSolution sysSolution = getById(ObjectId);
        JSONArray arrJsonConfig = JSONObject.parseArray(sysSolution.getSConfigJson());

        //收集ID，以便一次性查询出来
        List<String> listNavItemId = new ArrayList<>();
        for (Object jsonCfg : arrJsonConfig) {
            JSONArray arrNavItemId = ((JSONObject) jsonCfg).getJSONArray("arrChildren");
            for (int i = 0; i < arrNavItemId.size(); i++) {
                listNavItemId.add(arrNavItemId.getString(i));
            }
        }

        //把需要用到SysNavItem放入一个map中，以便取出
        Map<Integer, SysNavItem> mapSysNavItem = new HashMap<>();
        if (listNavItemId.size() > 0) {
            QueryWrapper<SysNavItem> navItemQueryWrapper = new QueryWrapper<>();
            navItemQueryWrapper.in("lID", listNavItemId);
            List<SysNavItem> listSysNavItem = sysNavItemService.list(navItemQueryWrapper);

            for (SysNavItem sysNavItem : listSysNavItem) {
                mapSysNavItem.put(sysNavItem.getLID(), sysNavItem);
            }
        }

        //把结果集赋值回arrChildren
        for (Object jsonCfg : arrJsonConfig) {
            List<SysNavItem> listChindren = new ArrayList<>();
            JSONArray arrNavItemId = ((JSONObject) jsonCfg).getJSONArray("arrChildren");
            for (int i = 0; i < arrNavItemId.size(); i++) {
                if (ObjectUtil.isNotEmpty(mapSysNavItem.get(arrNavItemId.getInteger(i)))) {
                    listChindren.add(mapSysNavItem.get(arrNavItemId.getInteger(i)));
                }
            }

            ((JSONObject) jsonCfg).put("arrChildren", listChindren);
        }

        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("lID", sysSolution.getLID());
        mapResult.put("sName", sysSolution.getSName());
        mapResult.put("listJsonCfg", arrJsonConfig);

        return mapResult;
    }

    /**
     * 获取全部菜单项
     *
     * @return
     */
    public List<SysNavItem> getAllNavItem() {
        QueryWrapper<SysNavItem> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sName");

        return sysNavItemService.list(queryWrapper);
    }

    /**
     * 新建菜单保存
     *
     * @param sName
     * @param sNaveItemSelectedJson
     */
    public void newNaveItemSave(String ObjectId, String sName, String sIcon, String sNaveItemSelectedJson) {
        SysSolution sysSolution = getById(ObjectId);
        JSONArray arrJsonConfig = JSONObject.parseArray(sysSolution.getSConfigJson());
        if (arrJsonConfig == null) {
            arrJsonConfig = new JSONArray();
        }

        JSONObject jsonNavItem = new JSONObject();
        jsonNavItem.put("ID", UUID.randomUUID().toString());
        jsonNavItem.put("sName", sName);
        jsonNavItem.put("sIcon", sIcon);
        jsonNavItem.put("arrChildren", JSONArray.parseArray(sNaveItemSelectedJson));

        arrJsonConfig.add(jsonNavItem);

        sysSolution.setSConfigJson(arrJsonConfig.toJSONString());
        saveOrUpdate(sysSolution);
    }

    /**
     * 新建菜单保存
     *
     * @param sName
     * @param sNaveItemSelectedJson
     */
    public void editNaveItemSave(String ObjectId, String id, String sName, String sIcon, String sNaveItemSelectedJson) {
        SysSolution sysSolution = getById(ObjectId);
        JSONArray arrJsonConfig = JSONObject.parseArray(sysSolution.getSConfigJson());

        for (int i = 0; i < arrJsonConfig.size(); i++) {
            JSONObject jsonCfg = (JSONObject) arrJsonConfig.get(i);
            if (jsonCfg.getString("ID").equals(id)) {
                jsonCfg.put("sName", sName);
                jsonCfg.put("sIcon", sIcon);
                jsonCfg.put("arrChildren", JSONArray.parseArray(sNaveItemSelectedJson));
                arrJsonConfig.set(i, jsonCfg);
            }
        }

        sysSolution.setSConfigJson(arrJsonConfig.toJSONString());
        saveOrUpdate(sysSolution);
    }

    /**
     * 删除菜单
     *
     * @param ObjectId
     * @param sSelectIds
     */
    public void del(String ObjectId, String sSelectIds) {
        JSONArray arrSelected = JSONArray.parseArray(sSelectIds);

        SysSolution sysSolution = getById(ObjectId);
        JSONArray arrJsonConfig = JSONObject.parseArray(sysSolution.getSConfigJson());

        for (int i = 0; i < arrSelected.size(); i++) {
            for (int j = 0; j < arrJsonConfig.size(); j++) {
                JSONObject jsonCfg = (JSONObject) arrJsonConfig.get(j);
                if (arrSelected.getString(i).equals(jsonCfg.getString("ID"))) {
                    arrJsonConfig.remove(j);
                }
            }
        }

        sysSolution.setSConfigJson(arrJsonConfig.toJSONString());
        saveOrUpdate(sysSolution);
    }

    /**
     * 菜单项排序保存
     *
     * @param ObjectId
     * @param sChildren
     */
    public void sortSave(String ObjectId, String id, String sChildren) {
        SysSolution sysSolution = getById(ObjectId);
        JSONArray arrJsonConfig = JSONObject.parseArray(sysSolution.getSConfigJson());

        for (int i = 0; i < arrJsonConfig.size(); i++) {
            JSONObject jsonCfg = (JSONObject) arrJsonConfig.get(i);
            if (jsonCfg.getString("ID").equals(id)) {
                jsonCfg.put("arrChildren", JSONArray.parseArray(sChildren));
            }
        }

        sysSolution.setSConfigJson(arrJsonConfig.toJSONString());
        saveOrUpdate(sysSolution);
    }

    /**
     * 菜单排序
     * @param ObjectId
     * @param sSortData
     */
    public void menuSortSave(String ObjectId, String sSortData) {
        SysSolution sysSolution = getById(ObjectId);
        JSONArray arrJsonConfig = JSONObject.parseArray(sysSolution.getSConfigJson());

        JSONArray arrJsonConfigNew = new JSONArray();

        JSONArray arrSortId = JSONArray.parseArray(sSortData);
        for (Object sortId : arrSortId) {
            for (Object jsonConfig : arrJsonConfig) {
                if (((JSONObject)jsonConfig).getString("ID").equals((String)sortId)) {
                    arrJsonConfigNew.add(jsonConfig);
                }
            }
        }

        sysSolution.setSConfigJson(arrJsonConfigNew.toJSONString());
        saveOrUpdate(sysSolution);
    }
}
