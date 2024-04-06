package cn.myerm.objectbuilder.service;

import cn.myerm.common.service.ICommonService;
import cn.myerm.objectbuilder.entity.SysField;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.myerm.objectbuilder.entity.SysObject;
import cn.myerm.objectbuilder.params.FieldParams;

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
public interface ISysFieldService extends ICommonService<SysField> {
    List<SysField> getAllFieldByObject(String sobjectname);

    Integer newSave(FieldParams fieldParams);

    boolean editSave(FieldParams fieldParams);

    boolean del(Integer id);

    SysField updateSysField(SysField sysField, FieldParams fieldParams);

    SysField getFieldById(Integer id);

    List<Map<String, Object>> refobjlist(String sobjectname);

    List<Map<String, Object>> refobjfieldlist(String sobjectname);

    List<Map<String, Object>> commonfieldlist();

    void addColumnToTable(String tableName, String dataType, String columnName, Boolean isautoincrement, Boolean isPk, String uiType);

    Map<String, Object> columnlist(String sobjectname);

    SysField getByFieldName(String sObjectName, String sFieldName);

    List<SysField> getByObjectName(String sObjectName);

    List<String> attachfieldlist(String sObjectName);

    Integer getRefFieldId(Integer objId, String fieldName);
}
