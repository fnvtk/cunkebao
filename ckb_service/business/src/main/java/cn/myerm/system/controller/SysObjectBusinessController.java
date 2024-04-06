package cn.myerm.system.controller;


import cn.myerm.business.controller.BusinessController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.service.impl.SysApproveConfigServiceImpl;
import cn.myerm.system.service.impl.SysObjectBusinessServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@RestController
@RequestMapping("/v1/backend/system/sysobject")
public class SysObjectBusinessController extends BusinessController {

    private final SysObjectBusinessServiceImpl sysObjectBusinessService;

    private final SysApproveConfigServiceImpl sysApproveConfigService;

    @Autowired
    public SysObjectBusinessController(SysObjectBusinessServiceImpl sysObjectBusinessService, SysApproveConfigServiceImpl sysApproveConfigService) {
        this.businessService = sysObjectBusinessService;
        this.sysObjectBusinessService = sysObjectBusinessService;
        this.sysApproveConfigService = sysApproveConfigService;
    }

    /**
     * 新增审批流程
     *
     * @return
     */
    @PostMapping("/approve/config/createsave")
    public MessageDTO createCfgSave(@RequestParam(required = true) String sObjectName,
                                    @RequestParam(required = true) String sApproveFormData
    ) {
        sysObjectBusinessService.createCfgSave(sObjectName, sApproveFormData);
        return success();
    }

    /**
     * 编辑审批流程
     *
     * @return
     */
    @PostMapping("/approve/config/editsave")
    public MessageDTO editCfgSave(@RequestParam(required = true) String sObjectName,
                                    @RequestParam(required = true) String sApproveFormData
    ) {
        sysObjectBusinessService.editCfgSave(sObjectName, sApproveFormData);
        return success();
    }

    /**
     * 编辑审批流程
     *
     * @return
     */
    @PostMapping("/approve/config/remove")
    public MessageDTO editCfgSave(@RequestParam(required = true) Integer lID) {
        sysObjectBusinessService.removeCfg(lID);
        return success();
    }


    /**
     * 查询所有的节点
     * @param sObjectName
     * @return
     */
    @PostMapping("/approve/config/all")
    public MessageDTO queryCfg(@RequestParam(required = true) String sObjectName) {
        return success(sysApproveConfigService.queryAll(sObjectName));
    }
}
