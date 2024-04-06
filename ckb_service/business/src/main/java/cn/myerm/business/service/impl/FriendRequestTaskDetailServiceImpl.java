package cn.myerm.business.service.impl;

import cn.myerm.business.entity.FriendRequestTaskDetail;
import cn.myerm.business.mapper.FriendRequestTaskDetailMapper;
import cn.myerm.business.service.IFriendRequestTaskDetailService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class FriendRequestTaskDetailServiceImpl extends BusinessServiceImpl<FriendRequestTaskDetailMapper, FriendRequestTaskDetail> implements IFriendRequestTaskDetailService {

    private static final Logger logger = LoggerFactory.getLogger(FriendRequestTaskDetailServiceImpl.class);

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();

        btn = new JSONObject();
        btn.put("ID", "import");
        btn.put("sName", "导入");
        btn.put("handler", "handleFriendReqImport");
        btn.put("icon", "el-icon-upload2");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "export");
        btn.put("sName", "导出");
        btn.put("handler", "handleFriendReqExport");
        btn.put("icon", "el-icon-download");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "del");
        btn.put("sName", "删除");
        btn.put("handler", "handleDel");
        btn.put("icon", "el-icon-delete");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "refresh");
        btn.put("sName", "刷新");
        btn.put("handler", "handleRefresh");
        btn.put("icon", "el-icon-refresh");
        arrBtn.add(btn);

        return arrBtn;
    }
}
