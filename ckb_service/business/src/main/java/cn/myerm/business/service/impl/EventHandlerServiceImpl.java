package cn.myerm.business.service.impl;

import cn.myerm.business.entity.EventHandler;
import cn.myerm.business.mapper.EventHandlerMapper;
import cn.myerm.business.service.IEventHandlerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventHandlerServiceImpl extends ServiceImpl<EventHandlerMapper, EventHandler> implements IEventHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(EventHandlerServiceImpl.class);
}
