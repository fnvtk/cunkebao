package cn.myerm.business.service.impl;

import cn.myerm.business.entity.Event;
import cn.myerm.business.entity.EventHandler;
import cn.myerm.business.entity.EventTrigger;
import cn.myerm.business.mapper.EventMapper;
import cn.myerm.business.service.IEventService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EventServiceImpl extends ServiceImpl<EventMapper, Event> implements IEventService {
    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventTriggerServiceImpl eventTriggerService;
    private final EventHandlerServiceImpl eventHandlerService;

    public EventServiceImpl(EventTriggerServiceImpl eventTriggerService, EventHandlerServiceImpl eventHandlerService) {
        this.eventTriggerService = eventTriggerService;
        this.eventHandlerService = eventHandlerService;
    }

    public void newSave(JSONObject jsonObject) {
        String eventkey = jsonObject.getString("event");

        Event event = new Event();
        event.setSName(jsonObject.getString("name"));
        event.setDNewTime(LocalDateTime.now());
        event.setSSourcePath(jsonObject.getString("path"));
        event.setJParam(jsonObject.getString("param"));
        event.setEventTriggerId(eventkey);
        save(event);

        EventTrigger eventTrigger = eventTriggerService.getById(eventkey);
        if (eventTrigger != null) {
            JSONArray arrHandler = JSONArray.parseArray(eventTrigger.getJEventHandler());
            for (int i = 0; i < arrHandler.size(); i++) {
                JSONObject handler = arrHandler.getJSONObject(i);

                EventHandler eventHandler = new EventHandler();
                eventHandler.setEventId(event.getLId());
                eventHandler.setEventHandlerId(handler.getString("id"));
                eventHandler.setStatusId(0);
                eventHandlerService.save(eventHandler);
            }
        }
    }
}
