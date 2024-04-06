package cn.myerm.business.service.impl;

import cn.myerm.business.entity.FriendMessageTask;
import cn.myerm.business.mapper.FriendMessageTaskMapper;
import cn.myerm.business.service.IFriendMessageTaskService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FriendMessageTaskServiceImpl extends BusinessServiceImpl<FriendMessageTaskMapper, FriendMessageTask> implements IFriendMessageTaskService {

    private static final Logger logger = LoggerFactory.getLogger(FriendMessageTaskServiceImpl.class);

}
