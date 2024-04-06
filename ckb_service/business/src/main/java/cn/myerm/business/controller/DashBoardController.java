package cn.myerm.business.controller;

import cn.myerm.business.entity.*;
import cn.myerm.business.service.impl.*;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.impl.SysUserServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/backend/business/dashboard")
public class DashBoardController extends BusinessController {

    private final SysUserServiceImpl sysUserService;
    private final AccountWechatServiceImpl accountWechatService;
    private final WeChatServiceImpl weChatService;
    private final DouYinServiceImpl douYinService;
    private final FriendRequestTaskServiceImpl friendRequestTaskService;
    private final FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService;

    @Autowired
    public DashBoardController(SysUserServiceImpl sysUserService, AccountWechatServiceImpl accountWechatService, WeChatServiceImpl weChatService, DouYinServiceImpl douYinService, FriendRequestTaskServiceImpl friendRequestTaskService, FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService) {
        this.sysUserService = sysUserService;
        this.accountWechatService = accountWechatService;
        this.weChatService = weChatService;
        this.douYinService = douYinService;
        this.friendRequestTaskService = friendRequestTaskService;
        this.friendRequestTaskDetailService = friendRequestTaskDetailService;
    }

    @PostMapping("/stats")
    public MessageDTO stats() {
        SysUser currUser = sysUserService.getCurrUser();

        Map<String, Object> mapParam = new HashMap<>();

        QueryWrapper<SysUser> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("AccountId", currUser.getAccountId());
        objectQueryWrapper.eq("bActive", 1);
        mapParam.put("accountamt", sysUserService.count(objectQueryWrapper));

        QueryWrapper<AccountWechat> accountWechatQueryWrapper = new QueryWrapper<>();
        accountWechatQueryWrapper.eq("AccountId", currUser.getAccountId());
        mapParam.put("wechatamt", accountWechatService.count(accountWechatQueryWrapper));

        QueryWrapper<WeChat> wechatQueryWrapper = new QueryWrapper<>();
        wechatQueryWrapper.inSql("lId", "SELECT WeChatId FROM AccountWechat WHERE AccountId=" + currUser.getAccountId());
        wechatQueryWrapper.eq("bOnline", 1);
        mapParam.put("wechatonlineamt", weChatService.count(wechatQueryWrapper));

        QueryWrapper<DouYin> douYinQueryWrapper = new QueryWrapper<>();
        douYinQueryWrapper.eq("AccountId", currUser.getAccountId());
        mapParam.put("douyinamt", douYinService.count(douYinQueryWrapper));

        QueryWrapper<FriendRequestTask> friendRequestTaskQueryWrapper = new QueryWrapper<>();
        friendRequestTaskQueryWrapper.eq("AccountId", currUser.getAccountId());
        friendRequestTaskQueryWrapper.eq("bEnable", 1);
        mapParam.put("taskamt", friendRequestTaskService.count(friendRequestTaskQueryWrapper));

        QueryWrapper<FriendRequestTaskDetail> friendRequestTaskDetailQueryWrapper = new QueryWrapper<>();
        friendRequestTaskDetailQueryWrapper.inSql("FriendRequestTaskId", "SELECT lId FROM FriendRequestTask WHERE AccountId='" + currUser.getAccountId() + "' AND bEnable=1");
        friendRequestTaskDetailQueryWrapper.eq("StatusId", 0);
        mapParam.put("noexecamt", friendRequestTaskDetailService.count(friendRequestTaskDetailQueryWrapper));

        friendRequestTaskDetailQueryWrapper = new QueryWrapper<>();
        friendRequestTaskDetailQueryWrapper.inSql("FriendRequestTaskId", "SELECT lId FROM FriendRequestTask WHERE AccountId='" + currUser.getAccountId() + "' AND bEnable=1");
        friendRequestTaskDetailQueryWrapper.inSql("StatusId", "2,4");
        mapParam.put("successamt", friendRequestTaskDetailService.count(friendRequestTaskDetailQueryWrapper));

        friendRequestTaskDetailQueryWrapper = new QueryWrapper<>();
        friendRequestTaskDetailQueryWrapper.inSql("FriendRequestTaskId", "SELECT lId FROM FriendRequestTask WHERE AccountId='" + currUser.getAccountId() + "' AND bEnable=1");
        friendRequestTaskDetailQueryWrapper.eq("StatusId", 2);
        mapParam.put("passamt", friendRequestTaskDetailService.count(friendRequestTaskDetailQueryWrapper));

        return success(mapParam);
    }
}
