package cn.myerm.business.service.impl;

import cn.myerm.business.entity.FriendRequestConfig;
import cn.myerm.business.mapper.FriendRequestConfigMapper;
import cn.myerm.business.service.IFriendRequestConfigService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FriendRequestConfigServiceImpl extends BusinessServiceImpl<FriendRequestConfigMapper, FriendRequestConfig> implements IFriendRequestConfigService {

    private static final Logger logger = LoggerFactory.getLogger(FriendRequestConfigServiceImpl.class);

}
