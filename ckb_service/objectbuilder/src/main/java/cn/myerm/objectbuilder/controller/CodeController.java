package cn.myerm.objectbuilder.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.service.ICodeService;
import cn.myerm.objectbuilder.service.ISysObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/objectbuilder/code")
public class CodeController extends CommonController {

    private final ICodeService codeService;

    private final ISysObjectService sysObjectService;

    @Autowired
    public CodeController(ICodeService codeService, ISysObjectService sysObjectService) {
        this.codeService = codeService;
        this.sysObjectService = sysObjectService;
    }

    @RequestMapping("/test")
    public void test(String tableName, String saveUrl, String basePackageUrl, String objectname, String moduleid) throws Exception {
        codeService.create(tableName, saveUrl, basePackageUrl, objectname, moduleid);
    }

    @RequestMapping("/create")
    public MessageDTO create(@RequestParam(required = true) String sobjectname) throws Exception {
        SysObject sysObject = sysObjectService.getSysObjectByObjectName(sobjectname);
        if (ObjectUtil.isEmpty(sysObject)) {
            return this.fail("对象不存在");
        }

        String table = sysObject.getSDbTable();
        String moduleid = sysObject.getSysModuleId();
        moduleid = moduleid.toLowerCase();

        boolean b = codeService.create(table, null, null, sobjectname, moduleid);
        return this.success(b);
    }

    @RequestMapping("/createlist")
    public MessageDTO createlist(@RequestParam(required = true) String sobjectname) {
        boolean b = codeService.createlist(sobjectname);
        return this.success(b);
    }
}
