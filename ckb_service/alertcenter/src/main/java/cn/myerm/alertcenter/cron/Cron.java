package cn.myerm.alertcenter.cron;

import cn.myerm.alertcenter.alert.AlertCenter;
import cn.myerm.alertcenter.alert.RedisQueueConsumer;
import cn.myerm.alertcenter.service.impl.MaterialServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@EnableScheduling
public class Cron {
    private static final Logger logger = LoggerFactory.getLogger(Cron.class);

    private final AlertCenter alertCenter;
    private final RedisQueueConsumer redisQueueConsumer;
    private final MaterialServiceImpl materialService;

    public Cron(AlertCenter alertCenter, RedisQueueConsumer redisQueueConsumer, MaterialServiceImpl materialService) {
        this.alertCenter = alertCenter;
        this.redisQueueConsumer = redisQueueConsumer;
        this.materialService = materialService;
    }

    @Scheduled(cron = "*/30 * * * * ?")
    public void heartBeat() {
        try {
            alertCenter.heartBeat();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "*/20 * * * * ?")
    public void saveToErrorTask() {
        try {
            alertCenter.saveToErrorTask();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "*/10 * * * * ?")
    public void alertHighLevel() {
        alertCenter.alertHighLevel();
    }

    @Scheduled(cron = "0 0 */6 * * ?")
    public void pushToChatroom() {
        alertCenter.pushToChatroom();
    }

    @Scheduled(cron = "10,40 * * * * ?")
    public void consumeAllFromQueue() {
        try {
            redisQueueConsumer.consumeAllFromQueue();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//
//    @Scheduled(cron = "*/2 * * * * ?")
//    public void consumeEvent() {
//        try {
//            redisQueueConsumer.consumeEvent();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Scheduled(cron = "35 * * * * ?")
//    private void generateAiContent() {
//        materialService.generateAiContent();
//    }
}
