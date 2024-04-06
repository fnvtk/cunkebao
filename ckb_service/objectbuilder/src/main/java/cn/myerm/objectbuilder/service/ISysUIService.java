package cn.myerm.objectbuilder.service;

import com.alibaba.fastjson.JSONArray;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.service.ICommonService;
import cn.myerm.objectbuilder.entity.SysField;
import cn.myerm.objectbuilder.entity.SysUI;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Mars
 * @since 2021-04-08
 */
public interface ISysUIService extends ICommonService<SysUI> {
    List<Map<String, Object>> list(String sObjectName);

    Map<String, Object> info(SysUI sysUI);

    boolean newsave(String sobjectname, String name, String type, String permission,String configs);

    boolean editsave(Integer id, String name, String type, String permission);

    boolean fieldclassnewsave(Integer uiid, String name, String group1, String group2, String group3);

    boolean fieldclasseditsave(Integer uiid, Integer id, String name, String group1, String group2, String group3);

    boolean del(String id);

    boolean fieldclassdel(Integer uiid, Integer id);

    Map<String, Object> view(Integer id);

    List<SysField> getUIFieldList(String sObjectName,Integer id);

    boolean fieldclassorder(Integer uiid, String order);

    boolean clone(Integer id);
}