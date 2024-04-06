package cn.myerm.business.controller;

import cn.binarywang.wx.miniapp.api.WxMaQrcodeService;
import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaCodeLineColor;
import cn.hutool.core.util.StrUtil;
import cn.myerm.business.dto.FriendRequestTaskDto;
import cn.myerm.business.entity.FriendRequestTask;
import cn.myerm.business.param.FriendReqImportParam;
import cn.myerm.business.param.FriendReqTaskParam;
import cn.myerm.business.service.impl.FriendRequestTaskServiceImpl;
import cn.myerm.common.dto.MessageDTO;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@RestController
@RequestMapping("/v1/backend/business/friendrequesttask")
public class FriendRequestTaskController extends BusinessController {

    private final FriendRequestTaskServiceImpl friendrequesttaskService;
    private final WxMaService wxMaService;

    @Autowired
    public FriendRequestTaskController(WxMaService wxService, FriendRequestTaskServiceImpl friendrequesttaskService) {
        this.wxMaService = wxService;
        this.businessService = friendrequesttaskService;
        this.friendrequesttaskService = friendrequesttaskService;
    }

    @PostMapping("/save")
    public MessageDTO save(FriendReqTaskParam friendReqTaskParam) {
        friendrequesttaskService.save(friendReqTaskParam);
        return success();
    }

    @PostMapping("/save/wechat")
    public MessageDTO saveWechat(Integer id, String wechatid) {
        friendrequesttaskService.saveWechat(id, wechatid);
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

    @PostMapping("/stats")
    public MessageDTO stats(Integer id, String time) {
        return success(friendrequesttaskService.stats(id, time));
    }

    @PostMapping("/import")
    public MessageDTO importUpload(@RequestParam("file") MultipartFile file, @RequestParam(name = "objectid", required = true) Integer objectid) {
        if (file.isEmpty()) {
            return fail("上传失败，请选择文件");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("file", friendrequesttaskService.importUpload(file, objectid));

        return success(jsonObject);
    }

    @PostMapping("/importsave")
    public MessageDTO importSave(FriendReqImportParam friendReqImportParam) {
        friendrequesttaskService.importSave(friendReqImportParam);
        return success();
    }

    @PostMapping("/posterqrcode")
    public MessageDTO posterqrcode(Integer id) {
        // 获取小程序二维码生成实例
        WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();

        // 设置小程序码参数
        String scene = String.valueOf(id);// 这里填入你需要的参数或标识
        String page = "pages/poster/index"; // 例如 "pages/index/index"
        int width = 430; // 二维码的宽度
        boolean autoColor = false; // 自动配置线条颜色，如果颜色依然是黑色，则使用二维码的`lineColor`
        WxMaCodeLineColor lineColor = new WxMaCodeLineColor("0", "0", "0"); // RGB颜色值

        try {
            // 调用 createWxaCodeUnlimit 方法生成小程序码
            byte[] qrCodeBytes = wxMaQrcodeService.createWxaCodeUnlimitBytes(scene, page, false, "release", width, autoColor, lineColor, true);
            String base64Encoded = Base64.encodeBase64String(qrCodeBytes);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("qrcode", "data:image/png;base64," + base64Encoded);
            return success(jsonObject);
        } catch (Exception e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/forminputqrcode")
    public MessageDTO forminputqrcode(Integer id) {
        // 获取小程序二维码生成实例
        WxMaQrcodeService wxMaQrcodeService = wxMaService.getQrcodeService();

        // 设置小程序码参数
        String scene = "id=" + String.valueOf(id);// 这里填入你需要的参数或标识
        String page = "pages/form/input"; // 例如 "pages/index/index"
        int width = 430; // 二维码的宽度
        boolean autoColor = false; // 自动配置线条颜色，如果颜色依然是黑色，则使用二维码的`lineColor`
        WxMaCodeLineColor lineColor = new WxMaCodeLineColor("0", "0", "0"); // RGB颜色值

        try {
            // 调用 createWxaCodeUnlimit 方法生成小程序码
            // 默认"release" 要打开的小程序版本。正式版为 "release"，体验版为 "trial"，开发版为 "develop"
            byte[] qrCodeBytes = wxMaQrcodeService.createWxaCodeUnlimitBytes(scene, page, false, "release", width, autoColor, lineColor, true);
            String base64Encoded = Base64.encodeBase64String(qrCodeBytes);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("qrcode", "data:image/png;base64," + base64Encoded);
            return success(jsonObject);
        } catch (Exception e) {
            return fail(e.getMessage());
        }
    }

    @PostMapping("/test")
    public MessageDTO test(FriendReqTaskParam friendReqTaskParam) {
        friendrequesttaskService.test(friendReqTaskParam);
        return success();
    }
}