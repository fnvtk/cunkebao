package cn.myerm.business.service.impl;

import cn.myerm.business.entity.DownloadVideoSchedule;
import cn.myerm.business.mapper.DownloadVideoScheduleMapper;
import cn.myerm.business.service.IDownloadVideoScheduleService;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DownloadVideoScheduleServiceImpl extends BusinessServiceImpl<DownloadVideoScheduleMapper, DownloadVideoSchedule> implements IDownloadVideoScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadVideoScheduleServiceImpl.class);

}
