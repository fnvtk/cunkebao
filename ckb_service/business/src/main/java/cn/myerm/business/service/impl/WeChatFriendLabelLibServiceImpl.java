package cn.myerm.business.service.impl;

import cn.myerm.business.entity.WeChatFriendLabelLib;
import cn.myerm.business.mapper.WeChatFriendLabelLibMapper;
import cn.myerm.business.service.IWeChatFriendLabelLibService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeChatFriendLabelLibServiceImpl extends BusinessServiceImpl<WeChatFriendLabelLibMapper, WeChatFriendLabelLib> implements IWeChatFriendLabelLibService {
    private static final Logger logger = LoggerFactory.getLogger(WeChatFriendLabelLibServiceImpl.class);

    public List<WeChatFriendLabelLib> getAllTag(Integer FriendRequestTaskId) {
        QueryWrapper<WeChatFriendLabelLib> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bSys", 1);
        List<WeChatFriendLabelLib> labelLibList = list(queryWrapper);

        if (FriendRequestTaskId != null && FriendRequestTaskId > 0) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("FriendRequestTaskId", FriendRequestTaskId);
            labelLibList.addAll(list(queryWrapper));
        }

        return labelLibList;
    }
}
