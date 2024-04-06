package cn.myerm.business.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import cn.myerm.business.param.ListParam;
import cn.myerm.common.config.Businessconfig;
import cn.myerm.common.exception.SystemException;
import cn.myerm.common.service.impl.CommonServiceImpl;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.entity.SysList;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.entity.SysUI;
import cn.myerm.objectbuilder.service.impl.SysFieldServiceImpl;
import cn.myerm.objectbuilder.service.impl.SysListServiceImpl;
import cn.myerm.objectbuilder.service.impl.SysObjectServiceImpl;
import cn.myerm.objectbuilder.service.impl.SysUIServiceImpl;
import cn.myerm.system.entity.*;
import cn.myerm.system.service.impl.*;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.randomNumbers;

/**
 * 基础的业务service层
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
 * @since 2021年4月14日09:15:05
 */
public class BusinessServiceImpl<M extends BaseMapper<T>, T> extends CommonServiceImpl<M, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessServiceImpl.class);

    @Resource
    Businessconfig businessconfig;

    @Resource
    private SysListServiceImpl sysListService;

    @Resource
    private SysFieldServiceImpl sysFieldService;

    @Resource
    private SysObjectServiceImpl sysObjectService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private SysSessionServiceImpl sysSessionService;

    @Resource
    private SysUserServiceImpl sysUserService;

    @Resource
    private SysRoleServiceImpl sysRoleService;

    @Resource
    private SysUIServiceImpl sysUIService;

    @Resource
    private SysAttachServiceImpl sysAttachService;

    @Resource
    private SysRelatedListServiceImpl sysRelatedListService;

    @Resource
    private SysOperatorPermissionServiceImpl sysOperatorPermissionService;

    @Resource
    private SysApproveServiceImpl sysApproveService;

    private SysUser currUser = null;

    /**
     * 获取视图的数据
     *
     * @param listParam
     * @return
     */
    public Map<String, Object> getListData(ListParam listParam) {
        SysList sysList = sysListService.getById(listParam.getListid());
        SysObject sysObject = sysObjectService.getById(listParam.getSobjectname());

        if (!sysList.getSObjectName().equalsIgnoreCase(listParam.getSobjectname())) {
            throw new SystemException("视图所属的对象名与传值sobjectname不匹配");
        }

        //查询构造器
        QueryWrapper<T> objectQueryWrapper = new QueryWrapper<T>();

        /**
         * 准备拼接WHERE
         */
        if (!StrUtil.hasEmpty(listParam.getSelectedids()) && !listParam.getSelectedids().equals("all")) {//如果有筛选，其余的条件作废
            JSONArray arrSelectId = JSONObject.parseArray(listParam.getSelectedids());
            objectQueryWrapper.in(sysObject.getSIdFieldAs(), arrSelectId);
        } else {
            //拼接数据权限
            if (!StrUtil.hasEmpty(sysObject.getDataPowerTypeId())) {
                //有拥有者字段，代表这个对象有数据权限
                if (sysFieldService.exists(listParam.getSobjectname(), "OwnerId")) {
                    List<Integer> listDownlineId = new ArrayList<>();
                    SysUser sysCurrUser = sysSessionService.getCurrUser();
                    List<SysUser> listDownlineUser = new ArrayList<>();
                    if (sysObject.getDataPowerTypeId().equalsIgnoreCase("SysUser")) {
                        listDownlineUser = sysUserService.getDownlineSysUser(sysCurrUser);
                    } else if (sysObject.getDataPowerTypeId().equalsIgnoreCase("SysRole")) {
                        SysRole sysCurrRole = sysRoleService.getById(sysCurrUser.getSysRoleId());
                        listDownlineUser = sysUserService.getDownlineSysUser(sysCurrRole);
                    }

                    if (CollectionUtil.isNotEmpty(listDownlineUser)) {
                        for (SysUser downlineUser : listDownlineUser) {
                            listDownlineId.add(downlineUser.getLID());
                        }
                        objectQueryWrapper.in("OwnerId", listDownlineId);
                    }
                }
            }

            //首先，拼接快速搜索
            if (!StrUtil.hasEmpty(listParam.getFastsearchfield()) && !StrUtil.hasEmpty(listParam.getFastsearchkeyword())) {
                if (sysFieldService.exists(listParam.getSobjectname(), listParam.getFastsearchfield())) {
                    objectQueryWrapper.like("`" + listParam.getFastsearchfield() + "`", listParam.getFastsearchkeyword());
                }
            }

            //其次，拼接高级搜索
            if (!StrUtil.hasEmpty(listParam.getAdvsearchjson())) {
                try {
                    JSONArray jsonArray = JSONArray.parseArray(listParam.getAdvsearchjson());
                    for (int i = 0; i < jsonArray.size(); i++) {
                        String sFieldName = jsonArray.getJSONObject(i).getString("field");
                        String sQueryValue = jsonArray.getJSONObject(i).getString("value");
                        if (StrUtil.hasEmpty(sQueryValue) || sQueryValue.equals("全部")) {
                            continue;
                        }

                        SysField queryField = sysFieldService.getByFieldName(listParam.getSobjectname(), sFieldName);
                        if (queryField == null) {
                            throw new SystemException(listParam.getSobjectname() + "的" + sFieldName + "属性不存在");
                        } else {
                            switch (queryField.getSDataType()) {
                                //日期型，日期时间型
                                case "Date":
                                case "DateTime":
                                    String sStartDate = null, sEndDate = null;
                                    if (sQueryValue.equals("今天")) {
                                        sStartDate = DateUtil.today() + " 00:00:00";
                                        sEndDate = DateUtil.today() + " 23:59:59";
                                    } else if (sQueryValue.equals("昨天")) {
                                        sStartDate = DateUtil.yesterday().toDateStr() + " 00:00:00";
                                        sEndDate = DateUtil.yesterday().toDateStr() + " 23:59:59";
                                    } else if (sQueryValue.equals("本周")) {
                                        sStartDate = DateUtil.beginOfWeek(DateUtil.date()).toString();
                                        sEndDate = DateUtil.endOfWeek(DateUtil.date()).toString();
                                    } else if (sQueryValue.equals("本月")) {
                                        sStartDate = DateUtil.beginOfMonth(DateUtil.date()).toString();
                                        sEndDate = DateUtil.endOfMonth(DateUtil.date()).toString();
                                    } else if (sQueryValue.equals("上月")) {
                                        sStartDate = DateUtil.beginOfMonth(DateUtil.lastMonth()).toString();
                                        sEndDate = DateUtil.endOfMonth(DateUtil.lastMonth()).toString();
                                    } else if (sQueryValue.equals("自定义")) {
                                        JSONArray arrExtra = jsonArray.getJSONObject(i).getJSONArray("extraValue");

                                        if (arrExtra != null && !StrUtil.hasEmpty(arrExtra.getString(0))) {
                                            sStartDate = arrExtra.getString(0) + " 00:00:00";
                                        }

                                        if (arrExtra != null && !StrUtil.hasEmpty(arrExtra.getString(1))) {
                                            sEndDate = arrExtra.getString(1) + " 23:59:59";
                                        }
                                    }

                                    if (!StrUtil.hasEmpty(sStartDate)) {
                                        objectQueryWrapper.ge(sFieldName, sStartDate);
                                    }

                                    if (!StrUtil.hasEmpty(sEndDate)) {
                                        objectQueryWrapper.le(sFieldName, sEndDate);
                                    }
                                    break;
                                case "MultiList":
                                    String sExtraValue = jsonArray.getJSONObject(i).getString("extraValue");
                                    objectQueryWrapper.like(sFieldName, ";" + sExtraValue + ";");
                                    break;
                                case "List":
                                case "ListTable":
                                    sExtraValue = jsonArray.getJSONObject(i).getString("extraValue");

                                    if (sExtraValue.contains(",")) {
                                        objectQueryWrapper.in(sFieldName, sExtraValue.split(","));
                                    } else {
                                        objectQueryWrapper.eq(sFieldName, sExtraValue);
                                    }

                                    break;
                                case "Reference":
                                case "Virtual":
                                    break;
                                case "Text":
                                case "TextArea":
                                    objectQueryWrapper.like(sFieldName, sQueryValue);
                                    break;
                                case "Bool":
                                    if (sQueryValue.equals("是")) {
                                        objectQueryWrapper.eq(sFieldName, 1);
                                    } else {
                                        objectQueryWrapper.eq(sFieldName, 0);
                                    }
                                    break;
                                case "AttachFile":

                                    break;
                                default:
                                    objectQueryWrapper.eq(sFieldName, sQueryValue);
                            }
                        }
                    }
                } catch (JSONException e) {
                    throw new SystemException(e.getMessage());
                }
            }

            //如果有传入keyword
            if (!StrUtil.hasEmpty(listParam.getKeyword())) {
                objectQueryWrapper.like(sysObject.getSNameFieldAs(), listParam.getKeyword());
            }

            //拼接List.sWhereSql字段
            if (!StrUtil.hasEmpty(sysList.getSWhereSql())) {
                objectQueryWrapper.apply(sysList.getSWhereSql());
            }

            //拼接二次开发的SQL，代码传入
            String sAppendWhereSql = appendWhereSql(listParam);
            if (!StrUtil.hasEmpty(sAppendWhereSql)) {
                objectQueryWrapper.apply(sAppendWhereSql);
            }

            //处理相关信息列表的条件
            if (!StrUtil.hasEmpty(listParam.getRelatedid())) {
                SysRelatedList sysRelatedList = sysRelatedListService.getById(listParam.getRelatedid());
                objectQueryWrapper.eq(sysRelatedList.getSRelatedField(), listParam.getObjectid());
            }
        }

        //计算记录数
        int lCount = this.count(objectQueryWrapper);

        /**
         * 准备拼接SELECT语句
         */

        List<SysField> listSelectField = new ArrayList<>();
        QueryWrapper<SysField> sysFieldQueryWrapper = new QueryWrapper<SysField>();
        sysFieldQueryWrapper.eq("sObjectName", listParam.getSobjectname());
        if (listParam.getDispcol() == null) {
            String sListSelectId = sysList.getSListSelectId();
            if (StrUtil.isNotEmpty(sListSelectId)) {
                sysFieldQueryWrapper.inSql("lID", sListSelectId);
            }
        } else {
            sysFieldQueryWrapper.in("sFieldAs", listParam.getDispcol());
        }
        listSelectField = sysFieldService.list(sysFieldQueryWrapper);

        //统计数字属性
        List<Map<String, Object>> listMapSumFieldVal = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(listSelectField)) {
            for (SysField sf : listSelectField) {
                if (!ObjectUtil.isEmpty(sf.getBSumField()) && sf.getBSumField()) {
                    objectQueryWrapper.select("'" + sf.getSFieldAs() + "' AS sFieldAs, SUM(" + sf.getSFieldAs() + ") AS fValue");
                    listMapSumFieldVal.add(getMap(objectQueryWrapper));
                }
            }
        }

        String sComm = "";
        StringBuilder sSelectSql = new StringBuilder();
        for (SysField sf : listSelectField) {
            if (sf.getSDataType().equals("Common") || sf.getSDataType().equals("Reference")) {
                sf.setReferenceField(sysFieldService.getById(sf.getRefSysFieldId()));
            }

            if (sf.getSDataType().equals("Reference")) {
                sf.setReferenceSourceField(sysFieldService.getByFieldName(sf.getSRefKey(), sf.getSRefFieldAs()));
            }

            if (sf.getSDataType().equalsIgnoreCase("Reference")) {
                sSelectSql.append(sComm + "NULL AS `" + sf.getSFieldAs() + "`");
                sSelectSql.append(sComm + "`" + sf.getReferenceField().getSFieldAs() + "`");
            } else if (sf.getSDataType().equalsIgnoreCase("Virtual")) {//虚拟属性
                sSelectSql.append(sComm + "NULL AS `" + sf.getSFieldAs() + "`");
            } else {
                sSelectSql.append(sComm + "`" + sf.getSFieldAs() + "`");
            }

            sComm = ", ";
        }
        //强制加入ID和Name字段
        sSelectSql.append(",").append("`" + sysObject.getSIdFieldAs() + "` AS ID");
        sSelectSql.append(",").append("`" + sysObject.getSNameFieldAs() + "` AS sName");
        objectQueryWrapper.select(sSelectSql.toString());

        //拼接List.sGroupBySql
        if (!StrUtil.hasEmpty(sysList.getSGroupBySql())) {
            objectQueryWrapper.groupBy(sysList.getSGroupBySql());
        }

        //拼接List.sOrderBySql
        StringBuilder sLastSql = new StringBuilder();
        if (!StrUtil.hasEmpty(listParam.getOrderby())) {
            JSONObject orderByJson = JSONObject.parseObject(listParam.getOrderby());
            if (!StrUtil.hasEmpty(orderByJson.getString("field"))) {
                if (orderByJson.getString("order").equals("ascending")) {
                    sLastSql.append(" ORDER BY " + orderByJson.getString("field"));
                } else if (orderByJson.getString("order").equals("descending")) {
                    sLastSql.append(" ORDER BY " + orderByJson.getString("field") + " DESC");
                }
            } else if (!StrUtil.hasEmpty(sysList.getSOrderBySql())) {
                sLastSql.append(" ORDER BY " + sysList.getSOrderBySql());
            }
        } else if (!StrUtil.hasEmpty(sysList.getSOrderBySql())) {
            sLastSql.append(" ORDER BY " + sysList.getSOrderBySql());
        }

        //是否可以分页
        int lPageLimit = listParam.getPagelimit();
        if (listParam.getCanpage() != null) {
            if (listParam.getCanpage().compareTo(1) == 0) {
                sLastSql.append(" LIMIT " + lPageLimit + " OFFSET " + (listParam.getPage() - 1) * lPageLimit);
            }
        } else if (sysList.getBCanPage()) {
            sLastSql.append(" LIMIT " + lPageLimit + " OFFSET " + (listParam.getPage() - 1) * lPageLimit);
        }

        objectQueryWrapper.last(sLastSql.toString());

        //利用最终拼接完的sql语句出查询结果集
        List<Map<String, Object>> listObjectData = listMaps(objectQueryWrapper);

        //把有关联性质的属性按关联的对象集合起来，为后面做数据集合映射做好准备
        Map<String, List<SysField>> mapRefField = new HashMap<>();
        for (SysField selectField : listSelectField) {
            switch (selectField.getSDataType()) {
                case "ListTable":
                case "List":
                case "MultiList":
                case "Common":
                case "Reference":
                case "AttachFile":

                    List<SysField> listRefSysField = mapRefField.get(selectField.getSRefKey());
                    if (listRefSysField == null) {
                        listRefSysField = new ArrayList<>();
                    }

                    listRefSysField.add(selectField);

                    if (selectField.getSDataType().equals("Common")) {
                        mapRefField.put(selectField.getReferenceField().getSRefKey(), listRefSysField);
                    } else {
                        mapRefField.put(selectField.getSRefKey(), listRefSysField);
                    }

                    break;
            }
        }

        //组装关联的select字段
        Map<String, String> mapRefSelect = new HashMap<>();
        for (String sRefKey : mapRefField.keySet()) {
            List<SysField> listRefSysField = mapRefField.get(sRefKey);
            for (SysField refSysField : listRefSysField) {
                if (refSysField.getSDataType().equals("MultiList") || refSysField.getSDataType().equals("List")) {
                    if (!StrUtil.hasEmpty(refSysField.getSEnumOption())) {//自定义的选项
                        continue;
                    }
                } else if (refSysField.getSDataType().equals("Common")) {
                    if (!StrUtil.hasEmpty(refSysField.getReferenceField().getSEnumOption())) {//自定义的选项
                        continue;
                    }
                }

                String sSelect = mapRefSelect.get(sRefKey);
                String sFieldName = "";
                if (refSysField.getSDataType().equals("Reference")) {//引用的属性比较特殊
                    sFieldName = refSysField.getSRefFieldAs();
                } else if (refSysField.getSDataType().equals("Common")) {
                    sFieldName = refSysField.getReferenceField().getSRefIdFieldAs() + " AS ID, " + refSysField.getReferenceField().getSRefNameFieldAs();
                } else {
                    sFieldName = refSysField.getSRefIdFieldAs() + " AS ID, " + refSysField.getSRefNameFieldAs();
                }

                if (StrUtil.hasEmpty(sSelect)) {
                    mapRefSelect.put(sRefKey, sFieldName);
                } else {
                    mapRefSelect.put(sRefKey, sSelect + "," + sFieldName);
                }

                if (refSysField.getSDataType().equals("Reference")) {
                    switch (refSysField.getReferenceSourceField().getSDataType()) {
                        case "ListTable":
                        case "AttachFile":
                            mapRefSelect.put(refSysField.getReferenceSourceField().getSRefKey(), sSelect + ","
                                    + refSysField.getReferenceSourceField().getSRefIdFieldAs() + " AS ID, "
                                    + refSysField.getReferenceSourceField().getSRefNameFieldAs());
                            break;
                    }
                }
            }
        }

        //获取关联对象的ID集合，为后面重新赋值做好准备
        Map<String, String> mapRefID = new HashMap<>();
        for (Map<String, Object> mapObjectData : listObjectData) {
            for (String sRefKey : mapRefField.keySet()) {
                List<SysField> listRefSysField = mapRefField.get(sRefKey);
                for (SysField refSysField : listRefSysField) {

                    String sRefId = String.valueOf(mapObjectData.get(refSysField.getSFieldAs()));
                    if (refSysField.getSDataType().equals("Reference")) {
                        sRefId = String.valueOf(mapObjectData.get(refSysField.getReferenceField().getSFieldAs()));
                    }

                    if (StrUtil.isEmpty(sRefId)) {
                        continue;
                    }

                    if (refSysField.getSDataType().equals("MultiList")) {
                        sRefId = sRefId.replaceAll("(^;|;$)", "").replaceAll(";", "','");
                    } else if (refSysField.getSDataType().equals("AttachFile")) {
                        sRefId = sRefId.replaceAll(",", "','");
                    }

                    String sRefIds = mapRefID.get(sRefKey);
                    if (StrUtil.hasEmpty(sRefIds)) {
                        mapRefID.put(sRefKey, "'" + sRefId + "'");
                    } else {
                        mapRefID.put(sRefKey, sRefIds + ",'" + sRefId + "'");
                    }

                }
            }
        }

        //引用过来的字段带有参照，列表，多选列表型，需要再进一步进行提取ID，然后取值再赋值回去
        //Map<String, Object>


        //ID集合完毕，准备查询关联对象的数据，为后面组装做准备
        Map<String, Object> mapRefObject = new HashMap<>();
        for (String sRefKey : mapRefSelect.keySet()) {
            SysObject sysObjectRef = sysObjectService.getById(sRefKey);

            Map<String, Object> refResultIndexById = new HashMap<>();
            List<Map<String, Object>> listRefObject;
            if (sRefKey.equals("System/SysAttach")) {
                listRefObject = jdbcTemplate.queryForList("SELECT *, lID AS ID "
                        + " FROM `" + sysObjectRef.getSDbTable() + "`"
                        + " WHERE " + sysObjectRef.getSIdFieldAs() + " IN (" + mapRefID.get(sRefKey) + ")");
            } else {
                listRefObject = jdbcTemplate.queryForList("SELECT " + sysObjectRef.getSIdFieldAs() + " AS ID, " + mapRefSelect.get(sRefKey)
                        + " FROM `" + sysObjectRef.getSDbTable() + "`"
                        + " WHERE " + sysObjectRef.getSIdFieldAs() + " IN (" + mapRefID.get(sRefKey) + ")");
            }

            for (Map<String, Object> refObject : listRefObject) {
                refResultIndexById.put(String.valueOf(refObject.get("ID")), refObject);
            }

            mapRefObject.put(sRefKey, refResultIndexById);
        }

        //从ID结果集中，把引用的数据，再次聚集ID
        HashMap<String, Object> mapReferenceId = new HashMap<>();
        for (String sRefKey : mapRefField.keySet()) {
            List<SysField> listRefSysField = mapRefField.get(sRefKey);
            for (SysField refSysField : listRefSysField) {
                if (refSysField.getSDataType().equals("Reference")) {
                    //如果引用过来的字段是参照和附件，则需要把ID再次聚集
                    if (refSysField.getReferenceSourceField().getSDataType().equals("ListTable")
                            || refSysField.getReferenceSourceField().getSDataType().equals("AttachFile")) {
                        Map<String, Object> mapReferenceData = (Map<String, Object>) mapRefObject.get(sRefKey);

                        for (Map.Entry<String, Object> referenceData : mapReferenceData.entrySet()) {
                            Map<String, Object> value = (Map<String, Object>) referenceData.getValue();

                            String ids = (String) mapReferenceId.get(refSysField.getSFieldAs());
                            if (StrUtil.isEmpty(ids)) {
                                mapReferenceId.put(refSysField.getSFieldAs(), value.get(refSysField.getReferenceSourceField().getSFieldAs()));
                            } else if (value.get(refSysField.getReferenceSourceField().getSFieldAs()) != null) {
                                String o = (String) value.get(refSysField.getReferenceSourceField().getSFieldAs());
                                if (StrUtil.isNotEmpty(o)) {
                                    mapReferenceId.put(refSysField.getSFieldAs(), ids + "," + o);
                                } else {
                                    mapReferenceId.put(refSysField.getSFieldAs(), ids);
                                }
                            }
                        }
                    }
                }
            }
        }

        //此时已经拿到了引用的引用ID数据，开始做查询准备
        for (String sRefKey : mapRefField.keySet()) {
            List<SysField> listRefSysField = mapRefField.get(sRefKey);
            for (SysField refSysField : listRefSysField) {
                if (refSysField.getSDataType().equals("Reference")) {

                    if (refSysField.getReferenceSourceField().getSRefKey() == null) {
                        continue;
                    }

                    SysObject sysObjectRef = sysObjectService.getById(refSysField.getReferenceSourceField().getSRefKey());

                    String ids = (String) mapReferenceId.get(refSysField.getSFieldAs());
                    if (StrUtil.isEmpty(ids)) {
                        ids = "-1";
                    }

                    Map<String, Object> refResultIndexById = new HashMap<>();
                    List<Map<String, Object>> listRefObject = null;
                    if (refSysField.getReferenceSourceField().getSRefKey().equals("System/SysAttach")) {
                        listRefObject = jdbcTemplate.queryForList("SELECT *, lID AS ID "
                                + " FROM `" + sysObjectRef.getSDbTable() + "`"
                                + " WHERE " + sysObjectRef.getSIdFieldAs() + " IN (" + ids + ")");
                    } else {
                        listRefObject = jdbcTemplate.queryForList("SELECT " + sysObjectRef.getSIdFieldAs() + " AS ID, " + sysObjectRef.getSNameFieldAs()
                                + " FROM `" + sysObjectRef.getSDbTable() + "`"
                                + " WHERE " + sysObjectRef.getSIdFieldAs() + " IN (" + ids + ")");
                    }

                    for (Map<String, Object> refObject : listRefObject) {
                        refResultIndexById.put(String.valueOf(refObject.get("ID")), refObject);
                    }

                    Map<String, Object> o = (Map<String, Object>) mapRefObject.get(sRefKey);
                    if (o != null) {//原来有值，要追加
                        o.putAll(refResultIndexById);
                        mapRefObject.put(sRefKey, o);
                    } else {
                        mapRefObject.put(sRefKey, refResultIndexById);
                    }
                }
            }
        }

        //重新组装数据关联的属性，格式化数据
        for (Map<String, Object> mapObjectData : listObjectData) {
            for (String sRefKey : mapRefField.keySet()) {

                Map<String, Object> refResultIndexById = (Map<String, Object>) mapRefObject.get(sRefKey);

                List<SysField> listRefSysField = mapRefField.get(sRefKey);

                for (SysField refSysField : listRefSysField) {
                    if (refSysField.getSDataType().equals("Reference")) {//引用的属性要特别对待
                        String sRefId = String.valueOf(mapObjectData.get(refSysField.getReferenceField().getSFieldAs()));
                        Map<String, Object> refResult = (Map<String, Object>) refResultIndexById.get(sRefId);
                        if (refResult != null) {
                            mapObjectData.put(refSysField.getSFieldAs(), refResult.get(refSysField.getSRefFieldAs()));
                        } else {
                            mapObjectData.put(refSysField.getSFieldAs(), null);
                        }
                    }
                }

                for (SysField refField : listRefSysField) {

                    SysField refSysField = null;
                    try {
                        refSysField = refField.clone();
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }

                    if (mapObjectData.get(refSysField.getSFieldAs()) == null) {
                        continue;
                    }

                    //引用的属性，把自己伪装成普通属性，这样可以正常的赋值
                    if (refSysField.getSDataType().equals("Reference")) {
                        SysField referenceField = refSysField.getReferenceSourceField();
                        refSysField.setSDataType(referenceField.getSDataType());
                        refSysField.setSUIType(referenceField.getSUIType());
                        refSysField.setSRefKey(referenceField.getSRefKey());
                        refSysField.setSRefIdFieldAs(referenceField.getSRefIdFieldAs());
                        refSysField.setSRefNameFieldAs(referenceField.getSRefNameFieldAs());
                        refSysField.setSEnumOption(referenceField.getSEnumOption());
                    }

                    if (refSysField.getSDataType().equals("MultiList")) {
                        String sRefId = String.valueOf(mapObjectData.get(refSysField.getSFieldAs()));
                        if (!StrUtil.hasEmpty(sRefId)) {
                            String[] arrRefId = sRefId.replaceAll("(^;|;$)", "").split(";");
                            List<Map<String, Object>> ListMapOption = new ArrayList<>();
                            if (!StrUtil.hasEmpty(refSysField.getSEnumOption())) {
                                for (String sKey : arrRefId) {
                                    Map<String, Object> mapOption = new HashMap<>();
                                    mapOption.put("ID", sKey);
                                    mapOption.put("sName", refSysField.getOptionValue(sKey));

                                    if (mapOption.get("sName") != null) {
                                        ListMapOption.add(mapOption);
                                    }
                                }
                            } else {
                                for (String sKey : arrRefId) {
                                    Map<String, Object> o = (Map<String, Object>) refResultIndexById.get(sKey);
                                    if (o != null) {
                                        Map<String, Object> mapOption = new HashMap<>();
                                        mapOption.put("ID", sKey);
                                        mapOption.put("sName", o.get("sName"));

                                        if (mapOption.get("sName") != null) {
                                            ListMapOption.add(mapOption);
                                        }
                                    }
                                }
                            }

                            mapObjectData.put(refSysField.getSFieldAs(), ListMapOption);
                        }
                    } else if (refSysField.getSDataType().equals("List")) {
                        String sRefId = String.valueOf(mapObjectData.get(refSysField.getSFieldAs()));
                        if (!StrUtil.hasEmpty(sRefId)) {
                            Map<String, Object> mapOption = new HashMap<>();
                            if (!StrUtil.hasEmpty(refSysField.getSEnumOption())) {
                                mapOption.put("ID", sRefId);
                                mapOption.put("sName", refSysField.getOptionValue(sRefId));
                            } else {
                                mapOption.put("ID", sRefId);
                                mapOption.put("sName", ((Map<String, Object>) refResultIndexById.get(sRefId)).get("sName"));
                            }

                            if (mapOption.get("sName") != null) {
                                mapObjectData.put(refSysField.getSFieldAs(), mapOption);
                            } else {
                                mapObjectData.put(refSysField.getSFieldAs(), null);
                            }
                        }
                    } else if (refSysField.getSDataType().equals("Common")) {
                        String sRefId = String.valueOf(mapObjectData.get(refSysField.getSFieldAs()));
                        if (!StrUtil.hasEmpty(sRefId)) {
                            if (refSysField.getReferenceField().getSDataType().equals("List")) {
                                Map<String, Object> mapOption = new HashMap<>();
                                if (!StrUtil.hasEmpty(refSysField.getReferenceField().getSEnumOption())) {
                                    mapOption.put("ID", sRefId);
                                    mapOption.put("sName", refSysField.getReferenceField().getOptionValue(sRefId));
                                } else {
                                    mapOption.put("ID", sRefId);
                                    mapOption.put("sName", ((Map<String, Object>) refResultIndexById.get(sRefId)).get("sName"));
                                }

                                if (mapOption.get("sName") != null) {
                                    mapObjectData.put(refSysField.getSFieldAs(), mapOption);
                                } else {
                                    mapObjectData.put(refSysField.getSFieldAs(), null);
                                }
                            } else {
                                String[] arrRefId = sRefId.replaceAll("(^;|;$)", "").split(";");
                                List<Map<String, Object>> ListMapOption = new ArrayList<>();
                                if (!StrUtil.hasEmpty(refSysField.getReferenceField().getSEnumOption())) {
                                    for (String sKey : arrRefId) {
                                        Map<String, Object> mapOption = new HashMap<>();
                                        mapOption.put("ID", sKey);
                                        mapOption.put("sName", refSysField.getReferenceField().getOptionValue(sKey));

                                        if (mapOption.get("sName") != null) {
                                            ListMapOption.add(mapOption);
                                        }
                                    }
                                } else {
                                    for (String sKey : arrRefId) {
                                        Map<String, Object> mapOption = new HashMap<>();
                                        mapOption.put("ID", sKey);
                                        mapOption.put("sName", ((Map<String, Object>) refResultIndexById.get(sKey)).get("sName"));

                                        if (mapOption.get("sName") != null) {
                                            ListMapOption.add(mapOption);
                                        }
                                    }
                                }

                                mapObjectData.put(refSysField.getSFieldAs(), ListMapOption);
                            }
                        }
                    } else if (refSysField.getSDataType().equals("AttachFile")) {
                        String sSysAttachId = String.valueOf(mapObjectData.get(refSysField.getSFieldAs()));

                        if (StrUtil.isNotEmpty(sSysAttachId)) {
                            String[] arrSysAttachId = sSysAttachId.split(",");

                            List<Map<String, Object>> listMapAttach = new ArrayList<>();
                            for (String sKey : arrSysAttachId) {
                                Map<String, Object> mapSysAttach = (Map<String, Object>) refResultIndexById.get(sKey);
                                listMapAttach.add(mapSysAttach);
                            }
                            mapObjectData.put(refSysField.getSFieldAs(), listMapAttach);
                        }
                    } else if (refSysField.getSDataType().equals("ListTable")) {
                        String sRefId = String.valueOf(mapObjectData.get(refSysField.getSFieldAs()));
                        mapObjectData.put(refSysField.getSFieldAs(), refResultIndexById.get(sRefId));
                    }
                }
            }
        }

        handleListData(listObjectData);

        //返回数据
        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("arrData", formatListData(listObjectData));
        mapResult.put("arrSumField", listMapSumFieldVal);
        mapResult.put("lCount", lCount);

        if (listParam.getPagelimit() * listParam.getPage() >= lCount) {
            mapResult.put("ismore", false);
        } else {
            mapResult.put("ismore", true);
        }

        return mapResult;
    }

    private void formatFieldValue() {

    }

    /**
     * 处理列表数据
     *
     * @param listObjectData
     */
    protected void handleListData(List<Map<String, Object>> listObjectData) {
    }

    /**
     * 删除
     *
     * @param listParam 客户端传值
     */
    @Transactional
    public void del(ListParam listParam) {
        SysObject sysObject = sysObjectService.getById(listParam.getSobjectname());

        //设置全部出来，不要分页
        listParam.setCanpage(0);

        //只要id和name字段
        List<String> listDispCol = new ArrayList<>();
        listDispCol.add(sysObject.getSIdFieldAs());
        listDispCol.add(sysObject.getSNameFieldAs());
        listParam.setDispcol(listDispCol);

        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        for (Map<String, Object> mapObjectData : listMapObjectData) {
            Map<String, Object> data = (Map<String, Object>) mapObjectData.get("data");
            String objectId = String.valueOf(data.get(sysObject.getSIdFieldAs()));
            if (hasDataPermit(listParam.getSobjectname(), objectId, "del")) {
                beforeDel(sysObject, objectId);
                removeById(objectId);
                afterDel(sysObject, objectId);
            } else {
                throw new SystemException("对不起，您没有删除ID为" + mapObjectData.get(sysObject.getSIdFieldAs()) + "的数据权限");
            }
        }

        afterDel(sysObject);
    }

    @Transactional
    public void alloc(ListParam listParam) {
        SysObject sysObject = sysObjectService.getById(listParam.getSobjectname());

        SysField field = sysFieldService.getByFieldName(listParam.getSobjectname(), "OwnerId");
        if (field == null) {
            field = sysFieldService.getByFieldName(listParam.getSobjectname(), "NewUserId");
        }

        //设置全部出来，不要分页
        listParam.setCanpage(0);

        //只要id和name字段
        List<String> listDispCol = new ArrayList<>();
        listDispCol.add(sysObject.getSIdFieldAs());
        listDispCol.add(sysObject.getSNameFieldAs());
        listParam.setDispcol(listDispCol);

        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        for (Map<String, Object> mapObjectData : listMapObjectData) {
            Map<String, Object> data = (Map<String, Object>) mapObjectData.get("data");
            String objectId = String.valueOf(data.get(sysObject.getSIdFieldAs()));

            String sSql = "UPDATE `" + sysObject.getSDbTable() + "` SET " + field.getSFieldAs() + "='" + listParam.getKeyword() + "' WHERE " + sysObject.getSIdFieldAs() + "='" + objectId + "'";
            jdbcTemplate.execute(sSql);

            afterAlloc(Integer.parseInt(objectId), Integer.parseInt(listParam.getKeyword()));
        }
    }

    public void afterAlloc(Integer objectId, Integer AllocUserId) {

    }

    /**
     * 处理分类tab，进行拼接sql
     *
     * @param listParam 客户端传值
     */
    public String appendWhereSql(ListParam listParam) {
        return "1>0";
    }

    /**
     * 获取编辑的数据
     *
     * @param sObjectName
     * @param ParentId
     * @return
     */
    public List<Map<String, Object>> getSubObjectData(String sObjectName, String ParentId) {
        SysObject sysObject = sysObjectService.getById(sObjectName);
        List<SysField> listSysField = sysFieldService.getByObjectName(sObjectName);
        List<Map<String, Object>> listResultSubObjectData = new ArrayList<>();

        QueryWrapper<T> subQueryWrapper = new QueryWrapper<>();
        subQueryWrapper.eq(sysObject.getSParentIdFieldAs(), ParentId);
        List<Map<String, Object>> listSubObjectData = listMaps(subQueryWrapper);
        for (Map<String, Object> subObjectData : listSubObjectData) {
            subObjectData.put("ID", subObjectData.get(sysObject.getSIdFieldAs()));
            listResultSubObjectData.add(formatUiObjectData(listSysField, subObjectData));
        }

        return listResultSubObjectData;
    }

    /**
     * 获取详情的数据
     *
     * @param sObjectName 对象名
     * @param ObjectId    详情ID
     * @return Map<String, Object>
     */
    public Map<String, Object> getViewData(String sObjectName, String ObjectId) {

        //获取格式化的对象数据
        Map<String, Object> objectData = getOneObjectData(sObjectName, ObjectId);

        Map<String, Object> mapResult = new HashMap<>();
        mapResult.put("data", objectData);

        //获取子对象信息，为了能显示相关的视图列表
        QueryWrapper<SysRelatedList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.orderByAsc("lIndex");
        List<SysRelatedList> listSysRelatedList = sysRelatedListService.list(queryWrapper);
        for (SysRelatedList sysRelatedList : listSysRelatedList) {
            SysObject sysObjectByObjectName = sysObjectService.getSysObjectByObjectName(sysRelatedList.getSRelatedObjectName());
            sysRelatedList.setOpera(getOperationPermit(sysObjectByObjectName));
        }
        mapResult.put("relatedlist", listSysRelatedList);

        mapResult.put("btn", getViewUiBtn(sObjectName, ObjectId));
        mapResult.put("approve", sysApproveService.getApprove(sObjectName, ObjectId));

        return mapResult;
    }

    /**
     * 通过属性的配置，格式化数据
     *
     * @param listSysField
     * @param objectData
     * @return
     */
    private Map<String, Object> formatUiObjectData(List<SysField> listSysField, Map<String, Object> objectData) {
        //先处理引用的字段
        for (SysField sysField : listSysField) {
            if (sysField.getSDataType().equals("Reference")) {
                SysField refSysField = sysFieldService.getById(sysField.getRefSysFieldId());
                if (objectData.get(refSysField.getSFieldAs()) == null) {
                    objectData.put(sysField.getSFieldAs(), null);
                } else {
                    try {
                        SysObject fieldSysObject = sysObjectService.getById(refSysField.getSRefKey());
                        Map<String, Object> mapRefResult = jdbcTemplate.queryForMap("SELECT `" + sysField.getSRefFieldAs() +
                                "` FROM " + fieldSysObject.getSDbTable() +
                                " WHERE " + fieldSysObject.getSIdFieldAs() +
                                "='" + objectData.get(refSysField.getSFieldAs()) + "'");

                        //查出被引用的属性
                        SysField refSourceSysField = sysFieldService.getByFieldName(sysField.getSRefKey(), sysField.getSRefFieldAs());
                        String sValue = String.valueOf(mapRefResult.get(sysField.getSRefFieldAs()));
                        switch (refSourceSysField.getSDataType()) {
                            case "MultiList":
                                objectData.put(sysField.getSFieldAs(), sysFieldService.getEnumOptionValue(refSourceSysField, sValue));
                                break;
                            case "List":
                                objectData.put(sysField.getSFieldAs(), sysFieldService.getEnumOptionValue(refSourceSysField, sValue).get(0));
                                break;
                            case "LisTable":
                                SysObject sourceRefFieldSysObject = sysObjectService.getById(refSourceSysField.getSRefKey());
                                Map<String, Object> mapSourceRefResult = jdbcTemplate.queryForMap("SELECT `" + refSourceSysField.getSRefIdFieldAs() + "` AS ID,`" + refSourceSysField.getSRefNameFieldAs() + "` AS sName" +
                                        " FROM " + sourceRefFieldSysObject.getSDbTable() +
                                        " WHERE " + sourceRefFieldSysObject.getSIdFieldAs() +
                                        "='" + sValue + "'");
                                objectData.put(sysField.getSFieldAs(), mapSourceRefResult);
                                break;
                            default:
                                objectData.put(sysField.getSFieldAs(), sValue);
                        }
                    } catch (EmptyResultDataAccessException e) {
                        objectData.put(sysField.getSFieldAs(), null);
                    }
                }
            }
        }

        //处理其他关联性质的属性
        SysObject fieldSysObject;
        for (SysField sysField : listSysField) {
            switch (sysField.getSDataType()) {
                case "AttachFile":
                    String sAttachId = String.valueOf(objectData.get(sysField.getSFieldAs()));
                    if (StrUtil.hasEmpty(sAttachId)) {
                        break;
                    }

                    String[] arrAttachId = sAttachId.split(",");
                    String sAttachIdLinked = String.join(",", arrAttachId);

                    QueryWrapper<SysAttach> sysAttachQueryWrapper = new QueryWrapper<>();
                    sysAttachQueryWrapper.inSql("lID", sAttachIdLinked);
                    objectData.put(sysField.getSFieldAs(), sysAttachService.list(sysAttachQueryWrapper));

                    break;
                case "ListTable":
                    if (ObjectUtil.isEmpty(objectData) || objectData.get(sysField.getSFieldAs()) == null) {
                        break;
                    }

                    try {
                        fieldSysObject = sysObjectService.getById(sysField.getSRefKey());
                        Map<String, Object> mapRefResult = jdbcTemplate.queryForMap("SELECT `" + sysField.getSRefIdFieldAs() + "` AS ID,`" + sysField.getSRefNameFieldAs() + "` AS sName" +
                                " FROM " + fieldSysObject.getSDbTable() +
                                " WHERE " + fieldSysObject.getSIdFieldAs() +
                                "='" + objectData.get(sysField.getSFieldAs()) + "'");
                        objectData.put(sysField.getSFieldAs(), mapRefResult);
                    } catch (EmptyResultDataAccessException e) {
                        objectData.put(sysField.getSFieldAs(), null);
                    }

                    break;
                case "List":
                case "MultiList":
                    String sRefId = String.valueOf(objectData.get(sysField.getSFieldAs()));
                    if (objectData.get(sysField.getSFieldAs()) == null || StrUtil.hasEmpty(sRefId)) {
                        break;
                    }

                    fieldSysObject = sysObjectService.getById(sysField.getSRefKey());
                    if (StrUtil.hasEmpty(sysField.getSEnumOption())) {
                        if (sysField.getSDataType().equals("MultiList")) {
                            String[] arrRefId = sRefId.replaceAll("(^;|;$)", "").split(";");
                            String sRefIdLinked = "'" + String.join("','", arrRefId) + "'";

                            List<Map<String, Object>> listMapResult = jdbcTemplate.queryForList("SELECT `" + sysField.getSRefIdFieldAs() + "` AS ID,`" + sysField.getSRefNameFieldAs() + "` AS sName" +
                                    " FROM " + fieldSysObject.getSDbTable() +
                                    " WHERE " + fieldSysObject.getSIdFieldAs() +
                                    " IN (" + sRefIdLinked + ")");
                            objectData.put(sysField.getSFieldAs(), listMapResult);
                        } else {
                            Map<String, Object> mapResult = jdbcTemplate.queryForMap("SELECT `" + sysField.getSRefIdFieldAs() + "` AS ID,`" + sysField.getSRefNameFieldAs() + "` AS sName" +
                                    " FROM " + fieldSysObject.getSDbTable() +
                                    " WHERE " + fieldSysObject.getSIdFieldAs() +
                                    " = '" + sRefId + "'");
                            objectData.put(sysField.getSFieldAs(), mapResult);
                        }
                    } else {
                        if (sysField.getSDataType().equals("MultiList")) {
                            String[] arrRefId = sRefId.replaceAll("(^;|;$)", "").split(";");
                            List<Map<String, String>> listMapRef = new ArrayList<>();
                            for (int i = 0; i < arrRefId.length; i++) {
                                Map<String, String> mapRef = new HashMap<>();
                                mapRef.put("ID", arrRefId[i]);
                                mapRef.put("sName", sysField.getOptionValue(arrRefId[i]));

                                listMapRef.add(mapRef);
                            }

                            objectData.put(sysField.getSFieldAs(), listMapRef);
                        } else {
                            Map<String, String> mapRef = new HashMap<>();
                            mapRef.put("ID", sRefId);
                            mapRef.put("sName", sysField.getOptionValue(sRefId));

                            objectData.put(sysField.getSFieldAs(), mapRef);
                        }
                    }

                    break;
                case "Common":
                    String sId = String.valueOf(objectData.get(sysField.getSFieldAs()));
                    if (objectData.get(sysField.getSFieldAs()) == null || StrUtil.hasEmpty(sId)) {
                        break;
                    }

                    SysField refSysField = sysFieldService.getById(sysField.getRefSysFieldId());
                    if (refSysField != null) {
                        if (StrUtil.hasEmpty(refSysField.getSEnumOption())) {
                            fieldSysObject = sysObjectService.getById(sysField.getSRefKey());
                            Map<String, Object> mapResult = jdbcTemplate.queryForMap("SELECT `" + refSysField.getSRefIdFieldAs() + "` AS ID,`" + refSysField.getSRefNameFieldAs() + "` AS sName" +
                                    " FROM " + fieldSysObject.getSDbTable() +
                                    " WHERE " + fieldSysObject.getSIdFieldAs() +
                                    " = '" + sId + "'");
                            objectData.put(sysField.getSFieldAs(), mapResult);
                        } else {
                            Map<String, String> mapRef = new HashMap<>();
                            mapRef.put("ID", sId);
                            mapRef.put("sName", refSysField.getOptionValue(sId));
                            objectData.put(sysField.getSFieldAs(), mapRef);
                        }
                    }

                    break;
            }
        }

        return objectData;
    }

    /**
     * 获取一个对象的数据，并且是已经格式化后的
     *
     * @param sObjectName 对象名
     * @param ObjectId    详情ID
     * @return Map<String, Object>
     */
    public Map<String, Object> getOneObjectData(String sObjectName, String ObjectId) {
        SysObject sysObject = sysObjectService.getById(sObjectName);

        //获取原始数据
        QueryWrapper<T> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(sysObject.getSIdFieldAs(), ObjectId);
        Map<String, Object> objectData = getMap(queryWrapper);
        objectData.put("ID", objectData.get(sysObject.getSIdFieldAs()));

        List<SysField> listSysField = sysFieldService.getByObjectName(sObjectName);

        return formatUiObjectData(listSysField, objectData);
    }

    /**
     * 获取当前操作用户，可以使用的对象界面
     *
     * @param sObjectName 对象名
     * @param sType       界面类型
     * @return SysUI
     */
    public SysUI getSysUI(String sObjectName, String sType) {

        SysUser currUser = sysSessionService.getCurrUser();

        QueryWrapper<SysUI> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.like("TypeId", sType);
        List<SysUI> listSysUI = sysUIService.list(queryWrapper);

        for (SysUI sysUI : listSysUI) {
            if (!StrUtil.hasEmpty(sysUI.getSPermissionJson())) {
                JSONObject jsonObject = JSONObject.parseObject(sysUI.getSPermissionJson());

                JSONArray arrUserJson = jsonObject.getJSONArray("sysusers");
                if (arrUserJson != null && arrUserJson.size() > 0) {
                    if (arrUserJson.contains(currUser.getLID())) {
                        return sysUI;
                    }
                }

                JSONArray arrRoleJson = jsonObject.getJSONArray("sysroles");
                if (arrRoleJson != null && arrRoleJson.size() > 0) {
                    if (arrRoleJson.contains(currUser.getSysRoleId())) {
                        return sysUI;
                    }
                }

                JSONArray arrDepJson = jsonObject.getJSONArray("sysdeps");
                if (arrDepJson != null && arrDepJson.size() > 0) {
                    if (arrDepJson.contains(currUser.getSysDepId())) {
                        return sysUI;
                    }
                }
            }
        }

        //如果没有匹配到界面，那么久找一个没有权限配置的，直接返回
        for (SysUI sysUI : listSysUI) {
            if (StrUtil.hasEmpty(sysUI.getSPermissionJson())) {
                return sysUI;
            } else if (sysUI.getSPermissionJson().equals("{}")) {
                return sysUI;
            } else {
                JSONObject jsonObject = JSONObject.parseObject(sysUI.getSPermissionJson());
                JSONArray arrUserJson = jsonObject.getJSONArray("sysusers");
                JSONArray arrDepJson = jsonObject.getJSONArray("sysdeps");
                JSONArray arrRoleJson = jsonObject.getJSONArray("sysroles");
                if (arrUserJson.size() == 0 && arrDepJson.size() == 0 && arrRoleJson.size() == 0) {
                    return sysUI;
                }
            }
        }

        throw new SystemException("没有匹配到合适的界面");
    }

    public SysUser getCurrUser() {
        return sysSessionService.getCurrUser();
    }

    /**
     * 校验当前的用户是否有权限使用该视图
     *
     * @param sysList
     * @return
     */
    private Boolean hasListPermit(SysList sysList) {
        SysUser currUser = getCurrUser();

        if (!StrUtil.hasEmpty(sysList.getSPermissionJson())) {//有设置权限
            JSONObject jsonObject = JSONObject.parseObject(sysList.getSPermissionJson());

            JSONArray arrUserJson = jsonObject.getJSONArray("sysusers");
            if (arrUserJson != null && arrUserJson.size() > 0) {
                if (arrUserJson.contains(currUser.getLID())) {
                    return true;
                }
            }

            JSONArray arrRoleJson = jsonObject.getJSONArray("sysroles");
            if (arrRoleJson != null && arrRoleJson.size() > 0) {
                if (arrRoleJson.contains(currUser.getSysRoleId())) {
                    return true;
                }
            }

            JSONArray arrDepJson = jsonObject.getJSONArray("sysdeps");
            if (arrDepJson != null && arrDepJson.size() > 0) {
                if (arrDepJson.contains(currUser.getSysDepId())) {
                    return true;
                }
            }

            if (arrUserJson != null && arrUserJson.size() == 0 && arrRoleJson != null && arrRoleJson.size() == 0 && arrDepJson != null && arrDepJson.size() == 0) {
                return true;
            }

            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取当前操作用户，可以使用的对象视图
     *
     * @param sObjectName 对象名
     * @param sType       类型
     * @return SysUI
     */
    public SysList getSysList(String sObjectName, String sType) {

        QueryWrapper<SysList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.eq("TypeId", sType);
        List<SysList> listSysList = sysListService.list(queryWrapper);

        //按权限来查找匹配的视图
        for (SysList sysList : listSysList) {
            if (hasListPermit(sysList)) {
                return sysList;
            }
        }

        //权限没匹配的，返回缺省的
        for (SysList sysList : listSysList) {
            if (sysList.getBDefault()) {
                return sysList;
            }
        }

        //没有缺省的，返回第一个
        return listSysList.get(0);
    }


    /**
     * 获取UI的信息块配置
     *
     * @param sObjectName 对象名
     * @param sType       类别
     * @return
     */
    public Map<String, Object> getUIConfig(String sObjectName, String sType) {
        SysUI sysUI = getSysUI(sObjectName, sType);

        Map<String, Object> mapUI = new HashMap<>();
        mapUI.put("masterobjectui", sysUIService.getConfig(sysUI));
        mapUI.put("subobjectui", null);

        //查找子对象
        QueryWrapper<SysObject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ParentId", sObjectName);
        List<SysObject> listSubSysObject = sysObjectService.list(queryWrapper);

        List<Object> listSubUIConfig = new ArrayList<>();
        for (SysObject subSysObject : listSubSysObject) {
            sysUI = getSysUI(subSysObject.getSObjectName(), sType);
            mapUI.put("subobjectui", sysUIService.getConfig(sysUI));
        }

        return mapUI;
    }

    /**
     * 获取对象的配置信息
     *
     * @param sObjectName
     * @return
     */
    public SysObject getObjectConfig(String sObjectName) {
        return sysObjectService.detail(sObjectName);
    }

    /**
     * 获取子对象
     *
     * @param sObjectName
     * @return
     */
    public SysObject getSubObject(String sObjectName) {
        QueryWrapper<SysObject> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ParentId", sObjectName);
        return sysObjectService.getOne(queryWrapper);
    }

    /**
     * 获取对象的视图配置信息
     *
     * @param mapParam 参数
     * @return
     */
    public Map<String, Object> getListConfig(Map<String, String> mapParam) {

        int ListId = Integer.parseInt(mapParam.get("ListId"));
        String sObjectName = mapParam.get("sObjectName");
        String sType = mapParam.get("sType");

        SysList sysList = null;
        if (ListId > 0) {
            sysList = sysListService.getById(ListId);
        } else {
            sysList = getSysList(sObjectName, sType);
        }

        Map<String, Object> result = null;
        if (ObjectUtil.isNotEmpty(sysList)) {
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
            result.put("lOperaColumnWidth", sysList.getLOperaColumnWidth());
            result.put("lHeight", sysList.getLHeight());
            result.put("bShowOpera", sysList.getBShowOpera());

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

            List<SysField> selectId = new ArrayList<>();
            List<SysField> advancedId = new ArrayList<>();
            List<SysField> fastSearchId = new ArrayList<>();
            String sAdvancedSearchId = sysList.getSAdvancedSearchId();
            String sFastSearchId = sysList.getSFastSearchId();

            if (StrUtil.isNotEmpty(sListSelectId)) {
                QueryWrapper<SysField> sysListQueryWrapper = new QueryWrapper<>();
                if (sListSelectId.indexOf(",") != -1) {
                    sysListQueryWrapper.in("lID", sListSelectId.split(","));
                } else {
                    sysListQueryWrapper.eq("lID", sListSelectId);
                }

                sysListQueryWrapper.orderByAsc("field(lID, " + sListSelectId + ")");

                selectId = sysFieldService.list(sysListQueryWrapper);

                for (SysField sysField : selectId) {
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
                }
            }

            if (StrUtil.isNotEmpty(sAdvancedSearchId)) {
                QueryWrapper<SysField> sysListQueryWrapper = new QueryWrapper<>();
                if (sAdvancedSearchId.contains(",")) {
                    sysListQueryWrapper.in("lID", sAdvancedSearchId.split(","));
                } else {
                    sysListQueryWrapper.eq("lID", sAdvancedSearchId);
                }

                advancedId = sysFieldService.list(sysListQueryWrapper);

                for (SysField sysField : advancedId) {
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
                }
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

            result.put("arrListSelectId", selectId);
            result.put("arrAdvancedSearchId", advancedId);
            result.put("arrFastSearchId", fastSearchId);
        } else {
            throw new SystemException(10001L, "当前对象没有适合您的视图");
        }

        return result;
    }

    /**
     * 获取对象主页的分类tab
     *
     * @param sObjectName 对象名
     * @return List<Map < String, Object>>
     */
    public List<Map<String, Object>> getTabList(String sObjectName) {
        QueryWrapper<SysList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.eq("TypeId", "list");
        queryWrapper.eq("bActive", "1");
        List<SysList> listSysList = sysListService.list(queryWrapper);

        List<Map<String, Object>> listMapTab = new ArrayList<>();
        for (SysList sysList : listSysList) {
            if (hasListPermit(sysList)) {
                Map<String, Object> mapTab = new HashMap<>();
                mapTab.put("ID", sysList.getLID());
                mapTab.put("sName", sysList.getSName());
                mapTab.put("ListId", sysList.getLID());

                listMapTab.add(mapTab);
            }
        }

        return listMapTab;
    }


    public SysList getReferList(String sObjectName) {
        QueryWrapper<SysList> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sObjectName", sObjectName);
        queryWrapper.eq("TypeId", "refer");
        return sysListService.getOne(queryWrapper);
    }

    /**
     * 格式化视图列表的数据，该方法可以被子对象继承修改
     *
     * @param listObjectData
     * @return
     */
    protected JSONArray formatListData(List<Map<String, Object>> listObjectData) {
        JSONArray arrJsonObjectData = new JSONArray();
        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);

            JSONObject jsonObjectData = new JSONObject();
            jsonObjectData.put("btns", getListInlineBtn(objectData));
            jsonObjectData.put("data", objectData);
            arrJsonObjectData.add(jsonObjectData);
        }

        return arrJsonObjectData;
    }

    /**
     * 获取视图列表，每行的操作按钮
     *
     * @param objectData
     * @return
     */
    protected List<JSONObject> getListInlineBtn(Map<String, Object> objectData) {
        List<JSONObject> listBtn = new ArrayList<>();

        //添加查看的按钮
        JSONObject jsonBtn = new JSONObject();
        jsonBtn.put("ID", "view");
        jsonBtn.put("sName", "查看");
        jsonBtn.put("handler", "handleView");
        listBtn.add(jsonBtn);

        //添加编辑的按钮
        jsonBtn = new JSONObject();
        jsonBtn.put("ID", "edit");
        jsonBtn.put("sName", "编辑");
        jsonBtn.put("handler", "handleEdit");
        listBtn.add(jsonBtn);

        return listBtn;
    }

    /**
     * 格式化新建/编辑界面提交上来的数据
     *
     * @return
     */
    protected JSONObject formatFormData(JSONObject jsonObjectData, SysUI sysUI, String sUIType, String ObjectId) {
        List<SysField> listSysField = sysUIService.getSysFieldInConfig(sysUI);

        //首先，判断必填
        List<Map<String, Object>> listMapFieldNull = new ArrayList<>();
        for (SysField sysField : listSysField) {
            if (BooleanUtil.isTrue(sysField.getBRequired())) {
                if (StrUtil.hasEmpty(jsonObjectData.getString(sysField.getSFieldAs()))) {
                    Map<String, Object> mapFieldNull = new HashMap<>();
                    mapFieldNull.put("lID", sysField.getLID());
                    mapFieldNull.put("sFieldAs", sysField.getSFieldAs());
                    mapFieldNull.put("sName", sysField.getSName());

                    listMapFieldNull.add(mapFieldNull);
                }
            }
        }

        if (listMapFieldNull.size() > 0) {
            throw new SystemException(11001L, "必填项校验失败", listMapFieldNull);
        }
        System.out.println(jsonObjectData.getJSONArray("ListTest"));


        //其次，校验数据类型是否符合规则
        List<Map<String, Object>> listMapFieldException = new ArrayList<>();
        for (SysField sysField : listSysField) {
            String sFieldData = jsonObjectData.getString(sysField.getSFieldAs());

            if (StrUtil.hasEmpty(sFieldData)) {
                continue;
            }

            try {
                switch (sysField.getSDataType()) {
                    case "Int":
                        try {
                            jsonObjectData.put(sysField.getSFieldAs(), Integer.valueOf(sFieldData));
                        } catch (NumberFormatException e) {
                            throw new Exception("请输入整数");
                        }
                        break;
                    case "Float":
                        try {
                            jsonObjectData.put(sysField.getSFieldAs(), BigDecimal.valueOf(Double.parseDouble(sFieldData)));
                        } catch (NumberFormatException e) {
                            throw new Exception("请正确输入的数字");
                        }
                        break;
                    case "Date":
                        try {
                            Date date = DateUtil.parse(sFieldData, "yyyy-MM-dd");
                        } catch (NumberFormatException e) {
                            throw new Exception("请正确的日期");
                        }

                        break;
                    case "DateTime":
                        try {
                            DateTime dateTime = new DateTime(sFieldData, DatePattern.NORM_DATETIME_FORMAT);
                        } catch (NumberFormatException e) {
                            throw new Exception("请正确的日期时间");
                        }

                        break;
                    case "MultiList":
                        JSONArray jsonArray = jsonObjectData.getJSONArray(sysField.getSFieldAs());
                        if (jsonArray == null) {
                            jsonObjectData.put(sysField.getSFieldAs(), null);
                        } else {
                            StringBuilder sValue = new StringBuilder();
                            sValue.append(";");
                            for (int i = 0; i < jsonArray.size(); i++) {
                                sValue.append(jsonArray.getString(i)).append(";");
                            }

                            jsonObjectData.put(sysField.getSFieldAs(), sValue.toString());
                        }

                        break;
                    case "ListTable":
                        jsonObjectData.put(sysField.getSFieldAs(), sFieldData);
                        break;
                    case "AttachFile":
                        jsonArray = jsonObjectData.getJSONArray(sysField.getSFieldAs());
                        if (jsonArray == null) {
                            jsonObjectData.put(sysField.getSFieldAs(), null);
                        } else {
                            StringBuilder sValue = new StringBuilder();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                sValue.append(jsonArray.getString(i));

                                if (i < jsonArray.size() - 1) {
                                    sValue.append(",");
                                }
                            }

                            jsonObjectData.put(sysField.getSFieldAs(), sValue.toString());
                        }

                        break;
                }

                //预留出接口给继承类验证字段
                fieldValidate(sysField, sFieldData, ObjectId);

            } catch (Exception e) {
                Map<String, Object> mapFieldException = new HashMap<>();
                mapFieldException.put("lID", sysField.getLID());
                mapFieldException.put("sFieldAs", sysField.getSFieldAs());
                mapFieldException.put("sName", sysField.getSName());
                mapFieldException.put("sMessage", e.getMessage());

                listMapFieldException.add(mapFieldException);
            }
        }

        if (listMapFieldException.size() > 0) {
            throw new SystemException(11002L, "数据项校验失败", listMapFieldException);
        }

        //查询出对象的保留属性，进行赋值
        if (sUIType.equals("new")) {
            QueryWrapper<SysField> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sObjectName", sysUI.getSObjectName());
            queryWrapper.inSql("sFieldAs", "'dNewTime', 'dEditTime', 'OwnerId', 'NewUserId', 'EditUserId'");
            List<SysField> listKeepField = sysFieldService.list(queryWrapper);
            for (SysField field : listKeepField) {
                if (jsonObjectData.containsKey(field.getSFieldAs()) && jsonObjectData.get(field.getSFieldAs()) != null) {//如果有设置值，就跳过
                    continue;
                }

                if (field.getSDataType().equals("DateTime")) {
                    jsonObjectData.put(field.getSFieldAs(), DateTime.now().toString());
                } else {
                    jsonObjectData.put(field.getSFieldAs(), sysSessionService.getCurrUser().getLID());
                }
            }
        } else if (sUIType.equals("edit")) {
            QueryWrapper<SysField> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sObjectName", sysUI.getSObjectName());
            queryWrapper.inSql("sFieldAs", "'dEditTime', 'EditUserId'");
            List<SysField> listKeepField = sysFieldService.list(queryWrapper);
            for (SysField field : listKeepField) {
                if (field.getSDataType().equals("DateTime")) {
                    jsonObjectData.put(field.getSFieldAs(), DateTime.now().toString());
                } else {
                    jsonObjectData.put(field.getSFieldAs(), sysSessionService.getCurrUser().getLID());
                }
            }
        }

        return jsonObjectData;
    }

    /**
     * 新建保存
     *
     * @param sObjectName 对象名
     * @param sObjectData 对象表单数据
     */
    @Transactional
    public Object newSave(String sObjectName, String sObjectData) {
        //对象数据
        JSONObject jsonObjectData = JSONObject.parseObject(sObjectData);

        //对象
        SysObject sysObject = sysObjectService.getById(sObjectName);

        //提取对象新建界面里的所有属性
        SysUI sysUI = getSysUI(sObjectName, "new");

        //校验，格式化数据
        formatFormData(jsonObjectData, sysUI, "new", null);

        //保存前，留接口给继承类进行数据处理
        beforeObjectNewSave(sysObject, jsonObjectData);

        //转换成实体类
        T objectEntity = jsonObjectData.toJavaObject(currentModelClass());

        //保存数据
        save(objectEntity);

        //保存后，留接口给继承类继续进行处理
        afterObjectNewSave(sysObject, objectEntity);

        //获取对象的主键属性，为的是拿到ID
        SysField pkSysField = sysFieldService.getPkField(sysObject.getSObjectName());

        //转换成Map，才能方便拿到ID
        HashMap mapObjectEntity = JSONObject.parseObject(JSONObject.toJSONString(objectEntity), HashMap.class);

        return mapObjectEntity.get(pkSysField.getSFieldAs());
    }

    /**
     * 编辑保存
     *
     * @param ObjectId    对象的ID
     * @param sObjectName 对象名
     * @param sObjectData 对象表单数据
     */
    @Transactional
    public void editSave(String ObjectId, String sObjectName, String sObjectData) {
        //对象数据
        JSONObject jsonObjectData = JSONObject.parseObject(sObjectData);

        //对象
        SysObject sysObject = sysObjectService.getById(sObjectName);

        //提取对象新建界面里的所有属性
        SysUI sysUI = getSysUI(sObjectName, "edit");

        //获取对象的主键属性，为的是赋值ID字段
        SysField pkSysField = sysFieldService.getPkField(sysObject.getSObjectName());
        jsonObjectData.put(pkSysField.getSFieldAs(), ObjectId);

        //校验，格式化数据
        formatFormData(jsonObjectData, sysUI, "edit", ObjectId);

        //保存前，留接口给继承类进行数据处理
        beforeObjectEditSave(sysObject, jsonObjectData);

        //转换成实体类
        T objectEntity = jsonObjectData.toJavaObject(currentModelClass());

        //保存数据
        saveOrUpdate(objectEntity);

        //保存后，留接口给继承类继续进行处理
        afterObjectEditSave(sysObject, objectEntity);
    }

    /**
     * 编辑审批保存
     *
     * @param ObjectId       数据ID
     * @param sObjectName    对象名
     * @param sObjectData    表单数据
     * @param sApproveAction 审批类型
     */
    public void editSave(String ObjectId, String sObjectName, String sObjectData, String sApproveAction) {
        editSave(ObjectId, sObjectName, sObjectData);

        SysObject sysObject = sysObjectService.getById(sObjectName);
        if (sysObject.getBWorkFlow()) {
            if (sApproveAction.equals("draft")) {
                sysApproveService.draft(sObjectName, ObjectId);
            } else if (sApproveAction.equals("submit")) {
                SysApprove sysApprove = sysApproveService.submit(sObjectName, ObjectId);
                if (sysApprove.getStatusId().equals("passed") || sysApprove.getStatusId().equals("submitted")) {
                    afterApprovePass(sysApprove);//回调
                }
            }
        }
    }

    /**
     * 编辑对象前的处理
     *
     * @param sysObject      对象
     * @param jsonObjectData 对象数据
     */
    protected void beforeObjectEditSave(SysObject sysObject, JSONObject jsonObjectData) {

    }

    /**
     * 编辑对象保存后的处理
     *
     * @param sysObject    对象
     * @param objectEntity 对象实体
     */
    protected void afterObjectEditSave(SysObject sysObject, T objectEntity) {

    }

    /**
     * 新建对象前的处理
     *
     * @param sysObject      对象
     * @param jsonObjectData 对象数据
     */
    protected void beforeObjectNewSave(SysObject sysObject, JSONObject jsonObjectData) {

    }

    /**
     * 新建对象后的处理
     *
     * @param sysObject 对象
     * @param entity    对象实体
     */
    protected void afterObjectNewSave(SysObject sysObject, T entity) {

    }

    /**
     * 字段校验
     *
     * @param sysField
     * @param sFieldValue
     */
    protected void fieldValidate(SysField sysField, String sFieldValue, String ObjectId) throws Exception {

    }

    /**
     * 上传文件
     *
     * @param file
     * @param sObjectName
     */
    @Transactional
    public Map<String, Object> upload(MultipartFile file, String sObjectName) {
        //文件名和存储路径的处理
        String sFileNameExt = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        String sFileName = randomNumbers(32) + "." + sFileNameExt.toLowerCase(Locale.ROOT);
        String sDirPath = DateUtil.format(DateUtil.date(), "yyyy/MM/dd");
        String sFullDirPath = businessconfig.getUploadsavepath() + "/" + sDirPath;

        if ("php|phtml|php3|php4|jsp|exe|dll|asp|cer|asa|shtml|shtm|html|htm|aspx|asax|cgi|fcgi|pl|bat".contains(sFileNameExt)) {
            throw new SystemException(10001L, "您上传的文件非法，请重新选择。");
        }

        //保存到数据库
        SysAttach sysAttach = sysAttachService.newSave(sObjectName, sDirPath + "/" + sFileName, file.getOriginalFilename());

        //创建文件夹
        File saveDir = new File(sFullDirPath);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            LOGGER.error(sFullDirPath + "创建失败");
            throw new SystemException(10001L, "文件上传失败");
        }

        //保存文件
        File dest = new File(sFullDirPath + "/" + sFileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            throw new SystemException(10001L, "文件上传失败");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("attachid", sysAttach.getLID());
        resultMap.put("url", businessconfig.getUploadurl() + "/" + sysAttach.getSFilePath());

        return resultMap;
    }

    /**
     * 导出视图的数据
     *
     * @param listParam 视图的参数
     * @return void
     */
    public String export(ListParam listParam) {
        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        SysList sysList = sysListService.getById(listParam.getListid());
        String sListSelectId = sysList.getSListSelectId();

        if (StrUtil.isNotEmpty(sListSelectId)) {
            QueryWrapper<SysField> sysListQueryWrapper = new QueryWrapper<>();
            if (sListSelectId.indexOf(",") != -1) {
                sysListQueryWrapper.in("lID", (Object) sListSelectId.split(","));
            } else {
                sysListQueryWrapper.eq("lID", sListSelectId);
            }

            List<SysField> listSysField = sysFieldService.list(sysListQueryWrapper);

            for (SysField sysField : listSysField) {
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
            }

            List<List<Object>> listExcelData = new ArrayList<>();
            ArrayList<Map<String, Object>> listRowData = new ArrayList<>();
            for (Map<String, Object> mapObjectData : listMapObjectData) {
                Map<String, Object> mapRowData = new LinkedHashMap<>();
                for (SysField sysField : listSysField) {
                    Object oValue = mapObjectData.get(sysField.getSFieldAs());
                    if (oValue == null || oValue.toString().equals("")) {
                        continue;
                    }

                    switch (sysField.getSDataType()) {
                        case "ListTable":
                        case "List":
                        case "Common":
                            mapRowData.put(sysField.getSName(), ((Map<String, Object>) oValue).get("sName"));
                            break;
                        case "MultiList":
                            List<Map<String, Object>> listMapValue = (List<Map<String, Object>>) oValue;

                            StringBuilder sValue = new StringBuilder();
                            String sComm = "";
                            for (Map<String, Object> mapValue : listMapValue) {
                                sValue.append(sComm + mapValue.get("sName"));
                                sComm = ";";
                            }
                            mapRowData.put(sysField.getSName(), sValue);
                            break;
                        case "Bool":
                            if (oValue.toString().equals("1")) {
                                mapRowData.put(sysField.getSName(), "是");
                            } else {
                                mapRowData.put(sysField.getSName(), "否");
                            }
                            break;
                        default:
                            mapRowData.put(sysField.getSName(), oValue);
                            break;
                    }
                }

                listRowData.add(mapRowData);
            }

            String sSaveDirPath = businessconfig.getUploadsavepath() + "/temp";
            File saveDir = new File(sSaveDirPath);
            if (!saveDir.exists() && !saveDir.mkdirs()) {
                LOGGER.error(sSaveDirPath + "创建失败");
                throw new SystemException(10001L, "导出失败");
            }

            String sFileName = randomNumbers(32) + ".xlsx";
            String sFullSavePath = sSaveDirPath + "/" + sFileName;

            // 通过工具类创建writer，默认创建xls格式
            ExcelWriter writer = ExcelUtil.getWriter(sFullSavePath);

            //一次性写出内容，强制输出标题
            writer.write(listRowData, true);

            //关闭writer，释放内存
            writer.close();

            return sFileName;
        }

        return null;
    }

    /**
     * 删除不存在的数据
     *
     * @param ParentId
     * @param listSubmitId
     */
    @Transactional
    public void delRemoveData(String sObjectName, String ParentId, List<Integer> listSubmitId) {
        SysObject sysObject = sysObjectService.getById(sObjectName);

        listSubmitId.add(0);
        QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
        queryWrapper.select(sysObject.getSIdFieldAs());
        queryWrapper.eq(sysObject.getSParentIdFieldAs(), ParentId);
        queryWrapper.notIn(sysObject.getSIdFieldAs(), listSubmitId);
        List<Map<String, Object>> listRemoveData = listMaps(queryWrapper);

        for (Map<String, Object> data : listRemoveData) {
            removeById((Integer) data.get(sysObject.getSIdFieldAs()));
        }
    }

    /**
     * 检查操作项的权限
     */
    public JSONArray getOperationPermit(SysObject sysObject) {

        //当前用户
        SysUser sysCurrUser = sysSessionService.getCurrUser();

        SysRole sysRole = sysRoleService.getById(sysCurrUser.getSysRoleId());

        //把有权限的项，都保存到这个变量里
        JSONArray arrOperationPermit = new JSONArray();

        QueryWrapper<SysOperatorPermission> queryWrapper = null;
        JSONArray arrOpera = JSONArray.parseArray(sysObject.getSOperatorJson());
        for (int i = 0; i < arrOpera.size(); i++) {
            JSONObject jsonOpera = arrOpera.getJSONObject(i);

            if (sysRole.getUpId() == null) {//超级管理员，不用权限判断
                arrOperationPermit.add(jsonOpera.getString("ID"));
            }

            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sObjectName", sysObject.getSObjectName());
            queryWrapper.eq("sOperator", jsonOpera.getString("ID"));
            queryWrapper.eq("TypeId", "SysUser");
            queryWrapper.eq("ObjectId", sysCurrUser.getLID());
            if (sysOperatorPermissionService.count(queryWrapper) == 0) {//如果不存在用户级别的权限，继续查找角色级别
                queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("sObjectName", sysObject.getSObjectName());
                queryWrapper.eq("sOperator", jsonOpera.getString("ID"));
                queryWrapper.eq("TypeId", "SysRole");
                queryWrapper.eq("ObjectId", sysCurrUser.getSysRoleId());
                if (sysOperatorPermissionService.count(queryWrapper) > 0) {
                    arrOperationPermit.add(jsonOpera.getString("ID"));
                }
            } else {
                arrOperationPermit.add(jsonOpera.getString("ID"));
            }
        }

        return arrOperationPermit;
    }

    /**
     * 视图按钮
     *
     * @return JSONArray
     */
    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

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
        btn.put("ID", "export");
        btn.put("sName", "导出");
        btn.put("handler", "handleExport");
        btn.put("icon", "el-icon-download");
        arrBtn.add(btn);

        btn.put("ID", "alloc");
        btn.put("sName", "分配");
        btn.put("handler", "handleAlloc");
        btn.put("icon", "el-icon-s-check");

        btn = new JSONObject();
        btn.put("ID", "refresh");
        btn.put("sName", "刷新");
        btn.put("handler", "handleRefresh");
        btn.put("icon", "el-icon-refresh");
        arrBtn.add(btn);

        return arrBtn;
    }


    /**
     * 获取编辑界面的按钮
     *
     * @param sObjectName
     * @param objectId
     */
    public JSONArray getEditUiBtn(String sObjectName, String objectId) {
        SysObject sysObject = sysObjectService.getById(sObjectName);

        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();
        btn.put("ID", "cancel");
        btn.put("sName", "取消");
        btn.put("handler", "handleCancel");
        btn.put("icon", "el-icon-erm-quxiao iconfont");
        arrBtn.add(btn);

        if (sysObject.getBWorkFlow()) {
            btn = new JSONObject();
            btn.put("ID", "editdraftsave");
            btn.put("sName", "保存草稿");
            btn.put("handler", "handleDraftSave");
            btn.put("icon", "el-icon-erm-cunrucaogaoxiang iconfont");
            arrBtn.add(btn);

            btn = new JSONObject();
            btn.put("ID", "editreqsave");
            btn.put("sName", "提交审批");
            btn.put("handler", "handleReqSave");
            btn.put("icon", "el-icon-erm-tianshenpi iconfont");
            arrBtn.add(btn);
        } else {
            btn = new JSONObject();
            btn.put("ID", "editsave");
            btn.put("sName", "保存");
            btn.put("handler", "handleEditSave");
            btn.put("icon", "el-icon-erm-baocun iconfont");
            arrBtn.add(btn);
        }

        return arrBtn;
    }

    /**
     * 获取详情界面的按钮
     *
     * @param sObjectName
     * @param objectId
     * @return
     */
    public JSONArray getViewUiBtn(String sObjectName, String objectId) {
        JSONArray arrBtn = new JSONArray();

        SysObject sysObject = sysObjectService.getById(sObjectName);

        if (sysObject.getBWorkFlow()) {
            SysApprove sysApprove = sysApproveService.getApprove(sObjectName, objectId);

            if (sysApproveService.canDeprecate(sysApprove)) {
                JSONObject btn = new JSONObject();
                btn.put("ID", "deprecate");
                btn.put("sName", "废弃");
                btn.put("handler", "handleDeprecate");
                btn.put("icon", "el-icon-erm-huishouzhan");
                arrBtn.add(btn);
            }

            if (ObjectUtil.isEmpty(sysApprove) || sysApproveService.canEdit(sysApprove)) {
                if (hasDataPermit(sObjectName, objectId, "edit")) {
                    JSONObject btn = new JSONObject();
                    btn.put("ID", "edit");
                    btn.put("sName", "编辑");
                    btn.put("handler", "handleEdit");
                    btn.put("icon", "el-icon-edit");
                    arrBtn.add(btn);
                }
            }

            if (sysApproveService.canApprove(sysApprove)) {
                JSONObject btn = new JSONObject();
                btn.put("ID", "reject");
                btn.put("sName", "驳回");
                btn.put("handler", "handleReject");
                btn.put("icon", "el-icon-erm-shenpibohui");
                arrBtn.add(btn);

                btn = new JSONObject();
                btn.put("ID", "pass");
                btn.put("sName", "审批通过");
                btn.put("handler", "handlePass");
                btn.put("icon", "el-icon-erm-shenpitongguo");
                arrBtn.add(btn);
            }
        } else {
            if (hasDataPermit(sObjectName, objectId, "edit")) {
                JSONObject btn = new JSONObject();
                btn.put("ID", "edit");
                btn.put("sName", "编辑");
                btn.put("handler", "handleEdit");
                btn.put("icon", "el-icon-edit");
                arrBtn.add(btn);
            }
        }

        JSONObject btn = new JSONObject();
        btn.put("ID", "refresh");
        btn.put("sName", "刷新");
        btn.put("handler", "handleRefresh");
        btn.put("icon", "el-icon-refresh");
        arrBtn.add(btn);

        return arrBtn;
    }

    /**
     * 审批通过
     *
     * @param sObjectName 对象名
     * @param objectId    对象数据的ID
     * @return
     */
    public void approvePass(String sObjectName, String objectId) {
        SysApprove sysApprove = sysApproveService.getApprove(sObjectName, objectId);

        if (ObjectUtil.isEmpty(sysApprove)) {
            throw new SystemException("审批流不存在");
        }

        if (!sysApproveService.canApprove(sysApprove)) {
            throw new SystemException("您没有权限审批");
        }

        sysApproveService.pass(sysApprove);

        if (sysApprove.getStatusId().equals("passed")) {
            afterApprovePass(sysApprove);
        }
    }

    /**
     * 审批废弃
     *
     * @param sObjectName 对象名
     * @param objectId    对象数据的ID
     * @return
     */
    public void approveDeprecate(String sObjectName, String objectId) {
        SysApprove sysApprove = sysApproveService.getApprove(sObjectName, objectId);

        if (ObjectUtil.isEmpty(sysApprove)) {
            throw new SystemException("审批流不存在");
        }

        if (!sysApproveService.canDeprecate(sysApprove)) {
            throw new SystemException("该对象不能被废弃");
        }

        beforeApproveDeprecate(sysApprove);

        sysApproveService.deprecate(sysApprove);

        afterApproveDeprecate(sysApprove);
    }

    /**
     * 审批拒绝
     *
     * @param sObjectName 对象名
     * @param objectId    对象数据的ID
     */
    public void approveReject(String sObjectName, String objectId) {
        SysApprove sysApprove = sysApproveService.getApprove(sObjectName, objectId);

        if (ObjectUtil.isEmpty(sysApprove)) {
            throw new SystemException("审批流不存在");
        }

        if (!sysApproveService.canApprove(sysApprove)) {
            throw new SystemException("您没有权限审批");
        }

        beforeApproveReject(sysApprove);

        sysApproveService.reject(sysApprove);

        afterApproveReject(sysApprove);
    }

    /**
     * 审批流通过之后的回调
     *
     * @param sysApprove 审批对象
     */
    protected void afterApprovePass(SysApprove sysApprove) {

    }

    /**
     * 审批废弃前置处理器
     *
     * @param sysApprove 审批对象
     */
    protected void beforeApproveDeprecate(SysApprove sysApprove) {

    }

    /**
     * 审批废弃后置处理器
     *
     * @param sysApprove 审批对象
     */
    protected void afterApproveDeprecate(SysApprove sysApprove) {

    }

    /**
     * 审批拒绝前置处理器
     *
     * @param sysApprove 审批对象
     */
    protected void beforeApproveReject(SysApprove sysApprove) {

    }

    /**
     * 审批拒绝后置处理器
     *
     * @param sysApprove 审批对象
     */
    protected void afterApproveReject(SysApprove sysApprove) {

    }


    protected void beforeDel(SysObject sysObject, String objectId) {

    }

    protected void afterDel(SysObject sysObject, String objectId) {

    }

    protected void afterDel(SysObject sysObject) {

    }

    /**
     * 是否有数据权限
     *
     * @param sObjectName
     * @param objectId
     * @param sAction
     * @return
     */
    public Boolean hasDataPermit(String sObjectName, String objectId, String sAction) {

        //如果不存在OwnerID，就不要判断数据权限
        if (!sysFieldService.hasOwnerId(sObjectName)) {
            return true;
        }

        //如果存在OwnerID，就要判断数据权限
        SysUser sysCurrUser = sysSessionService.getCurrUser();
        SysRole currUserRole = sysRoleService.getById(sysCurrUser.getSysRoleId());
        if (ObjectUtil.isEmpty(currUserRole.getUpId())) {//顶级的角色，不需要判断
            return true;
        }

        SysObject sysObject = sysObjectService.getById(sObjectName);

        //先取出数据的OwnerID
        Map<String, Object> mapObjectData = jdbcTemplate.queryForMap("SELECT OwnerId FROM `" + sysObject.getSDbTable() + "` WHERE " + sysObject.getSIdFieldAs() + "='" + objectId + "'");
        Integer OwnerId = (int) mapObjectData.get("OwnerId");

        //其次判断是否相等
        if (sysCurrUser.getLID().compareTo(OwnerId) == 0) {
            return true;
        }

        //再次判断数据权限
        SysUser owner = sysUserService.getById(OwnerId);
        if (owner.getSysRoleId().compareTo(sysCurrUser.getSysRoleId()) == 0) {//同一个角色
            return true;
        }

        //判断拥有者是否操作人的下级
        SysRole ownerRole = sysRoleService.getById(owner.getSysRoleId());
        if (ownerRole.getSPathId().contains(currUserRole.getSPathId())
                && ownerRole.getSPathId().length() > currUserRole.getSPathId().length()) {
            return true;
        }

        if (sysObject.getBWorkFlow() && sAction.equals("view")) {
            SysApprove sysApprove = sysApproveService.getApprove(sObjectName, objectId);
            return sysApproveService.canView(sysApprove);
        }

        return false;
    }
}

