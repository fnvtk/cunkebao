package cn.myerm.objectbuilder.controller;


import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.entity.SysList;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.params.ListParams;
import cn.myerm.objectbuilder.service.ISysListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Mars
 * @since 2021-04-09
 */
@RestController
@RequestMapping("/v1/objectbuilder/syslist/")
public class SysListController extends CommonController {
    @Autowired
    ISysListService sysListService;

    /**
     * 视图列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/list")
    public MessageDTO list(@RequestParam(required = true) String sobjectname) {
        List<Map<String, Object>> list = sysListService.sysListList(sobjectname);
        return this.success(list);
    }

    /**
     * 视图新增
     *
     * @param listParams
     * @return
     */
    @PostMapping("/newsave")
    public MessageDTO newsave(@Valid ListParams listParams) {
        boolean b = sysListService.newsave(listParams);
        return this.success(b);
    }

    /**
     * 视图编辑
     *
     * @param listParams
     * @return
     */
    @PostMapping("/editsave")
    public MessageDTO editsave(@Valid ListParams listParams) {
        boolean b = sysListService.editsave(listParams);
        return this.success(b);
    }

    /**
     * 复制
     * @param id
     * @return
     */
    @PostMapping("/clone")
    public MessageDTO clone(@RequestParam(required = true) Integer id) {
        sysListService.clone(id);
        return this.success("操作成功");
    }

    /**
     * 视图删除
     *
     * @param id
     * @return
     */
    @PostMapping("/del")
    public MessageDTO del(@RequestParam(required = true) String id) {
        List<Integer> ids = new ArrayList<>();
        if (id.indexOf(",") != -1) {
            String[] arr = id.split(",");
            for (String str : arr) {
                Integer lid = Integer.parseInt(str);
                ids.add(lid);
            }
        } else {
            ids.add(Integer.parseInt(id));
        }
        boolean b = sysListService.removeByIds(ids);

        return this.success(b);
    }

    /**
     * 编辑视图显示列
     *
     * @param id
     * @param selectid
     * @return
     */
    @PostMapping("/editselectid")
    public MessageDTO editselectid(@RequestParam(required = true) Integer id, @RequestParam(required = true) String selectid) {
        boolean b = sysListService.editlistfield(id, selectid);
        return this.success(b);
    }
}
