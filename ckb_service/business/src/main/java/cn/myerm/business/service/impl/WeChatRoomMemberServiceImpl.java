package cn.myerm.business.service.impl;

import cn.myerm.business.dto.WeChatDto;
import cn.myerm.business.dto.WeChatRoomDto;
import cn.myerm.business.dto.WeChatRoomMemberDto;
import cn.myerm.business.entity.WeChat;
import cn.myerm.business.entity.WeChatRoom;
import cn.myerm.business.entity.WeChatRoomMember;
import cn.myerm.business.mapper.WeChatRoomMemberMapper;
import cn.myerm.business.service.IWeChatRoomMemberService;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WeChatRoomMemberServiceImpl extends BusinessServiceImpl<WeChatRoomMemberMapper, WeChatRoomMember> implements IWeChatRoomMemberService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatRoomMemberServiceImpl.class);

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();
        btn.put("ID", "select");
        btn.put("sName", "批量选中");
        btn.put("handler", "handleBatchSelect");
        btn.put("icon", "el-icon-check");
        arrBtn.add(btn);

        arrBtn.addAll(super.getListTableBtn(mapParam));

        return arrBtn;
    }

    public List<WeChatRoomMemberDto> getByIds(String sMemberIds) {
        List<WeChatRoomMemberDto> listMember = new ArrayList<>();

        JSONArray jsonArrayMemberId = JSONArray.parseArray(sMemberIds);

        if (jsonArrayMemberId.size() == 0) {
            return listMember;
        }

        return BeanUtils.batchTransform(WeChatRoomMemberDto.class, listByIds(jsonArrayMemberId.toJavaList(Integer.class)));
    }
}
