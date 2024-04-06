package cn.myerm.business.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.dto.WeChatDto;
import cn.myerm.business.dto.WeChatRoomDto;
import cn.myerm.business.entity.WeChat;
import cn.myerm.business.entity.WeChatRoom;
import cn.myerm.business.entity.WeChatRoomMember;
import cn.myerm.business.mapper.WeChatRoomMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.service.IWeChatRoomService;
import cn.myerm.common.utils.BeanUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeChatRoomServiceImpl extends BusinessServiceImpl<WeChatRoomMapper, WeChatRoom> implements IWeChatRoomService {

    private static final Logger logger = LoggerFactory.getLogger(WeChatRoomServiceImpl.class);

    @Resource
    private CustomerTouchApi customerTouchApi;

    @Resource
    private CacheServiceImpl cacheService;

    @Resource
    private WeChatRoomMemberServiceImpl weChatRoomMemberService;

    @Resource
    private WeChatServiceImpl weChatService;

    @Resource
    private AccountWechatServiceImpl accountWechatService;

    public void setSync() {
        cacheService.set("syncwechatroom", "1");
    }

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
        btn.put("sName", "同步微信群");
        btn.put("handler", "handleSyncWechatroom");
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

    public List<WeChatRoomDto> getByIds(String sChatroomIds) {
        List<WeChatRoomDto> listWeChatRoomDto = new ArrayList<>();

        JSONArray jsonArrayChatroomId = JSONArray.parseArray(sChatroomIds);

        if (jsonArrayChatroomId.size() == 0) {
            return listWeChatRoomDto;
        }

        List<WeChatRoom> weChatRooms = listByIds(jsonArrayChatroomId.toJavaList(Integer.class));
        for (WeChatRoom weChatRoom : weChatRooms) {
            WeChat weChat = weChatService.getById(weChatRoom.getWechatId());//归属的客服

            WeChatRoomDto weChatRoomDto = BeanUtils.transform(WeChatRoomDto.class, weChatRoom);
            weChatRoomDto.setWeChat(BeanUtils.transform(WeChatDto.class, weChat));

            listWeChatRoomDto.add(weChatRoomDto);
        }

        return listWeChatRoomDto;
    }

    public void sync(Integer wechatChatroomId) {
        JSONArray arrWechatroomMember = customerTouchApi.getChatroomMemberList(wechatChatroomId);

        ArrayList<String> listChatroomMemberId = new ArrayList<>();
        for (int j = 0; j < arrWechatroomMember.size(); j++) {
            listChatroomMemberId.add(arrWechatroomMember.getJSONObject(j).getString("wechatId"));
        }
        listChatroomMemberId.add("-1");

        //删除不存在的群成员
        QueryWrapper<WeChatRoomMember> memberQueryWrapper = new QueryWrapper<>();
        memberQueryWrapper.eq("WeChatRoomId", wechatChatroomId);
        memberQueryWrapper.notIn("sWechatId", listChatroomMemberId);
        weChatRoomMemberService.remove(memberQueryWrapper);

        ArrayList<WeChatRoomMember> listWeChatRoomMember = new ArrayList<>();
        for (int j = 0; j < arrWechatroomMember.size(); j++) {
            JSONObject member = arrWechatroomMember.getJSONObject(j);

            memberQueryWrapper = new QueryWrapper<>();
            memberQueryWrapper.eq("WeChatRoomId", wechatChatroomId);
            memberQueryWrapper.eq("sWechatId", member.getString("wechatId"));
            WeChatRoomMember weChatRoomMember = weChatRoomMemberService.getOne(memberQueryWrapper);
            if (weChatRoomMember == null) {
                weChatRoomMember = new WeChatRoomMember();
            }

            weChatRoomMember.setSName(member.getString("nickname"));
            weChatRoomMember.setWeChatRoomId(wechatChatroomId);
            weChatRoomMember.setSWechatId(member.getString("wechatId"));
            weChatRoomMember.setSAvatar(member.getString("avatar"));
            weChatRoomMember.setBAdmin(member.getBoolean("isAdmin") ? 1 : 0);
            weChatRoomMember.setBDeleted(member.getBoolean("isDeleted") ? 1 : 0);
            weChatRoomMember.setDDeletedDate(LocalDateTimeUtil.parse(member.getString("deletedDate").replace("+08:00", "")));
            listWeChatRoomMember.add(weChatRoomMember);
        }


    }


    /**
     * 同步私域系统的微信群
     */
    public synchronized void sync() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("pagesize", 50);
        mapParam.put("keyword", "");
        mapParam.put("accountid", "");
        mapParam.put("page", 0);

        logger.info("正在更新群信息");

        JSONArray arrWechatroom = customerTouchApi.getWechatroomList(mapParam);
        if (arrWechatroom.size() > 0) {
            ArrayList<WeChatRoom> listWeChatRoom = new ArrayList<>();
            ArrayList<WeChatRoomMember> listWeChatRoomMember = new ArrayList<>();
            for (int i = 0; i < arrWechatroom.size(); i++) {
                JSONObject jsonWechatRoom = arrWechatroom.getJSONObject(i);

                if (jsonWechatRoom.getBoolean("isDeleted")) {//已删除
                    QueryWrapper<WeChatRoomMember> memberQueryWrapper = new QueryWrapper<>();
                    memberQueryWrapper.eq("WeChatRoomId", jsonWechatRoom.getIntValue("id"));
                    weChatRoomMemberService.remove(memberQueryWrapper);

                    removeById(jsonWechatRoom.getIntValue("id"));
                    continue;
                }

                WeChatRoom weChatRoom = new WeChatRoom();
                weChatRoom.setLId(jsonWechatRoom.getIntValue("id"));
                weChatRoom.setSName(jsonWechatRoom.getString("nickname"));
                weChatRoom.setWechatId(jsonWechatRoom.getIntValue("wechatAccountId"));
                weChatRoom.setSAvatar(jsonWechatRoom.getString("chatroomAvatar"));
                weChatRoom.setSOwnerNickName(jsonWechatRoom.getString("chatroomOwnerNickname"));
                weChatRoom.setSOwnerAvatar(jsonWechatRoom.getString("chatroomOwnerAvatar"));
                weChatRoom.setSChatroomId(jsonWechatRoom.getString("chatroomId"));
                weChatRoom.setDDeleteTime(LocalDateTimeUtil.parse(jsonWechatRoom.getString("deleteTime")));
                weChatRoom.setDCreateTime(LocalDateTimeUtil.parse(jsonWechatRoom.getString("createTime")));
                weChatRoom.setBDeleted(jsonWechatRoom.getBoolean("isDeleted") ? 1 : 0);
                weChatRoom.setLUserNum(jsonWechatRoom.getJSONArray("members").size());

                listWeChatRoom.add(weChatRoom);

                ArrayList<String> listChatroomMemberId = new ArrayList<>();
                for (int j = 0; j < jsonWechatRoom.getJSONArray("members").size(); j++) {
                    listChatroomMemberId.add(jsonWechatRoom.getJSONArray("members").getJSONObject(j).getString("wechatId"));
                }
                listChatroomMemberId.add("-1");

                //删除不存在的群成员
                QueryWrapper<WeChatRoomMember> memberQueryWrapper = new QueryWrapper<>();
                memberQueryWrapper.eq("WeChatRoomId", jsonWechatRoom.getIntValue("id"));
                memberQueryWrapper.notIn("sWechatId", listChatroomMemberId);
                weChatRoomMemberService.remove(memberQueryWrapper);

                for (int j = 0; j < jsonWechatRoom.getJSONArray("members").size(); j++) {
                    JSONObject member = jsonWechatRoom.getJSONArray("members").getJSONObject(j);

                    memberQueryWrapper = new QueryWrapper<>();
                    memberQueryWrapper.eq("WeChatRoomId", jsonWechatRoom.getIntValue("id"));
                    memberQueryWrapper.eq("sWechatId", member.getString("wechatId"));
                    WeChatRoomMember weChatRoomMember = weChatRoomMemberService.getOne(memberQueryWrapper);
                    if (weChatRoomMember == null) {
                        weChatRoomMember = new WeChatRoomMember();
                    }

                    weChatRoomMember.setSName(member.getString("nickname"));
                    weChatRoomMember.setWeChatRoomId(jsonWechatRoom.getIntValue("id"));
                    weChatRoomMember.setSWechatId(member.getString("wechatId"));
                    weChatRoomMember.setSAvatar(member.getString("avatar"));
                    weChatRoomMember.setBAdmin(member.getBoolean("isAdmin") ? 1 : 0);
                    weChatRoomMember.setBDeleted(member.getBoolean("isDeleted") ? 1 : 0);
                    weChatRoomMember.setDDeletedDate(LocalDateTimeUtil.parse(member.getString("deletedDate").replace("+08:00", "")));
                    listWeChatRoomMember.add(weChatRoomMember);
                }
            }

            saveOrUpdateBatch(listWeChatRoom);
            weChatRoomMemberService.saveOrUpdateBatch(listWeChatRoomMember);

            logger.info("群信息更新完毕");
        }
    }
}