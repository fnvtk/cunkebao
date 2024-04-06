package cn.myerm.business.controller;

import cn.myerm.business.dto.FriendMessageTplDto;
import cn.myerm.business.dto.FriendRequestTaskDto;
import cn.myerm.business.entity.FriendMessageTpl;
import cn.myerm.business.param.FriendReqTaskParam;
import cn.myerm.business.service.impl.FriendMessageTplServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/backend/business/friendmessagetpl")
public class FriendMessageTplController extends BusinessController {

    private final FriendMessageTplServiceImpl friendmessagetplService;

    @Autowired
    public FriendMessageTplController(FriendMessageTplServiceImpl friendmessagetplService) {
        this.businessService = friendmessagetplService;
        this.friendmessagetplService = friendmessagetplService;
    }

    @PostMapping("/save")
    public MessageDTO save(String tpl) {
        friendmessagetplService.save(tpl);
        return success();
    }

    @PostMapping("/getbyid")
    public MessageDTO getById(Integer id) {
        FriendMessageTpl messageTpl = friendmessagetplService.getById(id);
        FriendMessageTplDto friendMessageTplDto = BeanUtils.transform(FriendMessageTplDto.class, messageTpl);
        return success(friendMessageTplDto);
    }
}
