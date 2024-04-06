package cn.myerm.objectbuilder.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.common.exception.SystemException;
import cn.myerm.common.service.impl.CommonServiceImpl;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.entity.SysUI;
import cn.myerm.objectbuilder.mapper.SysUIMapper;
import cn.myerm.objectbuilder.service.ISysUIService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * 界面的service层
 * ============================================================================
 * 版权所有 2016-2021 来三斤（厦门）网络科技有限公司，并保留所有权利。
 * 网站地址: http://myerm.cn
 * ----------------------------------------------------------------------------
 * 这不是一个自由软件！您只能在不用于商业目的的前提下对程序代码进行修改和
 * 使用；不允许对程序代码以任何形式任何目的的再发布。
 * ============================================================================
 *
 * @author 陈鹭明  <lumingchen@qq.com>
 * @version v3.0
 * @since 2021年4月14日10:36:20
 */
@Service
public class SysUIServiceImpl extends CommonServiceImpl<SysUIMapper, SysUI> implements ISysUIService {

    @Resource
    private SysFieldServiceImpl sysFieldService;

    @Resource
    private SysObjectServiceImpl sysObjectService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public JSONArray getConfig(SysUI sysUI) {
        if (StrUtil.hasEmpty(sysUI.getSConfigJson())) {
            return null;
        } else {
            JSONArray jsonArray = JSONObject.parseArray(sysUI.getSConfigJson());
            int lIndex = 0;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                for (int k = 1; k < 4; k++) {
                    JSONArray jsonGroupField = jsonObject.getJSONArray("arrGroupField" + k);
                    if (jsonGroupField != null && jsonGroupField.size() > 0) {
                        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<>();
                        sysFieldQueryWrapper.in("lID", jsonGroupField);
                        List<SysField> listSysField = sysFieldService.list(sysFieldQueryWrapper);

                        List<SysField> listGroupField = new ArrayList<>();
                        for (int j = 0; j < jsonGroupField.size(); j++) {
                            for (SysField sysField : listSysField) {
                                if (sysField.getLID().compareTo(jsonGroupField.getInteger(j)) == 0) {
                                    if (sysField.getRefSysFieldId() != null) {
                                        if (sysField.getSDataType().equals("Common")) {
                                            sysField.setReferenceField(sysFieldService.getById(sysField.getRefSysFieldId()));
                                            sysField.setArrEnumOption(sysFieldService.getEnumOption(sysField.getReferenceField()));
                                        } else if (sysField.getSDataType().equals("Reference")) {
                                            sysField.setReferenceField(sysFieldService.getByFieldName(sysField.getSRefKey(), sysField.getSRefFieldAs()));
                                        }
                                    } else if (sysField.getSDataType().equalsIgnoreCase("List") || sysField.getSDataType().equalsIgnoreCase("MultiList")) {
                                        sysField.setArrEnumOption(sysFieldService.getEnumOption(sysField));
                                    }

                                    sysField.setLIndex(lIndex++);

                                    listGroupField.add(sysField);
                                    break;
                                }
                            }
                        }

                        jsonObject.put("arrGroupField" + k, listGroupField);
                    }
                }
            }

            return jsonArray;
        }
    }


    public List<Map<String, Object>> listHaveField(String sObjectName, Integer id) {
        QueryWrapper<SysUI> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.eq("lID", id);
        List<SysUI> sysUIList = this.list(queryWrapper);

        List<Map<String, Object>> resultList = new ArrayList<>();
        for (SysUI sysUI : sysUIList) {
            Map<String, Object> resultMap = this.info(sysUI);
            resultList.add(resultMap);
        }

        return resultList;
    }

    @Override
    public List<Map<String, Object>> list(String sObjectName) {
        QueryWrapper<SysUI> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        List<SysUI> sysUIList = this.list(queryWrapper);

        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> itemMap = null;
        for (SysUI sysUI : sysUIList) {
            Map<String, Object> resultMap = this.info(sysUI);
            itemMap = new HashMap<>();
            itemMap.put("lID", resultMap.get("lID"));
            itemMap.put("sName", resultMap.get("sName"));
            itemMap.put("arrType", resultMap.get("arrType"));

            JSONArray jsonArray = (JSONArray) resultMap.get("arrFieldClass");
            List<String> nameList = new ArrayList<>();
            if (CollectionUtil.isNotEmpty(jsonArray)) {
                for (Object o : jsonArray) {
                    JSONObject jsonObjec = (JSONObject) o;
                    String sName = jsonObjec.getString("sName");
                    nameList.add(sName);
                }
            }

            itemMap.put("arrFieldClass", nameList);
            resultList.add(itemMap);
        }

        return resultList;
    }

    @Override
    public Map<String, Object> info(SysUI sysUI) {
        String typeId = sysUI.getTypeId();
        List<String> typeList = new ArrayList<>();
        Map<String, String> uiType = new HashMap<>();
        uiType.put("new", "新建");
        uiType.put("edit", "修改");
        uiType.put("view", "查看");

        if (StrUtil.isNotEmpty(typeId)) {
            String[] arrType = typeId.split(",");
            for (String type : arrType) {
                String typeCn = uiType.get(type);
                typeList.add(typeCn);
            }
        }

        JSONArray arrFieldClass = this.getConfig(sysUI);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arrFieldClass", arrFieldClass);
        resultMap.put("arrType", typeList);
        resultMap.put("lID", sysUI.getLID());
        resultMap.put("sName", sysUI.getSName());
        return resultMap;
    }

    @Override
    public boolean newsave(String sobjectname, String name, String type, String permission, String configs) {
        SysUI sysUI = new SysUI();
        sysUI.setSObjectName(sobjectname);
        sysUI.setSName(name);
        sysUI.setTypeId(type);

        try {
            JSONObject permissionJson = JSONObject.parseObject(permission);
        } catch (JSONException e) {
            throw new SystemException(e.getMessage());
        }

        if (StrUtil.isNotEmpty(permission)) {
            sysUI.setSPermissionJson(permission);
        }

        if (StrUtil.isNotEmpty(configs)) {
            sysUI.setSConfigJson(configs);
        }

        boolean b = this.save(sysUI);
        return b;
    }

    @Override
    public boolean editsave(Integer id, String name, String type, String permission) {
        SysUI sysUI = this.getById(id);
        sysUI.setSName(name);
        sysUI.setTypeId(type);

        try {
            JSONObject permissionJson = JSONObject.parseObject(permission);
        } catch (JSONException e) {
            throw new SystemException(e.getMessage());
        }

        sysUI.setSPermissionJson(permission);
        boolean b = this.saveOrUpdate(sysUI);
        return b;
    }

    @Override
    public boolean fieldclassnewsave(Integer uiid, String name, String group1, String group2, String group3) {
        SysUI sysUI = this.getById(uiid);
        String sConfigJson = sysUI.getSConfigJson();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("sName", name);

        String[] arrGroupField1 = {group1};
        String[] arrGroupField2 = {group2};
        String[] arrGroupField3 = {group3};

        if (group1.indexOf(",") > 0) {
            arrGroupField1 = group1.split(",");
        }
        if (group2.indexOf(",") > 0) {
            arrGroupField2 = group2.split(",");
        }
        if (group3.indexOf(",") > 0) {
            arrGroupField3 = group3.split(",");
        }

        List<Integer> arrGroupField1List = new ArrayList<>();
        for (String field1 : arrGroupField1) {
            if (StrUtil.isNotEmpty(field1)) {
                Integer lField1 = Integer.parseInt(field1);
                arrGroupField1List.add(lField1);
            }
        }

        List<Integer> arrGroupField2List = new ArrayList<>();
        for (String field2 : arrGroupField2) {
            if (StrUtil.isNotEmpty(field2)) {
                Integer lField2 = Integer.parseInt(field2);
                arrGroupField2List.add(lField2);
            }
        }

        List<Integer> arrGroupField3List = new ArrayList<>();
        for (String field3 : arrGroupField3) {
            if (StrUtil.isNotEmpty(field3)) {
                Integer lField3 = Integer.parseInt(field3);
                arrGroupField3List.add(lField3);
            }
        }

        jsonObject.put("arrGroupField1", arrGroupField1List);
        jsonObject.put("arrGroupField2", arrGroupField2List);
        jsonObject.put("arrGroupField3", arrGroupField3List);

        JSONArray jsonArray = null;
        if (StrUtil.isNotEmpty(sConfigJson)) {
            jsonArray = JSONArray.parseArray(sConfigJson);
        } else {
            jsonArray = new JSONArray();
        }
        jsonArray.add(jsonObject);
        sysUI.setSConfigJson(jsonArray.toJSONString());
        boolean b = this.saveOrUpdate(sysUI);
        return b;
    }

    @Override
    public boolean fieldclasseditsave(Integer uiid, Integer id, String name, String group1, String group2, String group3) {
        SysUI sysUI = this.getById(uiid);
        String sConfigJson = sysUI.getSConfigJson();
        JSONArray jsonArray = JSONArray.parseArray(sConfigJson);

        JSONObject jsonObject = (JSONObject) jsonArray.get(id);
        jsonObject.put("sName", name);

        String[] arrGroupField1 = {group1};
        String[] arrGroupField2 = {group2};
        String[] arrGroupField3 = {group3};

        if (group1.indexOf(",") > 0) {
            arrGroupField1 = group1.split(",");
        }
        if (group2.indexOf(",") > 0) {
            arrGroupField2 = group2.split(",");
        }
        if (group3.indexOf(",") > 0) {
            arrGroupField3 = group3.split(",");
        }

        List<Integer> arrGroupField1List = new ArrayList<>();
        for (String field1 : arrGroupField1) {
            if (StrUtil.isNotEmpty(field1)) {
                Integer lField1 = Integer.parseInt(field1);
                arrGroupField1List.add(lField1);
            }
        }

        List<Integer> arrGroupField2List = new ArrayList<>();
        for (String field2 : arrGroupField2) {
            if (StrUtil.isNotEmpty(field2)) {
                Integer lField2 = Integer.parseInt(field2);
                arrGroupField2List.add(lField2);
            }
        }

        List<Integer> arrGroupField3List = new ArrayList<>();
        for (String field3 : arrGroupField3) {
            if (StrUtil.isNotEmpty(field3)) {
                Integer lField3 = Integer.parseInt(field3);
                arrGroupField3List.add(lField3);
            }
        }

        jsonObject.put("arrGroupField1", arrGroupField1List);
        jsonObject.put("arrGroupField2", arrGroupField2List);
        jsonObject.put("arrGroupField3", arrGroupField3List);

        jsonArray.set(id, jsonObject);

        sysUI.setSConfigJson(jsonArray.toJSONString());
        boolean b = this.saveOrUpdate(sysUI);
        return b;
    }

    @Override
    public boolean del(String id) {
        List<Integer> ids = new ArrayList<>();
        if (id.indexOf(",") != -1) {
            String[] arr = id.split(",");
            for (String str : arr) {
                Integer lid = Integer.parseInt(str);
                ids.add(lid);
            }
        } else {
            ids.add(Integer.parseInt(id));
        }
        boolean b = this.removeByIds(ids);
        return b;
    }

    @Override
    public boolean fieldclassdel(Integer uiid, Integer id) {
        SysUI sysUI = this.getById(uiid);
        String sConfigJson = sysUI.getSConfigJson();
        JSONArray jsonArray = JSONArray.parseArray(sConfigJson);
        jsonArray.remove(id.intValue());

        sysUI.setSConfigJson(jsonArray.toJSONString());
        boolean b = this.saveOrUpdate(sysUI);
        return b;
    }

    @Override
    public Map<String, Object> view(Integer id) {
        SysUI sysUI = this.getById(id);
        Map<String, Object> map = this.info(sysUI);
        Map<String, Object> resultMap = new HashMap<>();

        String typeid = sysUI.getTypeId();
        List<String> typeList = new ArrayList<>();
        if (typeid.indexOf(",") != -1) {
            String[] arrType = typeid.split(",");
            for (String s : arrType) {
                typeList.add(s);
            }
        } else {
            typeList.add(typeid);
        }
        resultMap.put("arrFieldClass", map.get("arrFieldClass"));
        resultMap.put("TypeId", typeList);
        resultMap.put("lID", map.get("lID"));
        resultMap.put("sName", map.get("sName"));

        String sPermissionJson = sysUI.getSPermissionJson();
        JSONObject parseObject = JSONObject.parseObject(sPermissionJson);
        resultMap.put("arrPower", parseObject);

        return resultMap;
    }

    /**
     * 提取界面里的所有属性
     *
     * @param sysUI
     * @return
     */
    public List<SysField> getSysFieldInConfig(SysUI sysUI) {
        if (StrUtil.hasEmpty(sysUI.getSConfigJson())) {
            return null;
        } else {
            JSONArray jsonArray = JSONObject.parseArray(sysUI.getSConfigJson());
            List<SysField> listGroupField = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {//循环信息块
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                for (int k = 1; k < 4; k++) {//循环信息块中的列
                    JSONArray jsonGroupField = jsonObject.getJSONArray("arrGroupField" + k);
                    if (jsonGroupField != null && jsonGroupField.size() > 0) {
                        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<>();
                        sysFieldQueryWrapper.in("lID", jsonGroupField);
                        listGroupField.addAll(sysFieldService.list(sysFieldQueryWrapper));
                    }
                }
            }

            return listGroupField;
        }
    }

    @Override
    public List<SysField> getUIFieldList(String sObjectName, Integer uid) {
        //找出可用的字段
        List<SysField> allList = sysFieldService.getAllFieldByObject(sObjectName);
        List<Map<String, Object>> haveList = this.listHaveField(sObjectName, uid);

        List<SysField> haveFieldList = new ArrayList<>();
        for (Map<String, Object> map : haveList) {
            JSONArray jsonArray = (JSONArray) map.get("arrFieldClass");
            if (ObjectUtil.isNotEmpty(jsonArray) && jsonArray.size() > 0) {
                for (Object object : jsonArray) {
                    JSONObject jsonObject = (JSONObject) object;
                    List<SysField> arrGroupField1 = (List<SysField>) jsonObject.get("arrGroupField1");
                    List<SysField> arrGroupField2 = (List<SysField>) jsonObject.get("arrGroupField2");
                    List<SysField> arrGroupField3 = (List<SysField>) jsonObject.get("arrGroupField3");

                    for (SysField sysField1 : arrGroupField1) {
                        if (!haveFieldList.contains(sysField1)) {
                            haveFieldList.add(sysField1);
                        }
                    }
                    for (SysField sysField2 : arrGroupField2) {
                        if (!haveFieldList.contains(sysField2)) {
                            haveFieldList.add(sysField2);
                        }
                    }

                    for (SysField sysField3 : arrGroupField3) {
                        if (!haveFieldList.contains(sysField3)) {
                            haveFieldList.add(sysField3);
                        }
                    }

                }
            }
        }

        List<SysField> needDelList = new ArrayList<>();
        int len = allList.size();

        for (int i = 0; i < len; i++) {
            Integer id = (Integer) allList.get(i).getLID();
            for (SysField sysField : haveFieldList) {
                if (sysField.getLID().intValue() == id.intValue()) {
                    needDelList.add(allList.get(i));
                    break;
                }
            }
        }

        allList.removeAll(needDelList);
        return allList;
    }

    public boolean isExist(String objectname, String typeid) {
        QueryWrapper<SysUI> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", objectname);
        queryWrapper.eq("TypeId", typeid);

        List<SysUI> list = this.list(queryWrapper);
        boolean exists = false;
        if (CollectionUtil.isNotEmpty(list)) {
            exists = true;
        }
        return exists;
    }

    @Override
    public boolean fieldclassorder(Integer uiid, String order) {
        SysUI sysUI = this.getById(uiid);
        String sConfigJson = sysUI.getSConfigJson();
        JSONArray oldArr = JSONArray.parseArray(sConfigJson);
        if (order.indexOf(",") != -1) {
            String[] arrOrder = order.split(",");
            int len = arrOrder.length;

            List<Object> newList = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                Integer currentOrder = Integer.parseInt(arrOrder[i]);
                newList.add(oldArr.get(currentOrder));
            }

            sysUI.setSConfigJson(JSONObject.toJSONString(newList));
        }
        boolean b = this.saveOrUpdate(sysUI);
        return b;
    }

    @Transactional
    @Override
    public boolean clone(Integer id) {
        SysUI sysUI = this.getById(id);
        String objectName = sysUI.getSObjectName();
        String sName = sysUI.getSName();
        String type = sysUI.getTypeId();
        String permission = sysUI.getSPermissionJson();
        String configs = sysUI.getSConfigJson();
        boolean b = this.newsave(objectName, sName, type, permission, configs);
        return b;
    }
}
