package cn.myerm.objectbuilder.controller;


import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.service.ISysUIService;
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
@RequestMapping("/v1/objectbuilder/sysui")
public class SysUIController extends CommonController {
    @Autowired
    ISysUIService sysUIService;

    /**
     * 界面列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/list")
    public MessageDTO list(String sobjectname) {
        Map<String, Object> result = new HashMap<>();
        result.put("sysuis", sysUIService.list(sobjectname));
        return this.success(result);
    }

    /**
     * 新建保存
     *
     * @param sobjectname
     * @param name
     * @param type
     * @param permission
     * @return
     */
    @PostMapping("/newsave")
    public MessageDTO newsave(@RequestParam(required = true) String sobjectname, @RequestParam(required = true) String name, @RequestParam(required = true) String type, String permission) {
        return this.success(sysUIService.newsave(sobjectname, name, type, permission, null));
    }

    /**
     * 编辑保存
     *
     * @param id
     * @param name
     * @param type
     * @param permission
     * @return
     */
    @PostMapping("/editsave")
    public MessageDTO editsave(@RequestParam(required = true) Integer id, @RequestParam(required = true) String name, @RequestParam(required = true) String type, String permission) {
        return this.success(sysUIService.editsave(id, name, type, permission));
    }

    /**
     * 新建信息块保存
     *
     * @param uiid
     * @param name
     * @param group1
     * @param group2
     * @param group3
     * @return
     */
    @PostMapping("/fieldclass/newsave")
    public MessageDTO fieldclassnewsave(Integer uiid, String name, String group1, String group2, String group3) {
        return this.success(sysUIService.fieldclassnewsave(uiid, name, group1, group2, group3));
    }

    /**
     * 编辑信息块保存
     *
     * @param uiid
     * @param id
     * @param name
     * @param group1
     * @param group2
     * @param group3
     * @return
     */
    @PostMapping("/fieldclass/editsave")
    public MessageDTO fieldclasseditsave(Integer uiid, Integer id, String name, String group1, String group2, String group3) {
        return this.success(sysUIService.fieldclasseditsave(uiid, id, name, group1, group2, group3));
    }

    /**
     * 删除界面
     *
     * @param id
     * @return
     */
    @PostMapping("/del")
    public MessageDTO del(String id) {
        return this.success(sysUIService.del(id));
    }

    /**
     * 删除信息块
     *
     * @param uiid
     * @param id
     * @return
     */
    @PostMapping("/fieldclass/del")
    public MessageDTO fieldclassdel(Integer uiid, Integer id) {
        return this.success(sysUIService.fieldclassdel(uiid, id));
    }

    /**
     * 删除信息块
     *
     * @param uiid
     * @param order
     * @return
     */
    @PostMapping("/fieldclass/order")
    public MessageDTO fieldclassorder(Integer uiid, String order) {
        boolean b = sysUIService.fieldclassorder(uiid, order);
        return this.success(b);
    }

    /**
     * 界面详情
     *
     * @param id
     * @return
     */
    @PostMapping("/view")
    public MessageDTO view(Integer id) {
        Map<String, Object> info = sysUIService.view(id);
        return this.success(info);
    }

    /**
     * 界面未选的属性列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/uifieldlist")
    public MessageDTO uifieldlist(String sobjectname, Integer id) {
        List<SysField> list = sysUIService.getUIFieldList(sobjectname, id);
        return this.success(list);
    }

    /**
     * 界面详情
     *
     * @param id
     * @return
     */
    @PostMapping("/clone")
    public MessageDTO clone(Integer id) {
        boolean b = sysUIService.clone(id);
        return this.success(b);
    }
}
