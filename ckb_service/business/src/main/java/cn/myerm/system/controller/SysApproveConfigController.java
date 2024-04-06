package cn.myerm.system.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import cn.myerm.business.controller.BusinessController;
import cn.myerm.system.service.impl.SysApproveConfigServiceImpl;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@RestController
@RequestMapping("/v1/backend/system/sys-approve-config")
public class SysApproveConfigController extends BusinessController {
    @Autowired
    public SysApproveConfigController (SysApproveConfigServiceImpl sysApproveConfigServiceImpl) {
        this.businessService = sysApproveConfigServiceImpl;
    }
}
