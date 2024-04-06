package cn.myerm.business.event;

import cn.myerm.business.entity.Event;
import cn.myerm.business.service.impl.*;
import cn.myerm.common.exception.SystemException;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class EventHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);

    @Resource
    private EventHandlerServiceImpl eventHandlerService;

    @Resource
    private EventServiceImpl eventService;

    @Resource
    private MaterialLibServiceImpl materialLibService;

    @Resource
    private MaterialServiceImpl materialService;

    @Resource
    private FriendRequestTaskServiceImpl friendRequestTaskService;

    /**
     * 运行事件的响应
     */
    public synchronized void run() {
        QueryWrapper<cn.myerm.business.entity.EventHandler> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("StatusId", "0");
        List<cn.myerm.business.entity.EventHandler> eventHandlerList = eventHandlerService.list(queryWrapper);
        for (cn.myerm.business.entity.EventHandler eventHandler : eventHandlerList) {
            logger.info("响应事件：" + eventHandler.getEventHandlerId());

            Event event = eventService.getById(eventHandler.getEventId());

            JSONObject param = JSONObject.parseObject(event.getJParam());

            try {
                switch (eventHandler.getEventHandlerId()) {
                    case "CollectCfgPush":
                        materialLibService.push();
                        break;
                    case "MaterialFliterByKeyword":
                        materialService.fliterByKeyword(param.getIntValue("id"));
                        break;
                    case "UpdateFriendRequestTaskStatus":
                        friendRequestTaskService.updateStatus(param.getIntValue("taskid"));
                        break;
                    case "UpdateFriendRequestTaskConfig":
                        friendRequestTaskService.updateConfig(param.getIntValue("taskid"));
                        break;
                    case "handleAddFriendReqPass":
                        friendRequestTaskService.handleAddFriendReqPass(param);
                        break;
                    case "MarkFriendLabel":
                        friendRequestTaskService.handleMarkLabel(param);
                        break;
                    case "handleUserIncome":
                        friendRequestTaskService.handleUserIncome(param);
                        break;
                }

                eventHandler.setDDoneTime(LocalDateTime.now());
                eventHandler.setStatusId(2);
                eventHandlerService.updateById(eventHandler);
            } catch (Exception e) {//这里捕捉异常的目的是，不影响其他事件的执行
                logger.error(e.getMessage());

                eventHandler.setDDoneTime(LocalDateTime.now());
                eventHandler.setStatusId(-1);
                eventHandler.setSFailReason(e.getMessage());
                eventHandlerService.updateById(eventHandler);
            }
        }
    }
}
