package cn.myerm.alertcenter.service.impl;

import cn.myerm.alertcenter.entity.EventHandler;
import cn.myerm.alertcenter.mapper.EventHandlerMapper;
import cn.myerm.alertcenter.service.IEventHandlerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EventHandlerServiceImpl extends ServiceImpl<EventHandlerMapper, EventHandler> implements IEventHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(EventHandlerServiceImpl.class);
}
