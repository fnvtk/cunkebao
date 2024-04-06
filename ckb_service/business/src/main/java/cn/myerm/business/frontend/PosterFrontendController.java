package cn.myerm.business.frontend;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.myerm.business.dto.FriendRequestTaskDto;
import cn.myerm.business.dto.PosterDto;
import cn.myerm.business.entity.FriendRequestTask;
import cn.myerm.business.service.impl.FriendRequestTaskServiceImpl;
import cn.myerm.business.service.impl.PosterServiceImpl;
import cn.myerm.common.controller.CommonController;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONObject;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/frontend/business/poster")
public class PosterFrontendController extends CommonController {

    private final PosterServiceImpl posterService;
    private final FriendRequestTaskServiceImpl friendrequesttaskService;
    private final WxMaService wxService;

    @Autowired
    public PosterFrontendController(PosterServiceImpl posterService, FriendRequestTaskServiceImpl friendrequesttaskService, WxMaService wxService) {
        this.posterService = posterService;
        this.friendrequesttaskService = friendrequesttaskService;
        this.wxService = wxService;
    }

    @PostMapping("/getone")
    public MessageDTO getOne(Integer id) {
        FriendRequestTask friendRequestTask = friendrequesttaskService.getById(id);
        friendrequesttaskService.scanCount(id);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("poster", BeanUtils.transform(PosterDto.class, posterService.getById(friendRequestTask.getPosterId())));
        jsonObject.put("task", BeanUtils.transform(FriendRequestTaskDto.class, friendRequestTask));
        return success(jsonObject);
    }

    @PostMapping("/decryptphone")
    public MessageDTO decryptphone(Integer id, String code) {
        try {
            WxMaPhoneNumberInfo phoneNoInfo = wxService.getUserService().getPhoneNoInfo(code);
            friendrequesttaskService.savePhoneFromFrontend(id, phoneNoInfo.getPhoneNumber());
            return success();
        } catch (WxErrorException e) {
            return fail(e.getMessage());
        }
    }
}
