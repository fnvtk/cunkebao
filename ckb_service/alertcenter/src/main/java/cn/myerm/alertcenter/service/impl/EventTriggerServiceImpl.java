package cn.myerm.alertcenter.service.impl;

import cn.myerm.alertcenter.entity.EventTrigger;
import cn.myerm.alertcenter.mapper.EventTriggerMapper;
import cn.myerm.alertcenter.service.IEventTriggerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventTriggerServiceImpl extends ServiceImpl<EventTriggerMapper, EventTrigger> implements IEventTriggerService {
    private static final Logger logger = LoggerFactory.getLogger(EventTriggerServiceImpl.class);
}
