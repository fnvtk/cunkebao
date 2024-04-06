package cn.myerm.business.cron;

import cn.myerm.business.collect.LongXiaGame;
import cn.myerm.business.event.EventApi;
import cn.myerm.business.event.EventHandler;
import cn.myerm.business.service.impl.*;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Component
@EnableScheduling
public class Cron {

    private static final Logger logger = LoggerFactory.getLogger(Cron.class);
    private final Environment environment;
    private final WeChatServiceImpl weChatService;

    private final WeChatFriendServiceImpl weChatFriendService;

    private final WeChatRoomServiceImpl weChatRoomService;

    private final CacheServiceImpl cacheService;

    private final MaterialServiceImpl materialService;

    private final SysAttachServiceImpl sysAttachService;

    private final MaterialPushMomentTaskServiceImpl materialPushMomentTaskService;
    private final MaterialPushChatroomTaskServiceImpl materialPushChatroomTaskService;

    private final MaterialPushLogServiceImpl materialPushLogService;

    private final DouYinServiceImpl douYinService;

    private final LongXiaGame longXiaGame;

    private final EventHandler eventHandler;

    private final FriendRequestTaskServiceImpl friendRequestTaskService;

    private final JdUnionService jdUnionService;

    private final EventApi eventApi;

    @Autowired
    public Cron(Environment environment, WeChatServiceImpl weChatService, WeChatFriendServiceImpl weChatFriendService,
                WeChatRoomServiceImpl weChatRoomService, CacheServiceImpl cacheService,
                MaterialServiceImpl materialService, SysAttachServiceImpl sysAttachService,
                MaterialPushMomentTaskServiceImpl materialPushMomentTaskService,
                MaterialPushChatroomTaskServiceImpl materialPushChatroomTaskService,
                MaterialPushLogServiceImpl materialPushLogService, DouYinServiceImpl douYinService, LongXiaGame longXiaGame, EventHandler eventHandler, FriendRequestTaskServiceImpl friendRequestTaskService, JdUnionService jdUnionService, EventApi eventApi) {
        this.environment = environment;
        this.weChatService = weChatService;
        this.weChatFriendService = weChatFriendService;
        this.weChatRoomService = weChatRoomService;
        this.cacheService = cacheService;
        this.materialService = materialService;
        this.sysAttachService = sysAttachService;
        this.materialPushMomentTaskService = materialPushMomentTaskService;
        this.materialPushChatroomTaskService = materialPushChatroomTaskService;
        this.materialPushLogService = materialPushLogService;
        this.douYinService = douYinService;
        this.longXiaGame = longXiaGame;
        this.eventHandler = eventHandler;
        this.friendRequestTaskService = friendRequestTaskService;
        this.jdUnionService = jdUnionService;
        this.eventApi = eventApi;
    }

    /**
     * 同步云端API的微信朋友数据到Cache
     */
    @Scheduled(cron = "*/30 * * * * ?")
    private void syncWechatFriend() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            String sync = cacheService.get("syncwechatfriend");
            if (sync != null && sync.equals("1")) {
                cacheService.set("syncwechatfriend", "2");//执行中

                weChatFriendService.sync();

                cacheService.set("syncwechatfriend", "0");//执行完毕
                cacheService.set("savewechatfriend", "1");//保存到数据库
            }
        }
    }

    /**
     * 从私域系统同步微信列表到本地
     */
    @Scheduled(cron = "17 * * * * ?")
    private void syncWechatList() {
        weChatService.execSync();
    }

    /**
     * 把Cache中的数据导入到微信好友数据库
     */
    @Scheduled(cron = "6 * * * * ?")
    private void saveWechatFriend() {
        String sync = cacheService.get("savewechatfriend");
        if (sync != null && sync.equals("1")) {
            cacheService.set("savewechatfriend", "2");//保存中
            weChatFriendService.saveSync();
            cacheService.set("savewechatfriend", "0");//执行完毕
        }
    }

    /**
     * 同步微信群数据
     */
    @Scheduled(cron = "* */5 * * * ?")
    private void syncWechatroom() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            weChatRoomService.sync();
        }
    }

    /**
     * 从私域系统那边同步朋友圈到存客宝
     */
    @Scheduled(cron = "28 * * * * ?")
    private void pullFromMoment() {
        materialService.pullFromMoment();
    }

    /**
     * 从私域系统那边拉取群消息（实时）
     */
    @Scheduled(cron = "*/30 * * * * ?")
    private void pullFromChatroomMessage() {
        String sync = cacheService.get("pushchatroomImmediately");
        if (sync != null && sync.equals("1")) {
            materialService.pullChatroomMessage();
        }
    }

    /**
     * 把本地的资源文件，上传oss
     */
    @Scheduled(cron = "38 * * * * ?")
    private void putOss() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            sysAttachService.putOss();
        }
    }

    /**
     * 下载视频
     */
    @Scheduled(cron = "34,4 * * * * ?")
    private void downloadVideo() {
        materialService.downloadVideo();
    }

    /**
     * 推送任务重新排程
     */
    @Scheduled(cron = "0 0 0 * * ?")
    private void execSchedule() {
        materialPushMomentTaskService.execSchedule();
        materialPushChatroomTaskService.execSchedule();
    }

    /**
     * 执行朋友圈推送任务到发送队列
     */
    @Scheduled(cron = "35 * * * * ?")
    private void execMomentPush() {
        materialPushLogService.execPushToLog(materialPushMomentTaskService, false);
    }

    /**
     * 执行群消息推送任务到发送队列（定时）
     */
    @Scheduled(cron = "*/30 * * * * ?")
    private void execChatroomPush() {
        materialPushLogService.execPushToLog(materialPushChatroomTaskService, false);
    }

    /**
     * 执行群消息推送任务到发送队列（实时）
     */
    @Scheduled(cron = "*/2 * * * * ?")
    private void execChatroomPushImmediately() {
        String sync = cacheService.get("pushchatroomImmediately");
        if (sync != null && sync.equals("1")) {
            materialPushLogService.execPushToLog(materialPushChatroomTaskService, true);
        }
    }

    /**
     * 发布朋友圈
     */
    @Scheduled(cron = "36 * * * * ?")
    private void publishMoment() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            materialPushLogService.publishMoment();
        }
    }

    /**
     * 推送到群
     */
    @Scheduled(cron = "*/2 * * * * ?")
    private void pushToChatroom() {
        materialPushLogService.pushToChatroom();
    }

    @Scheduled(cron = "38 * * * * ?")
    private void pushMomentList() {
        materialPushLogService.publishMomentList();
    }

    /**
     * 采集龙虾网
     */
    @Scheduled(cron = "* * 12,18,23 * * ?")
    private void longxiagame() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            longXiaGame.execCollect();
        }
    }

    /**
     * 删除已下架的商品
     */
    @Scheduled(cron = "* * */2 * * ?")
    private void longxiagameonsale() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("prod")) {
            longXiaGame.checkOnSale();
        }
    }

    /**
     * 同步抖音账号
     */
    @Scheduled(cron = "23 * * * * ?")
    private void syncDouyin() {
        douYinService.syncMerchantList();
    }

    /**
     * 事件响应
     */
    @Scheduled(cron = "*/3 * * * * ?")
    private void eventHandle() {
        eventHandler.run();
    }

    /**
     * 每分钟执行一次加友计划
     */
    @Scheduled(cron = "9 * * * * ?")
    private void execFriendReq() {
        String enable = cacheService.get("friendreqtaskenable");
        if (enable != null && enable.equals("1")) {
            friendRequestTaskService.execReq();
        }
    }

    /**
     * 更新好友申请的状态
     */
    @Scheduled(cron = "51 * * * * ?")
    private void updateTaskRemoteStatus() {
        String enable = cacheService.get("friendreqtaskenable");
        if (enable != null && enable.equals("1")) {
            friendRequestTaskService.updateTaskRemoteStatus();
        }
    }

    /**
     * 检查是否通过好友
     */
    @Scheduled(cron = "57 * * * * ?")
    private void checkIsPass() {
        String enable = cacheService.get("friendreqtaskenable");
        if (enable != null && enable.equals("1")) {
            friendRequestTaskService.checkIsPass();
        }
    }

    /**
     * 发送消息给好友
     */
    @Scheduled(cron = "*/2 * * * * ?")
    private void execSendMessageToFriend() {
        String enable = cacheService.get("friendreqtaskenable");
        if (enable != null && enable.equals("1")) {
            friendRequestTaskService.sendMessage();
        }
    }

    /**
     * 打好友交互频次标签
     */
    @Scheduled(cron = "*/30 * * * * ?")
    private void markFriendCommunicate() {
        friendRequestTaskService.markFriendCommunicate();
    }

    /**
     * 根据计划主动发送消息给好友
     */
    @Scheduled(cron = "26 * * * * ?")
    private void sendMessageToFriend() {
        friendRequestTaskService.sendMessageToFriend();
    }

    @Scheduled(cron = "*/2 * * * * ?")
    public void consumeEvent() {
        try {
            eventApi.consumeEvent();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
