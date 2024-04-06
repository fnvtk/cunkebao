package cn.myerm.business.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.dto.WeChatDto;
import cn.myerm.business.dto.WeChatFriendDto;
import cn.myerm.business.entity.Cache;
import cn.myerm.business.entity.WeChat;
import cn.myerm.business.entity.WeChatFriend;
import cn.myerm.business.mapper.WeChatFriendMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IWeChatFriendService;
import cn.myerm.common.exception.SystemException;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class WeChatFriendServiceImpl extends BusinessServiceImpl<WeChatFriendMapper, WeChatFriend> implements IWeChatFriendService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatFriendServiceImpl.class);

    @Resource
    private CustomerTouchApi customerTouchApi;

    @Resource
    private CacheServiceImpl cacheService;

    @Resource
    private WeChatServiceImpl weChatService;

    @Resource
    private AccountWechatServiceImpl accountWechatService;

    public String appendWhereSql(ListParam listParam) {
        if (getCurrUser().getSysRoleId() > 1) {
            List<String> wechatIds = accountWechatService.getWechatIds();
            wechatIds.add("0");
            return super.appendWhereSql(listParam) + " AND WeChatId IN (" + String.join(", ", wechatIds) + ")";
        }

        return super.appendWhereSql(listParam);
    }

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();
        btn.put("ID", "sync");
        btn.put("sName", "同步微信朋友");
        btn.put("handler", "handleSyncWechatFriend");
        btn.put("icon", "el-icon-refresh");
        arrBtn.add(btn);

        btn = new JSONObject();
        btn.put("ID", "select");
        btn.put("sName", "批量选中");
        btn.put("handler", "handleBatchSelect");
        btn.put("icon", "el-icon-check");
        arrBtn.add(btn);


        arrBtn.addAll(super.getListTableBtn(mapParam));

        return arrBtn;
    }

    /**
     * 同步私域系统的微信好友
     */
    public void sync() {
        logger.info("开始同步微信好友的数据");

        int lFriendTotal = customerTouchApi.getWechatFriendCount(new HashMap<>());
        int lPageTotal = Math.ceilDiv(lFriendTotal, 1000);

        logger.info("共有" + lFriendTotal + "个友好，" + lPageTotal + "页数据");

        Integer preFriendId = null;
        for (int i = 0; i < lPageTotal; i++) {
            logger.info("正在同步第" + i + "页的微信好友");

            Map<String, Object> mapParam = new HashMap<>();
            mapParam.put("ispass", true);
            mapParam.put("keyword", "");
            mapParam.put("accountid", "");
            mapParam.put("page", i);
            mapParam.put("pagesize", 1000);
            mapParam.put("preFriendId", preFriendId);

            try {
                JSONArray arrFriend = customerTouchApi.getWechatFriendList(mapParam);
                preFriendId = arrFriend.getJSONObject(arrFriend.size() - 1).getIntValue("id");

                cacheService.set("wechatfriend-" + i, arrFriend.toJSONString());
            } catch (SystemException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.info("同步微信好友完成");
    }

    /**
     * 把同步过来的数据，保存到数据库
     */
    public void saveSync() {
        ExecutorService threadPool = Executors.newFixedThreadPool(4);

        logger.info("开始处理微信朋友的缓存数据");

        int lStart = 0;
        while (true) {
            List<Cache> listFriend = cacheService.getByPrefix("wechatfriend", lStart, 1);
            if (listFriend.size() > 0) {
                for (int i = 0; i < listFriend.size(); i++) {
                    Cache cache = listFriend.get(i);
                    logger.info("正在处理:" + cache.getSKey());

                    threadPool.execute(new SaveSyncTask(cache.getSValue()));
                }
            } else {
                break;
            }

            lStart++;
        }

        threadPool.shutdown();

        while (true) {
            if (threadPool.isTerminated()) {
                break;
            }
        }

        cacheService.removeByPrefix("wechatfriend-");
        logger.info("微信朋友的缓存数据处理完毕");
    }

    public void setSync() {
        cacheService.set("syncwechatfriend", "1");
    }

    /**
     * 通过ID集合，获得微信好友的集合
     */
    public List<WeChatFriendDto> getByIds(String sFrindIds) {

        List<WeChatFriendDto> listWeChatFriendDto = new ArrayList<>();

        JSONArray jsonArrayFriendId = JSONArray.parseArray(sFrindIds);

        if (jsonArrayFriendId.size() == 0) {
            return listWeChatFriendDto;
        }

        List<WeChatFriend> weChatFriends = listByIds(jsonArrayFriendId.toJavaList(Integer.class));
        for (WeChatFriend weChatFriend : weChatFriends) {
            WeChat weChat = weChatService.getById(weChatFriend.getWeChatId());//归属的客服

            WeChatFriendDto weChatFriendDto = BeanUtils.transform(WeChatFriendDto.class, weChatFriend);
            weChatFriendDto.setWeChat(BeanUtils.transform(WeChatDto.class, weChat));

            listWeChatFriendDto.add(weChatFriendDto);
        }

        return listWeChatFriendDto;
    }

    public WeChatFriend saveWechatFriend(JSONObject friend) {
        WeChatFriend weChatFriend = new WeChatFriend();
        weChatFriend.setLId(friend.getIntValue("id"));
        weChatFriend.setSName(friend.getString("nickname"));
        weChatFriend.setWeChatId(friend.getIntValue("wechatAccountId"));
        weChatFriend.setSWechatId(friend.getString("wechatId"));
        weChatFriend.setSAlias(friend.getString("alias"));
        weChatFriend.setSPhone(friend.getString("phone"));
        weChatFriend.setSConRemark(friend.getString("conRemark"));
        weChatFriend.setSConRemark(friend.getString("conRemark"));
        weChatFriend.setSPyInitial(friend.getString("pyInitial"));
        weChatFriend.setSQuanPin(friend.getString("quanPin"));
        weChatFriend.setSAvatar(friend.getString("avatar"));
        weChatFriend.setGenderId(friend.getIntValue("gender"));
        weChatFriend.setSRegion(friend.getString("region"));
        weChatFriend.setSCountry(friend.getString("country"));
        weChatFriend.setSProvince(friend.getString("privince"));
        weChatFriend.setSCity(friend.getString("city"));
        weChatFriend.setLAddFrom(friend.getIntValue("addFrom"));
        weChatFriend.setSSignature(friend.getString("signature"));
        weChatFriend.setSLabels(friend.getJSONArray("labels").toJSONString());
        weChatFriend.setDCreateTime(LocalDateTimeUtil.parse(friend.getString("createTime")));
        weChatFriend.setDDeleteTime(LocalDateTimeUtil.parse(friend.getString("deleteTime")));
        weChatFriend.setBDeleted(friend.getBoolean("isDeleted") ? 1 : 0);
        weChatFriend.setBPassed(friend.getBoolean("isPassed") ? 1 : 0);

        return weChatFriend;
    }

    /**
     * 同步微信好友的任务
     */
    class SaveSyncTask implements Runnable {

        private final JSONArray arrData;

        public SaveSyncTask(String sJsonData) {
            this.arrData = JSONArray.parseArray(sJsonData);
        }

        @Override
        public void run() {
            ArrayList<WeChatFriend> listWeChatFriend = new ArrayList<>();
            for (int i = 0; i < arrData.size(); i++) {
                JSONObject friend = arrData.getJSONObject(i);
                WeChatFriend weChatFriend = saveWechatFriend(friend);

                listWeChatFriend.add(weChatFriend);
            }

            saveOrUpdateBatch(listWeChatFriend);
        }
    }
}
