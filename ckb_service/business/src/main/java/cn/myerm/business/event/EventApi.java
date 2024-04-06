package cn.myerm.business.event;

import cn.myerm.business.param.EventParam;
import cn.myerm.business.service.impl.EventServiceImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class EventApi {

    private static final Logger logger = LoggerFactory.getLogger(EventApi.class);

    private final RedisTemplate<String, String> redisTemplate;

    private final EventServiceImpl eventService;

    @Value("${spring.redis.eventkey}")
    private String eventkey;

    @Autowired
    public EventApi(RedisTemplate<String, String> redisTemplate, EventServiceImpl eventService) {
        this.redisTemplate = redisTemplate;
        this.eventService = eventService;
    }

    /**
     * 触发事件
     */
    public void triggle(EventParam eventParam) {
        redisTemplate.opsForList().rightPush("ckbevent", JSON.toJSONString(eventParam));
    }

    /**
     * 事件处理
     * @throws IOException
     */
    public void consumeEvent() throws IOException {
        ListOperations<String, String> listOps = redisTemplate.opsForList();

        Long size = 0L;
        try {
            size = listOps.size(eventkey);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return;
        }

        if (size > 0) {
            List<String> dataList = listOps.range(eventkey, 0, size - 1);
            for (String data : dataList) {
                final JSONObject jsonObject = JSONObject.parseObject(data);
                eventService.newSave(jsonObject);
                listOps.trim(eventkey, 1, -1);
            }
        }
    }
}
