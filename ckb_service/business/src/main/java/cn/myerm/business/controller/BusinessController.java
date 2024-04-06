package cn.myerm.business.controller;

import cn.hutool.core.util.StrUtil;
import cn.myerm.business.annotation.OperaPermission;
import cn.myerm.business.dto.MaterialLibDto;
import cn.myerm.business.entity.MaterialLib;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import cn.myerm.objectbuilder.entity.SysObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基础的业务控制层
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
 * @since 2021年4月19日15:09:31
 */
@RestController
public class BusinessController extends CommonController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusinessController.class);

    /**
     * 主对象通用的业务服务
     */
    protected BusinessServiceImpl businessService;

    /**
     * 子对象通用的业务服务（如果有子对象）
     */
    protected BusinessServiceImpl subBusinessService;

    /**
     * 删除对象
     *
     * @return MessageDTO
     */
    @PostMapping("/del")
    @OperaPermission("del")
    public MessageDTO del(@Validated ListParam listParam) {
        businessService.del(listParam);
        return success("");
    }

    @PostMapping("/alloc")
    @OperaPermission("alloc")
    public MessageDTO alloc(@Validated ListParam listParam) {
        businessService.alloc(listParam);
        return success("");
    }


    /**
     * 对象详情
     *
     * @return MessageDTO
     */
    @PostMapping("/view")
    @OperaPermission("view")
    public MessageDTO view(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                           @RequestParam(name = "objectid", required = true) String ObjectId) {
        return success(businessService.getViewData(sObjectName, ObjectId));
    }


    /**
     * 编辑
     *
     * @return MessageDTO
     */
    @PostMapping("/edit")
    @OperaPermission("edit")
    public MessageDTO edit(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                           @RequestParam(name = "ssubobjectname") String sSubObjectName,
                           @RequestParam(name = "objectid", required = true) String ObjectId) {

        Map<String, Object> objectData = businessService.getOneObjectData(sObjectName, ObjectId);

        List<Map<String, Object>> listSubObjectData = null;
        if (sSubObjectName != null && subBusinessService != null) {
            listSubObjectData = subBusinessService.getSubObjectData(sSubObjectName, ObjectId);
        }

        JSONArray arrBtn = businessService.getEditUiBtn(sObjectName, ObjectId);

        Map<String, Object> map = new HashMap<>();
        map.put("masterobjectdata", objectData);
        map.put("subobjectdata", listSubObjectData);
        map.put("btn", arrBtn);

        return success(map);
    }

    /**
     * 对象的视图数据
     *
     * @param listParam 视图传参的接收对象
     * @return MessageDTO
     */
    @PostMapping("/list")
    @OperaPermission("list")
    public MessageDTO list(@Validated ListParam listParam) {
        return success(businessService.getListData(listParam));
    }

    /**
     * 对象导出
     *
     * @param listParam 视图传参的接收对象
     * @return MessageDTO
     */
    @PostMapping("/export")
    @OperaPermission("export")
    public MessageDTO export(@Validated ListParam listParam) {
        String sFileName = businessService.export(listParam);
        Map<String, Object> map = new HashMap<>();
        map.put("filename", sFileName);
        return success(map);
    }

    /**
     * 新建保存
     *
     * @return MessageDTO
     */
    @PostMapping("/newsave")
    @OperaPermission("new")
    public MessageDTO newSave(@RequestParam(name = "smasterobjectname", required = true) String sMasterObjectName,
                              @RequestParam(name = "ssubobjectname") String sSubObjectName,
                              @RequestParam(name = "masterobjectdata", required = true) String sMasterObjectData,
                              @RequestParam(name = "subobjectdata") String sSubObjectData) {
        //处理主对象，返回主对象的ID
        Object masterObjectId = businessService.newSave(sMasterObjectName, sMasterObjectData);

        //处理子对象（如果有）
        if (subBusinessService != null) {
            if (!StrUtil.hasEmpty(sSubObjectName) && !StrUtil.hasEmpty(sSubObjectData)) {
                JSONArray arrJsonObjectData = JSONObject.parseArray(sSubObjectData);
                for (Object objectData : arrJsonObjectData) {
                    JSONObject jsonObjectData = (JSONObject) objectData;

                    //把父对象的ID，压入子对象的数据中
                    jsonObjectData.put("ParentId", masterObjectId);
                    subBusinessService.newSave(sSubObjectName, jsonObjectData.toJSONString());
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("ID", masterObjectId);
        return success(map);
    }

    /**
     * 编辑对象保存
     *
     * @return MessageDTO
     */
    @PostMapping("/editsave")
    @OperaPermission("edit")
    public MessageDTO editSave(@RequestParam(name = "objectid", required = true) String masterObjectID,
                               @RequestParam(name = "smasterobjectname", required = true) String sMasterObjectName,
                               @RequestParam(name = "ssubobjectname") String sSubObjectName,
                               @RequestParam(name = "masterobjectdata", required = true) String sMasterObjectData,
                               @RequestParam(name = "subobjectdata") String sSubObjectData,
                               @RequestParam(name = "approveaction") String sApproveAction
    ) {

        //主对象编辑保存
        businessService.editSave(masterObjectID, sMasterObjectName, sMasterObjectData, sApproveAction);

        //处理子对象（如果有）
        if (subBusinessService != null) {
            if (!StrUtil.hasEmpty(sSubObjectName) && !StrUtil.hasEmpty(sSubObjectData)) {
                JSONArray arrJsonObjectData = JSONObject.parseArray(sSubObjectData);

                //先处理删除的数据
                List<Integer> listSubmitId = new ArrayList<>();//客户提交的数据ID集合
                for (Object objectData : arrJsonObjectData) {
                    JSONObject jsonObjectData = (JSONObject) objectData;
                    if (!StrUtil.hasEmpty(jsonObjectData.getString("ID"))) {
                        listSubmitId.add(Integer.valueOf(jsonObjectData.getString("ID")));
                    }
                }
                subBusinessService.delRemoveData(sSubObjectName, masterObjectID, listSubmitId);

                for (Object objectData : arrJsonObjectData) {
                    JSONObject jsonObjectData = (JSONObject) objectData;

                    if (StrUtil.hasEmpty(jsonObjectData.getString("ID"))) {//如果传入的数据带有ID数据，表明是旧数据，用编辑
                        jsonObjectData.put("ParentId", masterObjectID);
                        subBusinessService.newSave(sSubObjectName, jsonObjectData.toJSONString());
                    } else {//不带有ID，表明是新增
                        subBusinessService.editSave(jsonObjectData.getString("ID"), sSubObjectName, jsonObjectData.toJSONString());
                    }
                }
            }
        }

        Map<String, Object> map = new HashMap<>();
        map.put("ID", masterObjectID);
        return success(map);
    }

    /**
     * 主页分类标签的分类
     *
     * @return MessageDTO
     */
    @PostMapping("/tab/list")
    public MessageDTO tabList(@RequestParam(name = "sobjectname", required = true) String sObjectName) {
        return success(businessService.getTabList(sObjectName));
    }

    /**
     * 参照视图
     *
     * @param sObjectName
     * @return
     */
    @PostMapping("/refer/list")
    public MessageDTO referList(@RequestParam(name = "sobjectname", required = true) String sObjectName) {
        return success(businessService.getReferList(sObjectName));
    }

    /**
     * 获取对象的新建界面的配置信息
     *
     * @return MessageDTO
     */
    @PostMapping("/config/ui")
    public MessageDTO uiConfig(@RequestParam(name = "stype", required = true) String sType, @RequestParam(name = "sobjectname", required = true) String sObjectName) {
        return success(businessService.getUIConfig(sObjectName, sType));
    }

    /**
     * 获取对象的视图配置信息
     *
     * @param sObjectName 对象名
     * @return MessageDTO
     */
    @PostMapping("/config/list")
    public MessageDTO listConfig(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                                 @RequestParam(name = "listid") String ListId,
                                 @RequestParam(name = "type", required = true) String sType,
                                 @RequestParam(name = "relatedid", required = false) String RelateId,
                                 @RequestParam(name = "objectid", required = false) String ObjectId,
                                 @RequestParam(name = "sfromobjectname", required = false) String sFromObjectName
    ) {
        Map<String, String> mapParam = new HashMap<>();
        mapParam.put("sObjectName", sObjectName);
        mapParam.put("ListId", ListId);
        mapParam.put("sType", sType);
        mapParam.put("SysRelatedListId", RelateId);
        mapParam.put("ObjectId", ObjectId);
        mapParam.put("sFromObjectName", sFromObjectName);

        Map<String, Object> map = businessService.getListConfig(mapParam);
        JSONArray arrBtn = businessService.getListTableBtn(mapParam);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", map);
        resultMap.put("arrBtn", arrBtn);

        return success(resultMap);
    }

    /**
     * 获取对象的配置信息
     *
     * @param sObjectName 对象名
     * @return MessageDTO
     */
    @PostMapping("/config/object")
    public MessageDTO objectConfig(@RequestParam(name = "sobjectname", required = true) String sObjectName) {
        SysObject sysObject = businessService.getObjectConfig(sObjectName);
        SysObject subSysObject = businessService.getSubObject(sObjectName);
        JSONArray arrOperationPermit = businessService.getOperationPermit(sysObject);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sysobject", sysObject);
        resultMap.put("subsysobject", subSysObject);
        resultMap.put("opera", arrOperationPermit);

        return this.success(resultMap);
    }

    /**
     * 上传文件
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public MessageDTO upload(@RequestParam("file") MultipartFile file, @RequestParam(name = "sobjectname", required = true) String sObjectName) {
        if (file.isEmpty()) {
            return fail("上传失败，请选择文件");
        }

        return success(businessService.upload(file, sObjectName));
    }

    /**
     * 审批通过
     * @return
     */
    @PostMapping("/approve/pass")
    public MessageDTO approvePass(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                                  @RequestParam(name = "objectid", required = true) String objectId
    ) {
        businessService.approvePass(sObjectName, objectId);
        return success();
    }

    /**
     * 审批废弃
     *
     * @param sObjectName
     * @param objectId
     * @return
     */
    @PostMapping("/approve/deprecate")
    public MessageDTO approveDeprecate(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                                       @RequestParam(name = "objectid", required = true) String objectId
    ) {
        businessService.approveDeprecate(sObjectName, objectId);
        return success();
    }

    /**
     * 审批驳回
     *
     * @param sObjectName
     * @param objectId
     * @return
     */
    @PostMapping("/approve/reject")
    public MessageDTO approveReject(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                                    @RequestParam(name = "objectid", required = true) String objectId
    ) {
        businessService.approveReject(sObjectName, objectId);
        return success();
    }

    @PostMapping("/getone")
    public MessageDTO getOne(Integer id) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", businessService.getById(id));

        return success(jsonObject);
    }
}
