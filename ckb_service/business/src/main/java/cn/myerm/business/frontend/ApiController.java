package cn.myerm.business.frontend;

import cn.hutool.core.util.StrUtil;
import cn.myerm.business.entity.FriendRequestTask;
import cn.myerm.business.entity.WeChatFriendLabelLib;
import cn.myerm.business.service.impl.FriendRequestTaskServiceImpl;
import cn.myerm.business.service.impl.WeChatFriendLabelLibServiceImpl;
import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;

@RestController
@RequestMapping("/v1/frontend/business/api")
public class ApiController extends CommonController {

    private final FriendRequestTaskServiceImpl friendrequesttaskService;

    private final WeChatFriendLabelLibServiceImpl weChatFriendLabelLibService;

    @Autowired
    public ApiController(FriendRequestTaskServiceImpl friendrequesttaskService, WeChatFriendLabelLibServiceImpl weChatFriendLabelLibService) {
        this.friendrequesttaskService = friendrequesttaskService;
        this.weChatFriendLabelLibService = weChatFriendLabelLibService;
    }

    @PostMapping("/lkdiecom/getphone")
    public MessageDTO getPhone(@RequestParam(required = true) Integer id,
                               @RequestParam(required = true) String mobile,
                               @RequestParam(required = false) String labels) {

        String[] arrLable = labels.split(",");

        HashMap<Integer, String> mapLabel = new HashMap<>();
        mapLabel.put(6, arrLable[0]);
        mapLabel.put(7, arrLable[1]);
        mapLabel.put(8, arrLable[2]);

        friendrequesttaskService.savePhone(id, mobile, mapLabel);

        return success();
    }

    @PostMapping("/wwwquwanzhicom/getphone")
    public MessageDTO getPhone2(@RequestParam(required = true) String key,
                                @RequestParam(required = true) String data) {

        JSONObject jData = JSONObject.parseObject(data);
        JSONObject jClass = jData.getJSONObject("class");

        HashMap<Integer, String> mapLabel = new HashMap<>();

        FriendRequestTask friendRequestTask = friendrequesttaskService.getByKey(key);
        if (friendRequestTask == null) {//不存在，创建一个新的计划
            friendRequestTask = friendrequesttaskService.getById(64);//取一个作为模板

            friendRequestTask.setLId(null);
            friendRequestTask.setSName("网站_" + jClass.getString("name"));
            friendRequestTask.setDNewTime(LocalDateTime.now());
            friendRequestTask.setDEditTime(LocalDateTime.now());

            if (jClass.getString("name").contains("魔兽")) {
                friendRequestTask.setWeChatId(";2590938;");
            } else if (jClass.getString("name").contains("暗黑")) {
                friendRequestTask.setWeChatId(";300745;");
            } else {
                friendRequestTask.setWeChatId(";13247083;");
            }

            friendRequestTask.setSKey("wwwquwanzhicom_" + jClass.getString("cid"));
            friendRequestTask.setSHello(jClass.getString("name"));
            friendRequestTask.setSTheme(jClass.getString("name"));
            friendRequestTask.setJTask(friendRequestTask.getJTask().replace("[替换]", jClass.getString("name")));
            friendrequesttaskService.save(friendRequestTask);

            WeChatFriendLabelLib weChatFriendLabelLib = new WeChatFriendLabelLib();
            weChatFriendLabelLib.setSName("输入1");
            weChatFriendLabelLib.setFriendRequestTaskId(friendRequestTask.getLId());
            weChatFriendLabelLib.setBLeaf(1);
            weChatFriendLabelLib.setBSys(0);
            weChatFriendLabelLib.setValueTypeId("Text");
            weChatFriendLabelLibService.save(weChatFriendLabelLib);

            weChatFriendLabelLib = new WeChatFriendLabelLib();
            weChatFriendLabelLib.setSName("输入2");
            weChatFriendLabelLib.setFriendRequestTaskId(friendRequestTask.getLId());
            weChatFriendLabelLib.setBLeaf(1);
            weChatFriendLabelLib.setBSys(0);
            weChatFriendLabelLib.setValueTypeId("Text");
            weChatFriendLabelLibService.save(weChatFriendLabelLib);

            weChatFriendLabelLib = new WeChatFriendLabelLib();
            weChatFriendLabelLib.setSName("金额");
            weChatFriendLabelLib.setFriendRequestTaskId(friendRequestTask.getLId());
            weChatFriendLabelLib.setBLeaf(1);
            weChatFriendLabelLib.setBSys(0);
            weChatFriendLabelLib.setValueTypeId("Num");
            weChatFriendLabelLibService.save(weChatFriendLabelLib);
        }

        if (StrUtil.isNotEmpty(jData.getString("input2"))) {
            QueryWrapper<WeChatFriendLabelLib> objectQueryWrapper = new QueryWrapper<>();
            objectQueryWrapper.eq("FriendRequestTaskId", friendRequestTask.getLId());
            objectQueryWrapper.eq("sName", "输入1");
            objectQueryWrapper.last("LIMIT 1");
            WeChatFriendLabelLib labelLib = weChatFriendLabelLibService.getOne(objectQueryWrapper);
            mapLabel.put(labelLib.getLId(), jData.getString("input1"));

            objectQueryWrapper = new QueryWrapper<>();
            objectQueryWrapper.eq("FriendRequestTaskId", friendRequestTask.getLId());
            objectQueryWrapper.eq("sName", "输入2");
            objectQueryWrapper.last("LIMIT 1");
            labelLib = weChatFriendLabelLibService.getOne(objectQueryWrapper);
            mapLabel.put(labelLib.getLId(), jData.getString("input2"));

            objectQueryWrapper = new QueryWrapper<>();
            objectQueryWrapper.eq("FriendRequestTaskId", friendRequestTask.getLId());
            objectQueryWrapper.eq("sName", "金额");
            objectQueryWrapper.last("LIMIT 1");
            labelLib = weChatFriendLabelLibService.getOne(objectQueryWrapper);
            mapLabel.put(labelLib.getLId(), jData.getString("money"));

            friendrequesttaskService.savePhone(friendRequestTask.getLId(), jData.getString("input2"), mapLabel);
        }

        friendrequesttaskService.updateStatus(friendRequestTask.getLId());

        return success();
    }
}
