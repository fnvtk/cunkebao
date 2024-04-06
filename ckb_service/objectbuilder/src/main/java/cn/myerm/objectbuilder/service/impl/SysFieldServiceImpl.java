package cn.myerm.objectbuilder.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.myerm.common.exception.SystemException;
import cn.myerm.common.service.impl.CommonServiceImpl;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.entity.SysList;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.entity.SysUI;
import cn.myerm.objectbuilder.mapper.SysFieldMapper;
import cn.myerm.objectbuilder.params.FieldParams;
import cn.myerm.objectbuilder.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @since 2021-04-08
 */
@Service
public class SysFieldServiceImpl extends CommonServiceImpl<SysFieldMapper, SysField> implements ISysFieldService {
    @Resource
    SysFieldMapper sysFieldMapper;

    @Resource
    ISysObjectService sysObjectService;

    @Resource
    ISysFieldService sysFieldService;

    @Resource
    ISysListService sysListService;

    @Resource
    ISysUIService sysUIService;

    @Resource
    IJdbcTemplateService jdbcTemplateService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<SysField> getAllFieldByObject(String sobjectname) {
        QueryWrapper<SysField> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sobjectname);
        queryWrapper.orderByAsc("lID");
        List<SysField> list = this.list(queryWrapper);

        Map<String, String> typeMap = new HashMap<>();
        typeMap.put("Text", "文本型");
        typeMap.put("TextArea", "长文本");
        typeMap.put("Bool", "布尔型");
        typeMap.put("ListTable", "参照");
        typeMap.put("AttachFile", "附件");
        typeMap.put("Int", "整型");
        typeMap.put("Float", "浮点型");
        typeMap.put("Date", "日期型");
        typeMap.put("DateTime", "日期时间型");
        typeMap.put("MultiList", "多选列表");
        typeMap.put("List", "列表");
        typeMap.put("Reference", "引用");
        typeMap.put("Common", "公共属性");
        typeMap.put("Virtual", "虚拟属性");

        for (SysField sysField : list) {
            String sDataType = sysField.getSDataType();
            String cnTxt = typeMap.get(sDataType);
            sysField.setSUITypeTxt(cnTxt + "/" + sDataType);
        }

        return list;
    }

    @Transactional
    @Override
    public Integer newSave(FieldParams fieldParams) {
        Boolean isHaveField = this.exists(fieldParams.getSobjectname(), fieldParams.getField());
        if (isHaveField) {
            throw new SystemException("此属性已存在");
        }

        SysField sysField = new SysField();
        sysField.setSObjectName(fieldParams.getSobjectname());
        sysField.setSName(fieldParams.getName());
        sysField.setSFieldAs(fieldParams.getField());
        String type = fieldParams.getType();
        Boolean isautoincrement = fieldParams.getIsautoincrement();
        sysField.setSTip(fieldParams.getTip());
        sysField.setLWidth(fieldParams.getWidth());
        sysField.setSDataType(type);
        sysField.setSUIType(fieldParams.getUitype());
        sysField.setBRequired(fieldParams.getRequired());
        sysField.setBReadOnly(fieldParams.getReadonly());
        sysField.setBDisabled(fieldParams.getDisabled());

        Boolean isPk = fieldParams.getIsPk();

        if (ObjectUtil.isNotEmpty(isPk)) {
            if (isautoincrement) {
                sysField.setBPrimaryKey(1);
                sysField.setSPrimartKeyType("autoincrement");
                sysField.setSDataType("Int");
            } else {
                sysField.setBPrimaryKey(0);
                sysField.setSPrimartKeyType("system");
                sysField.setSDataType("Text");
            }
        }

        String dbType = null;
        if (type.equals("Text")) {
            sysField.setLLength(fieldParams.getLength());
            sysField.setSDefaultValue(fieldParams.getDefaultvalue());

            dbType = "varchar(" + fieldParams.getLength() + ")";
        } else if (type.equals("TextArea")) {
            sysField.setLLength(fieldParams.getLength());
            sysField.setBEnableRTE(fieldParams.getEnablerte());
            sysField.setSDefaultValue(fieldParams.getDefaultvalue());
            dbType = "Text";
        } else if (type.equals("Bool")) {
            sysField.setSDefaultValue(fieldParams.getDefaultvalue());
            dbType = "Tinyint";
        } else if (type.equals("AttachFile")) {
            sysField.setSRefKey("System/SysAttach");
            sysField.setSRefIdFieldAs("lID");
            sysField.setSRefNameFieldAs("sName");
            sysField.setBMulti(fieldParams.getMulti());
            dbType = "varchar(200)";
        } else if (type.equals("Float")) {
            sysField.setLLength(fieldParams.getLength());
            sysField.setLDeciLength(fieldParams.getDecilength());
            sysField.setSDefaultValue(fieldParams.getDefaultvalue());
            sysField.setBSumField(fieldParams.getSumfield());
            dbType = "Decimal(" + fieldParams.getLength() + "," + fieldParams.getDecilength() + ")";
        } else if (type.equals("Date")) {
            sysField.setSDefaultValue(fieldParams.getDefaultvalue());
            dbType = "Date";
        } else if (type.equals("DateTime")) {
            sysField.setSDefaultValue(fieldParams.getDefaultvalue());
            dbType = "Datetime";
        } else if (type.equals("Int")) {
            sysField.setLLength(fieldParams.getLength());
            sysField.setBSumField(fieldParams.getSumfield());
            sysField.setSDefaultValue(fieldParams.getDefaultvalue());
            dbType = "Int";
        } else if (type.equals("Common")) {
            sysField.setRefSysFieldId(fieldParams.getReffield());
            dbType = "varchar(200)";
        } else if (type.equals("Virtual")) {
        } else if (type.equals("MultiList") || type.equals("List")) {
            dbType = "varchar(200)";
        } else if (type.equals("Reference")) {

        } else if (type.equals("ListTable")) {
            dbType = "Int";
        }

        sysField = updateSysField(sysField, fieldParams);

        SysObject sysObject = sysObjectService.getSysObjectByObjectName(fieldParams.getSobjectname());

        if (!fieldParams.getSobjectname().equals("System/Common")) {
            if (StrUtil.isNotEmpty(dbType) && (ObjectUtil.isNull(fieldParams.getIsattach()) || !fieldParams.getIsattach())) {
                this.addColumnToTable(sysObject.getSDbTable(), dbType, fieldParams.getField(), isautoincrement, isPk, type);
            }
        }
        sysFieldMapper.newSave(sysField);
        Integer lID = sysField.getLID();
        return lID;
    }

    @Override
    public boolean editSave(FieldParams fieldParams) {
        SysField sysField = this.getById(fieldParams.getId());
        sysField.setSName(fieldParams.getName());
        sysField.setSTip(fieldParams.getTip());
        sysField.setLLength(fieldParams.getLength());
        sysField.setLDeciLength(fieldParams.getDecilength());
        sysField.setSTip(fieldParams.getTip());
        sysField.setBRequired(fieldParams.getRequired());
        sysField.setSDefaultValue(fieldParams.getDefaultvalue());
        sysField.setBEnableRTE(fieldParams.getEnablerte());
        sysField.setSEnumOption(fieldParams.getEnumoption());
        sysField.setBReadOnly(fieldParams.getReadonly());
        sysField.setBDisabled(fieldParams.getDisabled());
        sysField.setBMulti(fieldParams.getMulti());
        sysField.setRefSysFieldId(fieldParams.getReffield());
        sysField.setSRefFieldAs(fieldParams.getRefobj());
        sysField.setSFieldAs(fieldParams.getField());
        sysField.setLWidth(fieldParams.getWidth());
        sysField.setSUIType(fieldParams.getUitype());

        sysField = updateSysField(sysField, fieldParams);
        return this.saveOrUpdate(sysField);
    }

    @Override
    public SysField updateSysField(SysField sysField, FieldParams fieldParams) {
        String type = fieldParams.getType();
        if (type.equals("MultiList") || type.equals("List")) {
            String refKey = fieldParams.getRefkey();
            if (StrUtil.isNotEmpty(refKey)) {
                SysObject sysObject = sysObjectService.getSysObjectByObjectName(refKey);

                sysField.setSRefKey(refKey);
                sysField.setSRefIdFieldAs(sysObject.getSIdFieldAs());
                sysField.setSRefNameFieldAs(sysObject.getSNameFieldAs());
            }
            sysField.setSEnumOption(fieldParams.getEnumoption());
        }

        if (type.equals("Reference")) {
            String refobj = fieldParams.getRefobj();
            Integer reffield = fieldParams.getReffield();
            Integer refobjfieldid = fieldParams.getRefobjfieldid();

            if (ObjectUtil.isNotEmpty(refobjfieldid)) {
                SysField sysField2 = sysFieldService.getById(refobjfieldid);
                String sysField2SObjectName = sysField2.getSRefKey();
                if (StrUtil.isEmpty(refobj)) {
                    refobj = sysField2SObjectName;
                }
            }

            SysField sysField1 = sysFieldService.getById(reffield);
            String sFieldAs = sysField1.getSFieldAs();

            sysField.setRefSysFieldId(refobjfieldid);
            sysField.setSRefKey(refobj);
            sysField.setSRefFieldAs(sFieldAs);
            //sysField.setSFieldAs(sFieldAs);
        }

        if (type.equals("ListTable")) {
            String refKey = fieldParams.getRefkey();
            SysObject sysObject = sysObjectService.getSysObjectByObjectName(refKey);

            sysField.setSRefKey(refKey);
            sysField.setSRefIdFieldAs(sysObject.getSIdFieldAs());
            sysField.setSRefNameFieldAs(sysObject.getSNameFieldAs());
        }

        return sysField;
    }

    /**
     * 某字段是否存在于对象之中
     *
     * @param sObjectName
     * @param sFieldName
     * @return
     */
    public Boolean exists(String sObjectName, String sFieldName) {
        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<>();
        sysFieldQueryWrapper.eq("sObjectName", sObjectName);
        sysFieldQueryWrapper.eq("sFieldAs", sFieldName);
        return count(sysFieldQueryWrapper) > 0;
    }

    /**
     * 通过对象名+字段名获取属性
     *
     * @param sObjectName 对象名
     * @param sFieldName  字段名
     * @return SysField
     */
    public SysField getByFieldName(String sObjectName, String sFieldName) {
        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<>();
        sysFieldQueryWrapper.eq("sObjectName", sObjectName);
        sysFieldQueryWrapper.eq("sFieldAs", sFieldName);
        return getOne(sysFieldQueryWrapper);
    }

    /**
     * 通过对象名获取该对象的所有属性
     *
     * @param sObjectName 对象名
     * @return List<SysField>
     */
    public List<SysField> getByObjectName(String sObjectName) {
        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<>();
        sysFieldQueryWrapper.eq("sObjectName", sObjectName);
        return list(sysFieldQueryWrapper);
    }

    /**
     * 通过属性的id获取
     *
     * @param id
     * @return
     */
    public SysField getById(int id) {
        return sysFieldMapper.getFieldById(id);
    }

    public SysField getFieldById(Integer id) {
        return sysFieldMapper.getFieldById(id);
    }

    @Transactional
    public boolean del(Integer id) {
        this.removeById(id);

        //遍历所有的试图
        List<SysList> sysList = sysListService.list();
        for (SysList sysList1 : sysList) {
            String sListSelectId = sysList1.getSListSelectId();
            if (StrUtil.isNotEmpty(sListSelectId)) {
                String[] arrListSelectId = sListSelectId.split(",");
                if (ArrayUtil.contains(arrListSelectId, id.toString())) {
                    ArrayUtil.removeEle(arrListSelectId, id.toString());
                    sListSelectId = arrListSelectId.toString();
                }
            }

            String sAdvancedSearchId = sysList1.getSAdvancedSearchId();
            if (StrUtil.isNotEmpty(sAdvancedSearchId)) {
                String[] arrAdvancedSearchId = sAdvancedSearchId.split(",");
                if (ArrayUtil.contains(arrAdvancedSearchId, id.toString())) {
                    ArrayUtil.removeEle(arrAdvancedSearchId, id.toString());
                    sAdvancedSearchId = arrAdvancedSearchId.toString();
                }
            }

            String sFastSearchId = sysList1.getSFastSearchId();
            if (StrUtil.isNotEmpty(sFastSearchId)) {
                JSONArray arrFastSearch = JSONUtil.parseArray(sFastSearchId);
                if (arrFastSearch.contains(id)) {
                    arrFastSearch.remove(id);
                    sFastSearchId = arrFastSearch.toJSONString(4);
                }
            }

            sysList1.setSListSelectId(sListSelectId);
            sysList1.setSAdvancedSearchId(sAdvancedSearchId);
            sysList1.setSFastSearchId(sFastSearchId);

            sysListService.saveOrUpdate(sysList1);
        }

        List<SysUI> sysUIList = sysUIService.list();
        for (SysUI sysUI : sysUIList) {
            String sConfigJson = sysUI.getSConfigJson();
            JSONArray configArray = JSONUtil.parseArray(sConfigJson);
            for (Object object : configArray) {
                JSONObject configObject = (JSONObject) object;
                for (int k = 1; k < 4; k++) {
                    JSONArray jsonGroupField = configObject.getJSONArray("arrGroupField" + k);
                    for (int j = 0; j < jsonGroupField.size(); j++) {
                        String idObj = jsonGroupField.getStr(j);
                        if (idObj.equals(id.toString())) {
                            jsonGroupField.remove(idObj);
                        }
                    }
                }
            }
            String delConfig = configArray.toJSONString(4);
            sysUI.setSConfigJson(delConfig);

            sysUIService.saveOrUpdate(sysUI);
        }
        return false;
    }

    @Override
    public List<Map<String, Object>> refobjlist(String sobjectname) {
        Map<String, Object> resultMap = null;
        List<Map<String, Object>> resultList = new ArrayList<>();

        QueryWrapper<SysField> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sDataType", "ListTable");
        queryWrapper.eq("sObjectName", sobjectname);
        queryWrapper.isNotNull("sRefKey");

        List<SysField> sysFieldList = this.list(queryWrapper);

        List<String> refList = new ArrayList<>();
        for (SysField sysField : sysFieldList) {
            refList.add(sysField.getSRefKey());
        }

        QueryWrapper<SysObject> sysObjectQueryWrapper = new QueryWrapper<>();
        sysObjectQueryWrapper.select("sObjectName,sName");
        if (CollectionUtil.isNotEmpty(refList)) {
            sysObjectQueryWrapper.in("sObjectName", refList);
            List<SysObject> sysObjectList = sysObjectService.list(sysObjectQueryWrapper);
            for (SysObject sysObject : sysObjectList) {
                resultMap = new HashMap<>();
                resultMap.put("ID", sysObject.getSObjectName());
                resultMap.put("sName", sysObject.getSName());

                resultList.add(resultMap);
            }
        }
        return resultList;
    }

    @Override
    public List<Map<String, Object>> refobjfieldlist(String sobjectname) {
        QueryWrapper<SysField> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("sFieldAs,sName");
        queryWrapper.eq("sObjectName", sobjectname);
        queryWrapper.eq("sDataType", "Reference");
        List<SysField> list = this.list(queryWrapper);

        Map<String, Object> resultMap = null;
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (SysField sysField : list) {
            resultMap = new HashMap<>();
            resultMap.put("ID", sysField.getSFieldAs());
            resultMap.put("sName", sysField.getSName());

            resultList.add(resultMap);
        }

        return resultList;
    }

    @Override
    public List<Map<String, Object>> commonfieldlist() {
        QueryWrapper<SysField> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("lID,sFieldAs,sName");
        queryWrapper.eq("sObjectName", "System/Common");
        List<SysField> list = this.list(queryWrapper);

        Map<String, Object> resultMap = null;
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (SysField sysField : list) {
            resultMap = new HashMap<>();
            resultMap.put("ID", sysField.getLID());
            resultMap.put("sName", sysField.getSName());

            resultList.add(resultMap);
        }

        return resultList;
    }

    @Override
    public void addColumnToTable(String tableName, String dataType, String columnName, Boolean isautoincrement, Boolean isPk, String uiType) {
        if (!columnName.equals("sName") && columnName.equals("lID")) {
            return;
        }
        dataType = dataType.toLowerCase();
        //查找此字段在数据库是否存在
        List<Map<String, Object>> mapList = jdbcTemplateService.isHaveColumn(tableName, columnName);
        if (CollectionUtil.isEmpty(mapList)) {
            jdbcTemplateService.addColumn(tableName, columnName, dataType, uiType, isPk, isautoincrement);
        }
    }

    @Override
    public Map<String, Object> columnlist(String sobjectname) {
        List<String> dbColumnList = new ArrayList<>();
        QueryWrapper<SysObject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sobjectname);

        SysObject sysObject = sysObjectService.getOne(queryWrapper);
        if (ObjectUtil.isNotEmpty(sysObject)) {
            String tablename = sysObject.getSDbTable();

            QueryWrapper<SysField> queryFieldWrapper = new QueryWrapper<>();
            queryFieldWrapper.eq("sObjectName", sobjectname);
            List<SysField> list = sysFieldService.list(queryFieldWrapper);

            List<String> fieldList = new ArrayList<>();
            for (SysField sysField : list) {
                fieldList.add(sysField.getSFieldAs());
            }

            List<Map<String, Object>> mapList = jdbcTemplateService.columnlist(tablename, fieldList.toString());

            for (Map<String, Object> map : mapList) {
                dbColumnList.add((String) map.get("column_name"));
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("columns", dbColumnList);
        return result;
    }

    /**
     * @return
     */
    public SysField getPkField(String sObjectName) {
        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<>();
        sysFieldQueryWrapper.eq("sObjectName", sObjectName);
        sysFieldQueryWrapper.eq("bPrimaryKey", 1);
        return getOne(sysFieldQueryWrapper);
    }

    @Override
    public List<String> attachfieldlist(String sObjectName) {
        Map<String, Object> allList = this.columnlist(sObjectName);
        List<SysField> list = this.getAllFieldByObject(sObjectName);
        List<String> allColumnList = (List<String>) allList.get("columns");

        List<String> needDelList = new ArrayList<>();
        for (String s : allColumnList) {
            for (SysField sysField : list) {
                String field = sysField.getSFieldAs();
                if (field.equals(s)) {
                    if (!needDelList.contains(field)) {
                        needDelList.add(field);
                    }
                    break;
                }
            }
        }
        allColumnList.removeAll(needDelList);


        return allColumnList;
    }

    public Integer getRefFieldId(Integer objId, String fieldName) {
        SysField sysField = sysFieldService.getById(objId);
        String sobjectname = sysField.getSRefKey();

        QueryWrapper<SysField> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sobjectname);
        queryWrapper.eq("sFieldAs", fieldName);

        SysField ref = sysFieldService.getOne(queryWrapper);
        Integer reffieldid = 0;
        if (ObjectUtil.isNotEmpty(ref)) {
            reffieldid = ref.getLID();
        }

        return reffieldid;
    }


    public List<Map<String, Object>> getEnumOptionValue(SysField sysField, String sValue) {
        if (sysField.getSDataType().equals("Common") || sysField.getSDataType().equals("List") || sysField.getSDataType().equals("MultiList")) {
            List<Map<String, Object>> listValue = getEnumOption(sysField);

            if (sysField.getSDataType().equals("MultiList")) {
                String[] arrValue = sValue.replaceAll("(^;|;$)", "").split(";");
                List<Map<String, Object>> listResult = new ArrayList<>();
                for (int i = 0; i < listValue.size(); i++) {
                    for (int j = 0; j < arrValue.length; j++) {
                        if (String.valueOf(listValue.get(i).get("ID")).equals(arrValue[j])) {
                            listResult.add(listValue.get(i));
                        }
                    }
                }

                return listResult;
            } else {
                for (int i = 0; i < listValue.size(); i++) {
                    if (((String) listValue.get(i).get("ID")).equals(sValue)) {
                        List<Map<String, Object>> listResult = new ArrayList<>();
                        listResult.add(listValue.get(i));

                        return listResult;
                    }
                }
            }
        }

        return null;
    }

    /**
     * 如果属性的类型是公共、列表、多选列表，则获取他们的选项
     *
     * @param sysField
     * @return
     */
    public List<Map<String, Object>> getEnumOption(SysField sysField) {
        if (sysField.getSDataType().equals("Common") || sysField.getSDataType().equals("List") || sysField.getSDataType().equals("MultiList")) {
            if (StrUtil.isNotEmpty(sysField.getSRefKey())) {
                SysObject enumSysObject = sysObjectService.getById(sysField.getSRefKey());
                List<Map<String, Object>> listRefObject = jdbcTemplate.queryForList("SELECT " + enumSysObject.getSIdFieldAs() + " AS ID, " + enumSysObject.getSNameFieldAs() + " AS sName"
                        + " FROM `" + enumSysObject.getSDbTable() + "`");
                return listRefObject;
            } else {
                List<Map<String, Object>> listEnumOption = new ArrayList<>();

                String[] lines = sysField.getSEnumOption().split("\n");

                for (String line : lines) {
                    String[] parts = line.split("=");

                    if (parts.length == 2) {
                        String key = parts[0].trim();
                        String value = parts[1].trim();

                        Map<String, Object> mapOption = new HashMap<>();
                        mapOption.put("ID", key);
                        mapOption.put("sName", value);

                        listEnumOption.add(mapOption);
                    }
                }
                return listEnumOption;
            }
        } else {
            return null;
        }
    }

    /**
     * 是否有
     *
     * @param sObjectName
     * @return
     */
    public Boolean hasOwnerId(String sObjectName) {
        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<>();
        sysFieldQueryWrapper.eq("sObjectName", sObjectName);
        sysFieldQueryWrapper.eq("sFieldAs", "OwnerId");
        return count(sysFieldQueryWrapper) > 0;
    }
}
