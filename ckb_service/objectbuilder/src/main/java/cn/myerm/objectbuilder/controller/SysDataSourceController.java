package cn.myerm.objectbuilder.controller;


import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.entity.SysDataSource;
import cn.myerm.objectbuilder.service.impl.SysDataSourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
@RequestMapping("/v1/objectbuilder/sysdatasource")
public class SysDataSourceController extends CommonController {

    private SysDataSourceServiceImpl sysDataSourceService;

    @Autowired
    public SysDataSourceController(SysDataSourceServiceImpl sysDataSourceService) {
        this.sysDataSourceService = sysDataSourceService;
    }

    /**
     * 数据源列表
     *
     * @return
     */
    @PostMapping("/list")
    public MessageDTO list() {
        List<SysDataSource> list = sysDataSourceService.list();
        Map<String, Object> result = new HashMap<>();
        result.put("sysdatasources", list);
        return this.success(result);
    }
}
