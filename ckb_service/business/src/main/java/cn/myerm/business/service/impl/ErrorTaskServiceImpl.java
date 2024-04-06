package cn.myerm.business.service.impl;

import cn.myerm.business.entity.ErrorTask;
import cn.myerm.business.mapper.ErrorTaskMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IErrorTaskService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ErrorTaskServiceImpl extends BusinessServiceImpl<ErrorTaskMapper, ErrorTask> implements IErrorTaskService {
    private static final Logger logger = LoggerFactory.getLogger(BusinessServiceImpl.class);

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        logger.info(mapParam.toString());

        JSONObject btn = new JSONObject();

        //待处理视图
        if (mapParam.get("ListId").equals("1531")) {
            btn.put("ID", "response");
            btn.put("sName", "已响应");
            btn.put("handler", "handleErrorTaskResponse");
            btn.put("icon", "el-icon-erm-xiangying");
            arrBtn.add(btn);
        }

        //处理中的视图
        if (mapParam.get("ListId").equals("1533")) {
            btn.put("ID", "done");
            btn.put("sName", "已解决");
            btn.put("handler", "handleErrorTaskDone");
            btn.put("icon", "el-icon-erm-wancheng");
            arrBtn.add(btn);
        }


        btn = new JSONObject();
        btn.put("ID", "export");
        btn.put("sName", "导出");
        btn.put("handler", "handleExport");
        btn.put("icon", "el-icon-download");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "refresh");
        btn.put("sName", "刷新");
        btn.put("handler", "handleRefresh");
        btn.put("icon", "el-icon-refresh");
        arrBtn.add(btn);

        return arrBtn;
    }

    /**
     * 响应
     *
     * @param listParam 客户端传值
     */
    @Transactional
    public void handle(ListParam listParam, String sAction) {
        //设置全部出来，不要分页
        listParam.setCanpage(0);

        //只要id字段
        List<String> listDispCol = new ArrayList<>();
        listDispCol.add("lId");
        listParam.setDispcol(listDispCol);

        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        List<ErrorTask> errorTaskList = new ArrayList<>();
        for (Map<String, Object> mapObjectData : listMapObjectData) {
            Map<String, Object> data = (Map<String, Object>) mapObjectData.get("data");

            ErrorTask errorTask = new ErrorTask();
            errorTask.setLId((Integer) data.get("lId"));

            if (sAction.equals("response")) {
                errorTask.setStatusId("handling");
                errorTask.setDResponseTime(LocalDateTime.now());
            } else if (sAction.equals("done")) {
                errorTask.setStatusId("done");
                errorTask.setDDoneTime(LocalDateTime.now());
            }

            errorTaskList.add(errorTask);
        }

        updateBatchById(errorTaskList);
    }
}
