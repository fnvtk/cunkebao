package cn.myerm.system.controller;


import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.service.impl.SysObjectServiceImpl;
import cn.myerm.system.service.impl.SysOperatorPermissionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mars
 * @since 2021-04-19
 */
@RestController
@RequestMapping("/v1/backend/system/sysopera")
public class SysOperatorPermissionController extends BusinessController {

    private SysObjectServiceImpl sysObjectService;
    private SysOperatorPermissionServiceImpl sysOperatorPermissionService;

    @Autowired
    public SysOperatorPermissionController(SysOperatorPermissionServiceImpl sysOperatorPermissionService, SysObjectServiceImpl sysObjectService) {
        this.businessService = sysOperatorPermissionService;
        this.sysObjectService = sysObjectService;
        this.sysOperatorPermissionService = sysOperatorPermissionService;
    }

    @PostMapping("/all")
    public MessageDTO all(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                          @RequestParam(name = "objectid", required = true) String objectId) {

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("arrOperator", sysObjectService.list());
        resultMap.put("arrPermission", sysOperatorPermissionService.queryAll(sObjectName, objectId));

        return this.success(resultMap);
    }

    @PostMapping("/save")
    public MessageDTO save(@RequestParam(name = "sobjectname", required = true) String sObjectName,
                          @RequestParam(name = "objectid", required = true) String objectId,
                          @RequestParam(name = "permission", required = true) String sPermission
                          ) {
        sysOperatorPermissionService.updateSave(sObjectName, objectId, sPermission);
        return this.success();
    }

}
