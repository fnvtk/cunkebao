package cn.myerm.business.service.impl;

import cn.myerm.business.entity.FriendMessageTpl;
import cn.myerm.business.mapper.FriendMessageTplMapper;
import cn.myerm.business.service.IFriendMessageTplService;
import cn.myerm.system.entity.SysUser;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Service
public class FriendMessageTplServiceImpl extends BusinessServiceImpl<FriendMessageTplMapper, FriendMessageTpl> implements IFriendMessageTplService {

    private static final Logger logger = LoggerFactory.getLogger(FriendMessageTplServiceImpl.class);

    public void save(String tpl) {
        SysUser currUser = getCurrUser();

        JSONObject jsonObject = JSONObject.parseObject(tpl);

        FriendMessageTpl friendMessageTpl = new FriendMessageTpl();
        friendMessageTpl.setAccountId(currUser.getAccountId());
        friendMessageTpl.setDNewTime(LocalDateTime.now());
        friendMessageTpl.setSName(jsonObject.getString("sName"));
        friendMessageTpl.setNewUserId(currUser.getLID());
        friendMessageTpl.setJMessage(tpl);
        save(friendMessageTpl);
    }
}
