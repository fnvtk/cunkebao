package cn.myerm.business.service.impl;

import cn.myerm.business.entity.AccountWechat;
import cn.myerm.business.entity.WeChat;
import cn.myerm.business.mapper.AccountWechatMapper;
import cn.myerm.business.mapper.WeChatMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IAccountWechatService;
import cn.myerm.common.exception.SystemException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AccountWechatServiceImpl extends BusinessServiceImpl<AccountWechatMapper, AccountWechat> implements IAccountWechatService {

    @Resource
    private WeChatMapper weChatMapper;

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = super.getListTableBtn(mapParam);

        for (int i = 0; i < arrBtn.size(); i++) {
            JSONObject jsonObject = arrBtn.getJSONObject(i);
            if (jsonObject.getString("ID").equals("new")) {
                jsonObject.put("sName", "添加");
                jsonObject.put("handler", "handleWechatAppend");
            }
        }

        return arrBtn;
    }

    public void bind(ListParam listParam) {
        JSONArray arrId = JSONArray.parseArray(listParam.getSelectedids());

        List<WeChat> weChats = weChatMapper.selectBatchIds(arrId.toJavaList(Integer.class));
        for (WeChat weChat : weChats) {
            QueryWrapper<AccountWechat> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("AccountId", listParam.getObjectid());
            queryWrapper.eq("WeChatId", weChat.getLId());
            if (count(queryWrapper) > 0) {
                throw new SystemException(weChat.getSName() + "已经绑定，不能重复绑定。");
            }

            AccountWechat accountWechat = new AccountWechat();
            accountWechat.setAccountId(Integer.valueOf(listParam.getObjectid()));
            accountWechat.setWeChatId(weChat.getLId());
            save(accountWechat);
        }
    }

    public List<String> getWechatIds() {
        Integer AccountId = getCurrUser().getAccountId();
        if (AccountId != null) {
            List<String> listWechatId = new ArrayList<>();
            QueryWrapper<AccountWechat> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("AccountId", AccountId);
            List<AccountWechat> accountWechatList = list(queryWrapper);
            for (AccountWechat accountWechat : accountWechatList) {
                listWechatId.add(accountWechat.getWeChatId() + "");
            }

            return listWechatId;
        }

        return null;
    }
}
