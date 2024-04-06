package cn.myerm.objectbuilder.service;

import cn.hutool.json.JSONArray;
import cn.myerm.common.service.ICommonService;
import cn.myerm.objectbuilder.entity.SysObject;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Mars
 * @since 2021-04-08
 */
public interface ISysObjectService extends ICommonService<SysObject> {
    SysObject getSysObjectByObjectName(String objectName);

    List<SysObject> sysObjectList();

    boolean newsave(String table, String name, String moduleid, String datasourceid, Boolean isworkflow, Boolean isdetail,Boolean isauto);

    boolean editsave(String sobjectname, String name, String datasourceid);

    boolean del(String sobjectname);

    SysObject detail(String sobjectname);

    boolean attachsave(String table, String name, String moduleid, String datasourceid, Boolean isworkflow,String idfield,String namefield,Boolean isauto,String pktype);

    JSONArray operationlist(String sobjectname);

    boolean operationnewsave(String sobjectname, String id, String name);

    boolean operationeditsave(String sobjectname, String id, String name);

    boolean operationdel(String sobjectname, String id);
}
