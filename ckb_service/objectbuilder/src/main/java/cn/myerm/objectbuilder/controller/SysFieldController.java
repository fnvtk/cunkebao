package cn.myerm.objectbuilder.controller;

import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.params.FieldParams;
import cn.myerm.objectbuilder.service.ISysFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
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
 * @since 2021-04-08
 */
@RestController
@RequestMapping("/v1/objectbuilder/sysfield")
public class SysFieldController extends CommonController {

    private ISysFieldService sysFieldService;

    @Autowired
    public SysFieldController(ISysFieldService sysFieldService) {
        this.sysFieldService = sysFieldService;
    }

    /**
     * 列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/list")
    public MessageDTO list(@RequestParam(required = true) String sobjectname) {
        List<SysField> list = sysFieldService.getAllFieldByObject(sobjectname);
        Map<String, Object> result = new HashMap<>();
        result.put("sysfields", list);
        return this.success(result);
    }

    /**
     * 新建保存
     *
     * @param fieldParams
     * @return
     */
    @PostMapping("/newsave")
    public MessageDTO newsave(@Valid FieldParams fieldParams, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                return this.fail(error.getDefaultMessage());
            }
        }
        Integer lID = sysFieldService.newSave(fieldParams);
        return this.success(lID);
    }

    /**
     * 编辑保存
     *
     * @param fieldParams
     * @return
     */
    @PostMapping("/editsave")
    public MessageDTO editsave(@Valid FieldParams fieldParams, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            for (ObjectError error : bindingResult.getAllErrors()) {
                return this.fail(error.getDefaultMessage());
            }
        }
        boolean b = sysFieldService.editSave(fieldParams);
        return this.success(b);
    }

    /**
     * 删除
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
        boolean b = sysFieldService.removeByIds(ids);
        return this.success(b);
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @PostMapping("/view")
    public MessageDTO view(@RequestParam(required = true) Integer id) {
        SysField sysField = sysFieldService.getFieldById(id);
        return this.success(sysField);
    }

    /**
     * 可引用的对象列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/refobjlist")
    public MessageDTO refobjlist(@RequestParam(required = true) String sobjectname) {
        List<Map<String, Object>> resultList = sysFieldService.refobjlist(sobjectname);

        Map<String, Object> map = new HashMap<>();
        map.put("refobjs", resultList);
        return this.success(map);
    }

    /**
     * 可引用的对象属性列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/refobjfieldlist")
    public MessageDTO refobjfieldlist(@RequestParam(required = true) String sobjectname) {
        List<Map<String, Object>> resultList = sysFieldService.refobjfieldlist(sobjectname);
        Map<String, Object> map = new HashMap<>();
        map.put("refobjfields", resultList);
        return this.success(map);
    }

    /**
     * 公共属性列表
     *
     * @return
     */
    @PostMapping("/commonfieldlist")
    public MessageDTO commonfieldlist() {
        List<Map<String, Object>> resultList = sysFieldService.commonfieldlist();
        Map<String, Object> map = new HashMap<>();
        map.put("commonfields", resultList);
        return this.success(map);
    }

    /**
     * 表字段列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/column/list")
    public MessageDTO columnlist(@RequestParam(required = true) String sobjectname) {
        Map<String, Object> resultList = sysFieldService.columnlist(sobjectname);
        return this.success(resultList);
    }

    /**
     * 附加表字段列表
     *
     * @param sobjectname
     * @return
     */
    @PostMapping("/attachfieldlist")
    public MessageDTO attachfieldlist(@RequestParam(required = true) String sobjectname) {
        List<String> resultList = sysFieldService.attachfieldlist(sobjectname);
        return this.success(resultList);
    }


    /**
     * 属性引用字段ID
     *
     * @param
     * @return
     */
    @PostMapping("/reffieldid")
    public MessageDTO refFieldId(@RequestParam(required = true) Integer objid, @RequestParam(required = true) String fieldname) {
        Integer fieldId = sysFieldService.getRefFieldId(objid, fieldname);
        return this.success(fieldId);
    }
}
