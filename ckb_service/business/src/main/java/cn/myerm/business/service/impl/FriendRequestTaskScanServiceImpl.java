package cn.myerm.business.service.impl;

import cn.myerm.business.entity.FriendRequestTaskScan;
import cn.myerm.business.mapper.FriendRequestTaskScanMapper;
import cn.myerm.business.service.IFriendRequestTaskScanService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FriendRequestTaskScanServiceImpl extends BusinessServiceImpl<FriendRequestTaskScanMapper, FriendRequestTaskScan> implements IFriendRequestTaskScanService {

    private static final Logger logger = LoggerFactory.getLogger(FriendRequestTaskScanServiceImpl.class);

}
