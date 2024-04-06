package cn.myerm.system.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import cn.myerm.system.entity.SysObjectBusiness;
import cn.myerm.system.mapper.SysObjectBusinessMapper;
import cn.myerm.system.service.ISysObjectBusinessService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@Service
public class SysObjectBusinessServiceImpl extends BusinessServiceImpl<SysObjectBusinessMapper, SysObjectBusiness> implements ISysObjectBusinessService {

    @Resource
    private SysApproveConfigServiceImpl sysApproveConfigService;

    protected JSONArray formatListData(List<Map<String, Object>> listObjectData) {
        JSONArray arrJsonObjectData = new JSONArray();
        JSONObject jsonObjectData = null;
        for (int i = 0; i < listObjectData.size(); i++) {
            List<JSONObject> listBtn = new ArrayList<>();

            //添加查看的按钮
            JSONObject jsonBtn = new JSONObject();
            Integer bWorkFlow = (Integer) listObjectData.get(i).get("bWorkFlow");
            if (bWorkFlow == 1) {
                jsonBtn = new JSONObject();
                jsonBtn.put("ID", "edit");
                jsonBtn.put("sName", "设置审批");
                jsonBtn.put("handler", "handleConfigWorkFlow");
                listBtn.add(jsonBtn);
            }

            jsonObjectData = new JSONObject();
            jsonObjectData.put("btns", listBtn);
            jsonObjectData.put("data", listObjectData.get(i));
            arrJsonObjectData.add(jsonObjectData);
        }

        return arrJsonObjectData;
    }

    /**
     * 新增审批流程
     *
     * @param sObjectName
     * @param sApproveFormData
     */
    public void createCfgSave(String sObjectName, String sApproveFormData) {
        sysApproveConfigService.createCfgSave(sObjectName, sApproveFormData);
    }

    /**
     * 编辑审批流程
     *
     * @param sObjectName
     * @param sApproveFormData
     */
    public void editCfgSave(String sObjectName, String sApproveFormData) {
        sysApproveConfigService.editCfgSave(sObjectName, sApproveFormData);
    }

    /**
     * 删除流程
     * @param lID
     */
    public void removeCfg(Integer lID) {
        sysApproveConfigService.removeCfg(lID);
    }
}
