package cn.myerm.business.controller;

import cn.hutool.core.util.StrUtil;
import cn.myerm.business.collect.LongXiaGame;
import cn.myerm.business.entity.FriendRequestTask;
import cn.myerm.business.entity.FriendRequestTaskDetail;
import cn.myerm.business.entity.MaterialLib;
import cn.myerm.business.param.WechatFriendLabelParam;
import cn.myerm.business.service.impl.*;
import cn.myerm.common.dto.MessageDTO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/backend/business/fix")
public class FixController extends BusinessController {

    private static final Logger logger = LoggerFactory.getLogger(FixController.class);

    private final LongXiaGame longXiaGame;

    private final MaterialPushLogServiceImpl materialPushLogService;

    private final MaterialPushChatroomTaskServiceImpl materialPushChatroomTaskService;
    private final MaterialPushMomentTaskServiceImpl materialPushMomentTaskService;
    private final FriendRequestTaskServiceImpl friendRequestTaskService;
    private final FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService;
    private final WeChatFriendLabelServiceImpl weChatFriendLabelService;
    private final MaterialLibServiceImpl materialLibService;
    private final MaterialServiceImpl materialService;

    @Autowired
    public FixController(LongXiaGame longXiaGame, MaterialPushLogServiceImpl materialPushLogService,
                         MaterialPushChatroomTaskServiceImpl materialPushChatroomTaskService,
                         MaterialPushMomentTaskServiceImpl materialPushMomentTaskService, FriendRequestTaskServiceImpl friendRequestTaskService, FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService, WeChatFriendLabelServiceImpl weChatFriendLabelService, MaterialLibServiceImpl materialLibService, MaterialServiceImpl materialService) {
        this.longXiaGame = longXiaGame;
        this.materialPushLogService = materialPushLogService;
        this.materialPushChatroomTaskService = materialPushChatroomTaskService;
        this.materialPushMomentTaskService = materialPushMomentTaskService;
        this.friendRequestTaskService = friendRequestTaskService;
        this.friendRequestTaskDetailService = friendRequestTaskDetailService;
        this.weChatFriendLabelService = weChatFriendLabelService;
        this.materialLibService = materialLibService;
        this.materialService = materialService;
    }

    @PostMapping("/longxia")
    public MessageDTO longxia() {
        longXiaGame.fix();
        return success();
    }

    @PostMapping("/task/status")
    public MessageDTO updateTaskStatus() {
        List<FriendRequestTask> friendRequestTasks = friendRequestTaskService.list();
        for (FriendRequestTask friendRequestTask : friendRequestTasks) {
            friendRequestTaskService.updateStatus(friendRequestTask.getLId());
        }

        return success();
    }


    @PostMapping("/task/fix")
    public MessageDTO fixreqconfig() {
        QueryWrapper<FriendRequestTask> queryWrapper = new QueryWrapper<>();
        List<FriendRequestTask> list = friendRequestTaskService.list(queryWrapper);
        for (FriendRequestTask friendRequestTask : list) {
            JSONArray jsonArray = JSONArray.parseArray(friendRequestTask.getJTask());
            for (int i=0; i<jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.put("id", UUID.randomUUID());
            }
            friendRequestTask.setJTask(jsonArray.toJSONString());
            friendRequestTaskService.updateById(friendRequestTask);

            friendRequestTaskService.updateConfig(friendRequestTask.getLId());
        }

        return success();
    }

    @PostMapping("/task/fixtpl")
    public MessageDTO fixTpl() {

        QueryWrapper<FriendRequestTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.like("jTask", "嗯嗯，明白了");

        List<FriendRequestTask> list = friendRequestTaskService.list(queryWrapper);
        for (FriendRequestTask friendRequestTask : list) {
            JSONArray jsonArray = JSONArray.parseArray(friendRequestTask.getJTask());
            for (int i = jsonArray.size() - 1; i > -1; i--) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                boolean t = false;
                JSONArray jMessage = jsonObject.getJSONArray("jMessage");
                for (int j = jMessage.size() - 1; j > -1; j--) {
                    String content = jMessage.getJSONObject(j).getString("content");
                    if (StrUtil.isNotEmpty(content)) {
                        if (content.equals("嗯嗯，明白了")) {
                            jMessage.remove(j);
                            t = true;
                        } else if (content.contains("不知道现在您还有在听樊登读书吗")) {
                            jMessage.remove(j);
                        }
                    }
                }
            }

            friendRequestTask.setJTask(jsonArray.toJSONString());
        }
        friendRequestTaskService.updateBatchById(list);


        return success();
    }

    @PostMapping("/lable/fix")
    public MessageDTO fixLabel() {
        QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("StatusId", 6);
        List<FriendRequestTaskDetail> details = friendRequestTaskDetailService.list(queryWrapper);
        for (FriendRequestTaskDetail detail : details) {
            WechatFriendLabelParam wechatFriendLabelParam = new WechatFriendLabelParam();
            wechatFriendLabelParam.setFriendRequestTaskId(detail.getFriendRequestTaskId());
            wechatFriendLabelParam.setFriendRequestTaskDetailId(detail.getLId());
            wechatFriendLabelParam.setWeChatFriendId(detail.getWeChatFriendId());
            wechatFriendLabelParam.setLabelId(1);
            wechatFriendLabelParam.setSValue("second");
            weChatFriendLabelService.markLabel(wechatFriendLabelParam);
        }

        return success();
    }

    @PostMapping("/lib/fix")
    public MessageDTO libfix() {
        for (MaterialLib materialLib : materialLibService.list()) {
            materialService.updateAmt(materialLib.getLId());
        }

        return success();
    }
}
