package cn.myerm.business.frontend;

import cn.hutool.core.util.StrUtil;
import cn.myerm.business.controller.BusinessController;
import cn.myerm.business.dto.FriendRequestTaskDto;
import cn.myerm.business.entity.FriendRequestTask;
import cn.myerm.business.entity.FriendRequestTaskDetail;
import cn.myerm.business.param.FriendReqImportParam;
import cn.myerm.business.service.impl.FriendRequestTaskDetailServiceImpl;
import cn.myerm.business.service.impl.FriendRequestTaskServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import cn.myerm.system.entity.SysUser;
import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/frontend/business/form")
public class TaskFormController extends BusinessController {
    private final FriendRequestTaskServiceImpl friendrequesttaskService;
    private final FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService;

    public TaskFormController(FriendRequestTaskServiceImpl friendrequesttaskService, FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService) {
        this.friendrequesttaskService = friendrequesttaskService;
        this.friendRequestTaskDetailService = friendRequestTaskDetailService;
    }

    @PostMapping("/listdata")
    public MessageDTO listData(Integer page, Integer limit) {
        SysUser currUser = friendRequestTaskDetailService.getCurrUser();
        QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("NewUserId", currUser.getLID());
        queryWrapper.orderByDesc("dNewTime");
        queryWrapper.last(" LIMIT " + limit + " OFFSET " + (page - 1) * limit);
        friendRequestTaskDetailService.list(queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", friendRequestTaskDetailService.list(queryWrapper));
        return success(result);
    }

    @PostMapping("/importsave")
    public MessageDTO importSave(FriendReqImportParam friendReqImportParam) {
        friendrequesttaskService.importSave(friendReqImportParam);
        return success();
    }

    @PostMapping("/getbyid")
    public MessageDTO getById(Integer id) {
        FriendRequestTask friendRequestTask = friendrequesttaskService.getById(id);
        FriendRequestTaskDto friendRequestTaskDto = BeanUtils.transform(FriendRequestTaskDto.class, friendRequestTask);
        friendRequestTaskDto.setArrTask(JSONArray.parseArray(friendRequestTask.getJTask()));
        friendRequestTaskDto.setArrWechatId(StrUtil.splitTrim(friendRequestTask.getWeChatId(), ";"));
        friendRequestTaskDto.setMemoParamId(Arrays.asList(StrUtil.split(friendRequestTask.getMemoParamId(), ",")));
        return success(friendRequestTaskDto);
    }
}
