package cn.myerm.objectbuilder.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.myerm.common.exception.SystemException;
import cn.myerm.common.service.impl.CommonServiceImpl;
import cn.myerm.objectbuilder.entity.*;
import cn.myerm.objectbuilder.mapper.SysObjectMapper;
import cn.myerm.objectbuilder.params.FieldParams;
import cn.myerm.objectbuilder.params.ListParams;
import cn.myerm.objectbuilder.service.*;
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
 * @since 2021-04-08
 */
@Service
public class SysObjectServiceImpl extends CommonServiceImpl<SysObjectMapper, SysObject> implements ISysObjectService {
    @Autowired
    ISysFieldService sysFieldService;

    @Autowired
    ISysListService sysListService;

    @Autowired
    ISysUIService sysUIService;

    @Autowired
    ISysNavItemService sysNavItemService;

    @Autowired
    IJdbcTemplateService jdbcTemplateService;

    @Override
    public SysObject getSysObjectByObjectName(String objectName) {
        QueryWrapper<SysObject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", objectName);
        return this.getOne(queryWrapper);
    }

    @Override
    public List<SysObject> sysObjectList() {
        QueryWrapper<SysObject> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sObjectName");
        return this.list(queryWrapper);
    }

    @Transactional
    @Override
    public boolean newsave(String table, String name, String moduleid, String datasourceid, Boolean isworkflow, Boolean isdetail, Boolean isauto) {
        //插入SysObject
        String sObjectName = moduleid + "/" + table;
        SysObject sysObject = new SysObject();
        sysObject.setSObjectName(sObjectName);
        sysObject.setSDbTable(table);
        sysObject.setSysModuleId(moduleid);
        sysObject.setSName(name);
        sysObject.setSysDataSourceId(datasourceid);
        sysObject.setBWorkFlow(isworkflow);
        sysObject.setDataPowerTypeId("SysRole");
        sysObject.setSIdFieldAs("lID");
        sysObject.setSNameFieldAs("sName");
        sysObject.setSOperatorJson("[{\"ID\":\"del\",\"sName\":\"删除\"},{\"ID\":\"edit\",\"sName\":\"编辑\"},{\"ID\":\"list\",\"sName\":\"列表\"},{\"ID\":\"new\",\"sName\":\"新建\"},{\"ID\":\"view\",\"sName\":\"查看详情\"}]");

        SysObject hasObject = this.getSysObjectByObjectName(sObjectName);
        if (ObjectUtil.isNotEmpty(hasObject)) {
            throw new SystemException("此对象已存在");
        }
        this.save(sysObject);
        this.newsaveHandler(sObjectName, name, isauto, sysObject.getSDbTable(), false, null);

        if (isdetail) {
            String sDetailObjectName = sObjectName + "Detail";
            SysObject detailSysObject = new SysObject();
            detailSysObject.setSObjectName(sDetailObjectName);
            detailSysObject.setSDbTable(table + "Detail");
            detailSysObject.setSysModuleId(moduleid);
            detailSysObject.setSName(name + "明细");
            detailSysObject.setSysDataSourceId(datasourceid);
            detailSysObject.setBWorkFlow(isworkflow);
            detailSysObject.setSIdFieldAs("lID");
            detailSysObject.setSNameFieldAs("sName");
            detailSysObject.setParentId(sObjectName);
            detailSysObject.setDataPowerTypeId("SysRole");
            detailSysObject.setSParentIdFieldAs("ParentId");
            SysObject hasDetailObjec = this.getSysObjectByObjectName(sDetailObjectName);
            if (ObjectUtil.isNotEmpty(hasDetailObjec)) {
                throw new SystemException("此对象已存在");
            }
            this.save(detailSysObject);
            this.newsaveHandler(sDetailObjectName, name + "明细", isauto, detailSysObject.getSDbTable(), true, sObjectName);
        }

        return true;
    }

    public void newsaveHandler(String sObjectName, String name, Boolean isAuto, String table, boolean isdetail, String parentObj) {
        jdbcTemplateService.createDbTable(table, true);

        //lID、sName、OwnerId、NewUserId、EditUserId、dNewTime、dEditTime
        //插入SysField
        //lID
        FieldParams idFieldParams = new FieldParams();
        idFieldParams.setSobjectname(sObjectName);
        idFieldParams.setName(name + "ID");
        idFieldParams.setIsautoincrement(true);
        idFieldParams.setIsPk(true);
        idFieldParams.setField("lID");
        idFieldParams.setType("Int");
        Integer idlID = sysFieldService.newSave(idFieldParams);

        //sName
        FieldParams nameFieldParams = new FieldParams();
        nameFieldParams.setSobjectname(sObjectName);
        nameFieldParams.setName(name + "名称");
        nameFieldParams.setField("sName");
        nameFieldParams.setType("Text");
        nameFieldParams.setLength(64);
        Integer namelID = sysFieldService.newSave(nameFieldParams);

        //OwnerId
        if (!isdetail) {
            FieldParams OwnerIdFieldParams = new FieldParams();
            OwnerIdFieldParams.setSobjectname(sObjectName);
            OwnerIdFieldParams.setName("负责人");
            OwnerIdFieldParams.setField("OwnerId");
            OwnerIdFieldParams.setType("ListTable");
            OwnerIdFieldParams.setRefkey("System/SysUser");
            sysFieldService.newSave(OwnerIdFieldParams);

            //NewUserId
            FieldParams NewUserIdFieldParams = new FieldParams();
            NewUserIdFieldParams.setSobjectname(sObjectName);
            NewUserIdFieldParams.setName("新建人");
            NewUserIdFieldParams.setField("NewUserId");
            NewUserIdFieldParams.setType("ListTable");
            NewUserIdFieldParams.setRefkey("System/SysUser");
            sysFieldService.newSave(NewUserIdFieldParams);

            //EditUserId
            FieldParams EditUserIdFieldParams = new FieldParams();
            EditUserIdFieldParams.setSobjectname(sObjectName);
            EditUserIdFieldParams.setName("编辑人");
            EditUserIdFieldParams.setField("EditUserId");
            EditUserIdFieldParams.setType("ListTable");
            EditUserIdFieldParams.setRefkey("System/SysUser");
            sysFieldService.newSave(EditUserIdFieldParams);

            //dNewTime
            FieldParams dNewTimeFieldParams = new FieldParams();
            dNewTimeFieldParams.setSobjectname(sObjectName);
            dNewTimeFieldParams.setName("新建时间");
            dNewTimeFieldParams.setField("dNewTime");
            dNewTimeFieldParams.setType("DateTime");
            dNewTimeFieldParams.setDefaultvalue("?curdatetime?");
            sysFieldService.newSave(dNewTimeFieldParams);

            //dEditTime
            FieldParams dEditTimeFieldParams = new FieldParams();
            dEditTimeFieldParams.setSobjectname(sObjectName);
            dEditTimeFieldParams.setName("编辑时间");
            dEditTimeFieldParams.setField("dEditTime");
            dEditTimeFieldParams.setType("DateTime");
            dEditTimeFieldParams.setDefaultvalue("?curdatetime?");
            sysFieldService.newSave(dEditTimeFieldParams);
        } else {
            //ParentId
            FieldParams ParentIdFieldParams = new FieldParams();
            ParentIdFieldParams.setSobjectname(sObjectName);
            ParentIdFieldParams.setName("父对象");
            ParentIdFieldParams.setField("ParentId");
            ParentIdFieldParams.setType("ListTable");
            ParentIdFieldParams.setRefkey(parentObj);
            sysFieldService.newSave(ParentIdFieldParams);
        }

        //插入SysList
        ListParams allListParams = new ListParams();
        allListParams.setSname("全部");
        allListParams.setSobjectname(sObjectName);
        allListParams.setTypeid("List");
        allListParams.setSordersql("dNewTime DESC");
        allListParams.setBactive(true);
        allListParams.setBdefault(true);
        allListParams.setBcanpage(true);
        allListParams.setBcanbat(true);
        allListParams.setBsingle(false);
        allListParams.setLpagelimit(20);

        List<Integer> selectidList = new ArrayList<>();
        String slistselectid = CollectionUtil.join(selectidList, ",");
        allListParams.setSlistselectid(slistselectid);

        List<Integer> fastsearchList = new ArrayList<>();
        String sfastsearchid = CollectionUtil.join(fastsearchList, ",");
        allListParams.setSfastsearchid(sfastsearchid);

        sysListService.newsave(allListParams);

        ListParams refListParams = new ListParams();
        refListParams.setSname("参照");
        refListParams.setSobjectname(sObjectName);
        refListParams.setTypeid("Refer");
        refListParams.setSordersql("dNewTime DESC");
        refListParams.setBactive(true);
        refListParams.setBdefault(false);
        refListParams.setBcanpage(true);
        refListParams.setBcanbat(true);
        refListParams.setBsingle(false);
        refListParams.setLpagelimit(20);
        refListParams.setSlistselectid(slistselectid);
        refListParams.setSfastsearchid(sfastsearchid);
        sysListService.newsave(refListParams);

        //插入SysUI
        sysUIService.newsave(sObjectName, "新建/编辑", "new,edit", null, "[{'sName':'基本信息','arrGroupField1':[" + namelID + "],'arrGroupField2':[],'arrGroupField3':[]}]");
        sysUIService.newsave(sObjectName, "详情", "view", null, "[{'sName':'基本信息','arrGroupField1':[" + namelID + "],'arrGroupField2':[],'arrGroupField3':[]}]");

        //插入SysNavItem
        SysNavItem sysNavItem = new SysNavItem();
        sysNavItem.setSName(name);
        sysNavItem.setSObjectName(sObjectName);
        sysNavItem.setSRoute("/" + sObjectName.toLowerCase() + "/home");
        sysNavItemService.save(sysNavItem);

        if (!isdetail) {
            List<String> dNewTimeList = new ArrayList<>();
            dNewTimeList.add("dNewTime");
            jdbcTemplateService.createIndex(table, "dNewTime", dNewTimeList);

            List<String> OwnerIddNewTimeList = new ArrayList<>();
            OwnerIddNewTimeList.add("OwnerId");
            OwnerIddNewTimeList.add("dNewTime");
            jdbcTemplateService.createIndex(table, "OwnerIddNewTime", OwnerIddNewTimeList);
        } else {
            List<String> ParentIdList = new ArrayList<>();
            ParentIdList.add("ParentId");
            jdbcTemplateService.createIndex(table, "ParentId", ParentIdList);
        }
    }

    @Transactional
    @Override
    public boolean editsave(String sobjectname, String name, String datasourceid) {
        SysObject sysObject = this.getSysObjectByObjectName(sobjectname);
        sysObject.setSName(name);
        sysObject.setSysDataSourceId(datasourceid);
        this.saveOrUpdate(sysObject);

        return true;
    }

    @Transactional
    @Override
    public boolean del(String sobjectname) {
        if (sobjectname.indexOf(",") == -1) {
            delone(sobjectname);
        } else {
            String[] arrObjectname = sobjectname.split(",");
            for (String s : arrObjectname) {
                delone(s);
            }
        }
        return true;
    }


    private boolean delone(String sobjectname) {
        QueryWrapper<SysObject> querySysObjectWrapper = new QueryWrapper<>();
        querySysObjectWrapper.eq("sObjectName", sobjectname);
        this.remove(querySysObjectWrapper);

        QueryWrapper<SysField> querySysFieldWrapper = new QueryWrapper<>();
        querySysFieldWrapper.eq("sObjectName", sobjectname);
        sysFieldService.remove(querySysFieldWrapper);

        QueryWrapper<SysList> querySysListWrapper = new QueryWrapper<>();
        querySysListWrapper.eq("sObjectName", sobjectname);
        sysListService.remove(querySysListWrapper);

        QueryWrapper<SysNavItem> querySysNavItemWrapper = new QueryWrapper<>();
        querySysNavItemWrapper.eq("sObjectName", sobjectname);
        sysNavItemService.remove(querySysNavItemWrapper);

        QueryWrapper<SysUI> querySysUIWrapper = new QueryWrapper<>();
        querySysUIWrapper.eq("sObjectName", sobjectname);
        sysUIService.remove(querySysUIWrapper);

        return true;
    }

    @Override
    public SysObject detail(String sobjectname) {
        return this.getSysObjectByObjectName(sobjectname);
    }

    @Transactional
    @Override
    public boolean attachsave(String table, String name, String moduleid, String datasourceid, Boolean isworkflow, String idfield, String namefield, Boolean isauto, String pktype) {
        if (idfield.equals(namefield)) {
            throw new SystemException("ID字段和名称字段不能同一个");
        }

        String sObjectName = moduleid + "/" + table;
        SysObject hasObject = this.getSysObjectByObjectName(sObjectName);
        if (ObjectUtil.isNotEmpty(hasObject)) {
            throw new SystemException("此对象已存在");
        }

        //插入SysObject信息
        SysObject sysObject = new SysObject();
        sysObject.setSObjectName(sObjectName);
        sysObject.setSDbTable(table);
        sysObject.setSysModuleId(moduleid);
        sysObject.setSName(name);
        sysObject.setSysDataSourceId(datasourceid);
        sysObject.setBWorkFlow(isworkflow);
        sysObject.setDataPowerTypeId("SysRole");
        sysObject.setSIdFieldAs(idfield);

        String[] arrKey = new String[]{"new", "edit", "view", "list", "export", "del"};
        String[] arrVal = new String[]{"新建", "编辑", "详情", "列表", "导出", "删除"};

        List<Map<String, String>> operationList = new ArrayList<>();
        Map<String, String> newMap = null;
        for (int i = 0; i < arrKey.length; i++) {
            newMap = new HashMap<>();
            newMap.put("ID", arrKey[i]);
            newMap.put("sName", arrVal[i]);

            operationList.add(newMap);
        }
        sysObject.setSOperatorJson(JSONUtil.toJsonStr(operationList));
        sysObject.setSNameFieldAs(namefield);
        this.save(sysObject);

        //插入SysField信息
        //lID
        FieldParams idFieldParams = new FieldParams();
        idFieldParams.setSobjectname(sObjectName);
        idFieldParams.setName("ID");
        if (isauto) {
            idFieldParams.setIsautoincrement(true);
            idFieldParams.setType("Int");
        } else {
            idFieldParams.setType("Text");
        }
        idFieldParams.setIsPk(true);
        idFieldParams.setField(idfield);
        idFieldParams.setLength(10);
        idFieldParams.setWidth(80);
        Integer idlID = sysFieldService.newSave(idFieldParams);

        //sName
        FieldParams nameFieldParams = new FieldParams();
        nameFieldParams.setSobjectname(sObjectName);
        nameFieldParams.setName(name + "名称");
        nameFieldParams.setField(namefield);
        nameFieldParams.setType("Text");
        nameFieldParams.setUitype("Text");
        nameFieldParams.setLength(64);
        nameFieldParams.setRequired(true);
        Integer namelID = sysFieldService.newSave(nameFieldParams);

        //插入SysList信息
        ListParams allListParams = new ListParams();
        allListParams.setSname("全部");
        allListParams.setSobjectname(sObjectName);
        allListParams.setTypeid("List");
        allListParams.setBactive(true);
        allListParams.setBdefault(true);
        allListParams.setBcanpage(true);
        allListParams.setBcanbat(true);
        allListParams.setBsingle(false);
        allListParams.setLpagelimit(20);

        List<Integer> selectidList = new ArrayList<>();
        String slistselectid = CollectionUtil.join(selectidList, ",");
        allListParams.setSlistselectid(slistselectid);

        List<Integer> fastsearchList = new ArrayList<>();
        String sfastsearchid = CollectionUtil.join(fastsearchList, ",");
        allListParams.setSfastsearchid(sfastsearchid);

        sysListService.newsave(allListParams);

        ListParams refListParams = new ListParams();
        refListParams.setSname("参照");
        refListParams.setSobjectname(sObjectName);
        refListParams.setTypeid("Refer");
        refListParams.setBactive(true);
        refListParams.setBdefault(false);
        refListParams.setBcanpage(true);
        refListParams.setBcanbat(true);
        refListParams.setBsingle(false);
        refListParams.setLpagelimit(20);
        refListParams.setSlistselectid(slistselectid);
        refListParams.setSfastsearchid(sfastsearchid);
        sysListService.newsave(refListParams);

        //插入SysUI信息
        sysUIService.newsave(sObjectName, "新建/编辑", "new,edit", null, "[{'sName':'基本信息','arrGroupField1':[" + namelID + "],'arrGroupField2':[],'arrGroupField3':[]}]");
        sysUIService.newsave(sObjectName, "详情", "view", null, "[{'sName':'基本信息','arrGroupField1':[" + namelID + "],'arrGroupField2':[],'arrGroupField3':[]}]");

        //插入SysNavItem信息
        SysNavItem sysNavItem = new SysNavItem();
        sysNavItem.setSName(name);
        sysNavItem.setSObjectName(sObjectName);
        sysNavItem.setSRoute("/" + sObjectName.toLowerCase() + "/home");
        sysNavItemService.save(sysNavItem);

        return true;
    }

    @Override
    public JSONArray operationlist(String sobjectname) {
        SysObject sysObject = getSysObjectByObjectName(sobjectname);
        String sOperatorJson = sysObject.getSOperatorJson();

        JSONArray jsonArray = null;
        if (StrUtil.isNotEmpty(sOperatorJson)) {
            jsonArray = JSONUtil.parseArray(sOperatorJson);
        }
        return jsonArray;
    }

    @Override
    public boolean operationnewsave(String sobjectname, String id, String name) {
        SysObject sysObject = getSysObjectByObjectName(sobjectname);
        String sOperatorJson = sysObject.getSOperatorJson();
        JSONArray jsonArray = JSONUtil.parseArray(sOperatorJson);
        Map<String, Object> map = new HashMap<>();
        map.put("ID", id);
        map.put("sName", name);

        jsonArray.add(map);
        sysObject.setSOperatorJson(jsonArray.toStringPretty());
        return this.saveOrUpdate(sysObject);
    }

    @Override
    public boolean operationeditsave(String sobjectname, String id, String name) {
        SysObject sysObject = getSysObjectByObjectName(sobjectname);
        String sOperatorJson = sysObject.getSOperatorJson();
        JSONArray jsonArray = JSONUtil.parseArray(sOperatorJson);

        for (Object object : jsonArray) {
            Map<String, Object> jsonObject = (Map<String, Object>) object;
            String ID = (String) jsonObject.get("ID");
            if (ID.equals(id)) {
                jsonObject.put("sName", name);
                break;
            }
        }
        sysObject.setSOperatorJson(jsonArray.toString());
        return this.saveOrUpdate(sysObject);
    }

    @Override
    public boolean operationdel(String sobjectname, String id) {
        SysObject sysObject = getSysObjectByObjectName(sobjectname);
        String sOperatorJson = sysObject.getSOperatorJson();
        JSONArray jsonArray = JSONUtil.parseArray(sOperatorJson);

        for (Object object : jsonArray) {
            Map<String, Object> jsonObject = (Map<String, Object>) object;
            String ID = (String) jsonObject.get("ID");
            if (ID.equals(id)) {
                jsonArray.remove(object);
                break;
            }
        }
        sysObject.setSOperatorJson(jsonArray.toStringPretty());
        return this.saveOrUpdate(sysObject);
    }
}
