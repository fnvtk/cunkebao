package cn.myerm.objectbuilder.controller;


import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.service.IJdbcTemplateService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
@RequestMapping("/v1/objectbuilder")
public class JdbcTemplateController extends CommonController {
    @Resource
    private IJdbcTemplateService jdbcTemplateService;

    /**
     * 数据库表列表
     * @return
     */
    @PostMapping("/dbtable/list")
    public MessageDTO dbtablelist() {
        Map<String, Object> list = jdbcTemplateService.tablelist();
        return this.success(list);
    }

    /**
     * 数据库表字段列表
     * @return
     */
    @PostMapping("/column/list")
    public MessageDTO columnlist(String tablename) {
        Map<String, Object> list = jdbcTemplateService.columnlist(tablename);
        return this.success(list);
    }
}
