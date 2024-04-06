package cn.myerm.business.service.impl;

import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.entity.WeChat;
import cn.myerm.business.mapper.WeChatMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IWeChatService;
import cn.myerm.common.exception.SystemException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@EnableScheduling
public class WeChatServiceImpl extends BusinessServiceImpl<WeChatMapper, WeChat> implements IWeChatService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatServiceImpl.class);

    private final CustomerTouchApi customerTouchApi;

    private final CacheServiceImpl cacheService;

    private final AccountWechatServiceImpl accountWechatService;

    @Autowired
    public WeChatServiceImpl(CustomerTouchApi customerTouchApi, CacheServiceImpl cacheService, AccountWechatServiceImpl accountWechatService) {
        this.customerTouchApi = customerTouchApi;
        this.cacheService = cacheService;
        this.accountWechatService = accountWechatService;
    }

    public String appendWhereSql(ListParam listParam) {
        if (getCurrUser().getSysRoleId() > 1) {
            List<String> wechatIds = accountWechatService.getWechatIds();
            wechatIds.add("0");
            return super.appendWhereSql(listParam) + " AND lId IN (" + String.join(", ", wechatIds) + ")";
        }

        return super.appendWhereSql(listParam);
    }

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();
        if (mapParam.get("ListId").equals("1544")) {
            btn = new JSONObject();
            btn.put("ID", "select");
            btn.put("sName", "批量绑定");
            btn.put("handler", "handleBatchBindWechat");
            btn.put("icon", "el-icon-check");
            arrBtn.add(btn);
        } else {
            btn.put("ID", "sync");
            btn.put("sName", "同步微信账号");
            btn.put("handler", "handleSyncWechat");
            btn.put("icon", "el-icon-refresh");
            arrBtn.add(btn);
        }

        arrBtn.addAll(super.getListTableBtn(mapParam));

        return arrBtn;
    }

    /**
     * 从私域系统同步微信列表到本地
     */
    public synchronized void execSync() {
        String sync = cacheService.get("syncwechatlist");
        if (sync != null && sync.equals("1")) {
            logger.info("刷新微信账号列表");
            JSONArray arrWechat = customerTouchApi.getWechatAccountList();
            for (int i = 0; i < arrWechat.size(); i++) {
                final JSONObject wechatObj = arrWechat.getJSONObject(i);

                WeChat wechat = getById(wechatObj.getIntValue("id"));
                if (wechat == null) {
                    wechat = new WeChat();
                }

                wechat.setLId(wechatObj.getIntValue("id"));
                wechat.setSName(wechatObj.getString("nickname"));
                wechat.setSWechatId(wechatObj.getString("wechatId"));
                wechat.setSAlias(wechatObj.getString("alias"));
                wechat.setBOnline(wechatObj.getBoolean("wechatAlive") ? 1 : 0);
                wechat.setSAvatar(wechatObj.getString("avatar"));
                wechat.setSMobile(wechatObj.getString("bindMobile"));
                wechat.setLTotalFriend(wechatObj.getIntValue("totalFriend"));
                saveOrUpdate(wechat);
            }

            cacheService.set("syncwechatlist", "0");
        }
    }

    public void setSync() {
        cacheService.set("syncwechatlist", "1");
    }

    /**
     * 手机号是否该客服号的好友
     *
     * @param wechatId
     * @param sPhone
     * @return
     */
    public JSONObject getFriendByPhone(Integer wechatId, String sPhone) {
        WeChat weChat = getById(wechatId);

        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("ispass", true);
        mapParam.put("keyword", "");
        mapParam.put("accountid", "");
        mapParam.put("page", 0);
        mapParam.put("pagesize", 100);
        mapParam.put("preFriendId", "");
        mapParam.put("wechatAccountKeyword", weChat.getSWechatId());
        mapParam.put("friendKeyword", sPhone);
        mapParam.put("friendPhoneKeyword", sPhone);

        try {
            JSONArray arrFriend = customerTouchApi.getWechatFriendList(mapParam);
            if (arrFriend.size() > 0) {
                return arrFriend.getJSONObject(0);
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 客服号是否在线
     *
     * @param weChat
     * @return
     */
    public boolean isAlive(WeChat weChat) {
        JSONArray arrWechat = customerTouchApi.getWechatByKeyword(weChat.getSWechatId());
        for (int i = 0; i < arrWechat.size(); i++) {
            if (arrWechat.getJSONObject(i).getIntValue("id") == weChat.getLId()) {
                Boolean wechatAlive = arrWechat.getJSONObject(i).getBoolean("wechatAlive");

                //回写到数据库
                weChat.setBOnline(wechatAlive ? 1 : 0);
                updateById(weChat);

                return wechatAlive;
            }
        }

        //回写到数据库
        weChat.setBOnline(0);
        updateById(weChat);

        return false;
    }

    /**
     * 客服号是否在线
     *
     * @param wechatId
     * @return
     */
    public boolean isAlive(Integer wechatId) {
        WeChat weChat = getById(wechatId);
        return isAlive(weChat);
    }

    /**
     * 微信号是否允许发起好友申请
     * @return
     */
    public boolean canReq(WeChat weChat) {
        if (isAlive(weChat) && customerTouchApi.canReq(weChat.getLId())) {
            return true;
        }

        return false;
    }
}
