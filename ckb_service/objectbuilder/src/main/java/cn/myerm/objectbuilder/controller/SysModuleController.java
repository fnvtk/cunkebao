package cn.myerm.objectbuilder.controller;


import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.entity.SysModule;
import cn.myerm.objectbuilder.service.ISysModuleService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
@RequestMapping("/v1/objectbuilder/sysmodule")
public class SysModuleController extends CommonController {
    @Resource
    private ISysModuleService sysModuleService;

    /**
     * 模块列表
     *
     * @return
     */
    @PostMapping("/list")
    public MessageDTO list() {
        List<SysModule> list = sysModuleService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("sysmodules", list);
        return this.success(result);
    }
}
