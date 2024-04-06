package cn.myerm.objectbuilder.controller;


import cn.hutool.json.JSONArray;
import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.service.ISysObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mars
 * @since 2021-04-08
 */
@RestController
@RequestMapping("/v1/objectbuilder/sysobject/")
public class SysObjectController extends CommonController {
    @Autowired
    ISysObjectService sysObjectService;

    /**
     * 对象列表
     * @return
     */
    @PostMapping("/list")
    public MessageDTO list() {
        List<SysObject> list = sysObjectService.sysObjectList();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("sysobjects", list);

        return this.success(resultMap);
    }

    /**
     * 对象新建保存
     * @param table
     * @param name
     * @param moduleid
     * @param datasourceid
     * @param isworkflow
     * @param isdetail
     * @param isauto
     * @return
     */
    @PostMapping("/newsave")
    public MessageDTO newsave(@RequestParam(required = true) String table, @RequestParam(required = true)String name, @RequestParam(required = true)String moduleid, String datasourceid, Boolean isworkflow, Boolean isdetail, Boolean isauto) {
        boolean b = sysObjectService.newsave(table, name, moduleid, datasourceid, isworkflow, isdetail, isauto);
        return this.success(b);
    }

    /**
     * 对象编辑保存
     * @param sobjectname
     * @param name
     * @param datasourceid
     * @return
     */
    @PostMapping("/editsave")
    public MessageDTO editsave(@RequestParam(required = true)String sobjectname, String name, String datasourceid) {
        boolean b = sysObjectService.editsave(sobjectname, name, datasourceid);
        return this.success(b);
    }

    /**
     * 对象删除
     * @param sobjectname
     * @return
     */
    @PostMapping("/del")
    public MessageDTO del(String sobjectname) {
        boolean b = sysObjectService.del(sobjectname);
        return this.success(b);
    }

    /**
     * 对象详情
     * @param sobjectname
     * @return
     */
    @PostMapping("/detail")
    public MessageDTO detail(String sobjectname) {
        SysObject sysObject = sysObjectService.detail(sobjectname);
        return this.success(sysObject);
    }

    /**
     * 附加
     * @param table
     * @param name
     * @param moduleid
     * @param datasourceid
     * @param isworkflow
     * @param idfield
     * @param namefield
     * @return
     */
    @PostMapping("/attachsave")
    public MessageDTO attachsave(@RequestParam(required = true) String table,
                                 @RequestParam(required = true) String name,
                                 @RequestParam(required = true) String moduleid,
                                 @RequestParam(required = true) String datasourceid,
                                 @RequestParam(name = "isworkflow") Boolean isworkflow,
                                 @RequestParam(required = true) String idfield,
                                 @RequestParam(name = "isauto") Boolean isauto,
                                 @RequestParam(required = false) String pktype,
                                 @RequestParam(required = true) String namefield) {
        boolean b = sysObjectService.attachsave(table, name, moduleid, datasourceid, isworkflow, idfield, namefield,isauto,pktype);
        return this.success(b);
    }

    /**
     * 操作权限列表
     * @param sobjectname
     * @return
     */
    @PostMapping("/operation/list")
    public MessageDTO operationlist(String sobjectname) {
        JSONArray jsonArray = sysObjectService.operationlist(sobjectname);
        return this.success(jsonArray);
    }

    /**
     * 新建操作权限
     *
     * @param sobjectname
     * @param id
     * @param name
     * @return
     */
    @PostMapping("/operation/newsave")
    public MessageDTO operationnewsave(String sobjectname, String id, String name) {
        boolean b = sysObjectService.operationnewsave(sobjectname, id, name);
        return this.success(b);
    }

    /**
     * 修改操作权限
     * @param sobjectname
     * @param id
     * @param name
     * @return
     */
    @PostMapping("/operation/editsave")
    public MessageDTO operationeditsave(String sobjectname, String id, String name) {
        boolean b = sysObjectService.operationeditsave(sobjectname, id, name);
        return this.success(b);
    }

    /**
     * 删除操作权限
     * @param sobjectname
     * @param id
     * @return
     */
    @PostMapping("/operation/del")
    public MessageDTO operationdel(String sobjectname, String id) {
        boolean b = sysObjectService.operationdel(sobjectname, id);
        return this.success(b);
    }

}
