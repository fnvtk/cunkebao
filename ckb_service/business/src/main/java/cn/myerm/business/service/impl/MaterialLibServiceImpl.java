package cn.myerm.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.entity.*;
import cn.myerm.business.event.EventApi;
import cn.myerm.business.mapper.MaterialLibMapper;
import cn.myerm.business.param.EventParam;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.param.MaterialLibParam;
import cn.myerm.business.service.IMaterialLibService;
import cn.myerm.objectbuilder.entity.SysObject;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class MaterialLibServiceImpl extends BusinessServiceImpl<MaterialLibMapper, MaterialLib> implements IMaterialLibService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialLibServiceImpl.class);

    @Resource
    private WeChatFriendServiceImpl weChatFriendService;

    @Resource
    private WeChatRoomMemberServiceImpl weChatRoomMemberService;

    @Resource
    private CustomerTouchApi customerTouchApi;

    @Resource
    private EventApi eventApi;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public JSONArray getListTableBtn(Map<String, String> mapParam) {
        JSONArray arrBtn = new JSONArray();

        JSONObject btn = new JSONObject();

        btn.put("ID", "select");
        btn.put("sName", "批量选中");
        btn.put("handler", "handleBatchSelect");
        btn.put("icon", "el-icon-check");
        arrBtn.add(btn);

        arrBtn.addAll(super.getListTableBtn(mapParam));

        return arrBtn;
    }

    protected void handleListData(List<Map<String, Object>> listObjectData) {
        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);
            objectData.put("sCollectObjectJson", JSONArray.parseArray((String) objectData.get("sCollectObjectJson")));
        }
    }

    /**
     * 新建保存
     *
     * @param materialLibParam
     */
    public Integer save(MaterialLibParam materialLibParam) {
        JSONObject jsonChatroomMember = JSONObject.parseObject(materialLibParam.getChatroomMember());

        JSONObject jsonMomentCfg = new JSONObject();
        jsonMomentCfg.put("objectid", JSONArray.parseArray(materialLibParam.getArrFriendId()));

        JSONObject jsonChatroomCfg = new JSONObject();

        JSONArray arrObject = new JSONArray();
        JSONArray arrJsonChatroomId = JSONArray.parseArray(materialLibParam.getArrChatroomId());
        for (int i = 0; i < arrJsonChatroomId.size(); i++) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("chatroomid", arrJsonChatroomId.getIntValue(i));

            JSONArray arrMemberId = jsonChatroomMember.getJSONArray(arrJsonChatroomId.getString(i));
            if (arrMemberId == null) {
                jsonObject.put("objectid", new JSONArray());
            } else {
                jsonObject.put("objectid", arrMemberId);
            }

            arrObject.add(jsonObject);
        }
        jsonChatroomCfg.put("objects", arrObject);

        JSONObject jsonCfg = new JSONObject();
        jsonCfg.put("moment", jsonMomentCfg);
        jsonCfg.put("chatroom", jsonChatroomCfg);

        MaterialLib materialLib = new MaterialLib();
        materialLib.setSName(materialLibParam.getSName());
        materialLib.setSKeyWord(materialLibParam.getSKeyWord());
        materialLib.setSAIRequire(materialLibParam.getSAIRequire());
        materialLib.setSExclude(materialLibParam.getSExclude());
        materialLib.setBEnable(materialLibParam.getBEnable() ? 1 : 0);
        materialLib.setBAIEnable(materialLibParam.getBAIEnable() ? 1 : 0);
        materialLib.setSConfigJson(jsonCfg.toJSONString());
        materialLib.setSTimeLimit(materialLibParam.getSTimeLimit());
        materialLib.setNewUserId(getCurrUser().getLID());

        if (materialLibParam.getId() != null) {
            materialLib.setLId(materialLibParam.getId());
        }

        saveOrUpdate(materialLib);

        materialLib.setSCollectObjectJson(JSONArray.toJSONString(getCollectObjects(materialLib)));
        updateById(materialLib);

        JSONArray arrFriendId = JSONArray.parseArray(materialLibParam.getArrFriendId());
        if (arrFriendId.size() > 0) {
            String wechatFriendIds = String.join(",", arrFriendId.toJavaList(String.class));
            customerTouchApi.multiAllocFriendToAccount(wechatFriendIds);
        }

        EventParam eventParam = new EventParam();
        eventParam.setEvent("materiallibsave");
        eventParam.setName("内容库新建或者编辑保存");
        eventParam.setPath("/ckbservice/materiallib/save");
        eventParam.setParam("{\"id\":" + materialLib.getLId() + "}");
        eventApi.triggle(eventParam);

        return materialLib.getLId();
    }

    /**
     * 删除后
     *
     * @param sysObject
     * @param objectId
     */
    protected void afterDel(SysObject sysObject, String objectId) {
        jdbcTemplate.execute("DELETE FROM `MaterialPushLog` WHERE MaterialId IN (SELECT lId FROM `Material` WHERE MaterialLibId='" + objectId + "')");
        jdbcTemplate.execute("DELETE FROM `Material` WHERE MaterialLibId='" + objectId + "'");

        String sSql = "SELECT lId, MaterialLibIds FROM `MaterialPushChatroomTask` WHERE MaterialLibIds LIKE '%;" + objectId + ";%'";
        List<Map<String, Object>> listTask = jdbcTemplate.queryForList(sSql);
        for (Map<String, Object> mapTask : listTask) {
            List<String> newIdList = new ArrayList<>();
            List<String> materialLibIds = StrUtil.splitTrim((String) mapTask.get("MaterialLibIds"), ";");
            for (String materialLibId : materialLibIds) {
                if (!materialLibId.equals(objectId)) {
                    newIdList.add(materialLibId);
                }
            }

            String join = ";" + String.join(";", newIdList) + ";";
            jdbcTemplate.execute("UPDATE `MaterialPushChatroomTask` SET MaterialLibIds='" + join + "' WHERE lId='" + mapTask.get("lId") + "'");
        }

        sSql = "SELECT lId, MaterialLibIds FROM `MaterialPushMomentTask` WHERE MaterialLibIds LIKE '%;" + objectId + ";%'";
        listTask = jdbcTemplate.queryForList(sSql);
        for (Map<String, Object> mapTask : listTask) {
            List<String> newIdList = new ArrayList<>();
            List<String> materialLibIds = StrUtil.splitTrim((String) mapTask.get("MaterialLibIds"), ";");
            for (String materialLibId : materialLibIds) {
                if (!materialLibId.equals(objectId)) {
                    newIdList.add(materialLibId);
                }
            }

            String join = ";" + String.join(";", newIdList) + ";";

            if (newIdList.size() == 0) {
                jdbcTemplate.execute("UPDATE `MaterialPushMomentTask` SET MaterialLibIds='" + join + "', bEnable=0 WHERE lId='" + mapTask.get("lId") + "'");
            } else {
                jdbcTemplate.execute("UPDATE `MaterialPushMomentTask` SET MaterialLibIds='" + join + "' WHERE lId='" + mapTask.get("lId") + "'");
            }
        }
    }

    /**
     * 把采集的对象推到触客端
     */
    public void push() {
        List<MaterialLib> listMaterialLib = list();
        JSONArray pushObjects = new JSONArray();

        ArrayList<Integer> arrDisableFriendId = new ArrayList<>();
        ArrayList<Integer> arrDisableChatroomId = new ArrayList<>();
        ArrayList<Integer> arrEnableFriendId = new ArrayList<>();
        ArrayList<Integer> arrEnableChatroomId = new ArrayList<>();
        Map<Integer, JSONArray> mapChatroomMember = new HashMap<>();
        for (MaterialLib materialLib : listMaterialLib) {
            JSONObject jsonCfg = JSONObject.parseObject(materialLib.getSConfigJson());
            JSONObject moment = jsonCfg.getJSONObject("moment");
            if (moment != null) {
                JSONArray arrObjectId = moment.getJSONArray("objectid");
                for (int i = 0; i < arrObjectId.size(); i++) {
                    //是否启用
                    if (materialLib.getBEnable().equals(1)) {
                        arrEnableFriendId.add(arrObjectId.getIntValue(i));

                        int index = arrDisableFriendId.indexOf(arrObjectId.getIntValue(i));
                        if (index > -1) {
                            arrDisableFriendId.remove(index);
                        }
                    } else {
                        int index = arrEnableFriendId.indexOf(arrObjectId.getIntValue(i));
                        if (index == -1) {
                            arrDisableFriendId.add(arrObjectId.getIntValue(i));
                        }
                    }
                }
            }

            JSONObject jsonChatroom = jsonCfg.getJSONObject("chatroom");
            if (jsonChatroom != null) {
                JSONArray chatroomobjects = jsonChatroom.getJSONArray("objects");
                for (int i = 0; i < chatroomobjects.size(); i++) {
                    JSONObject chatroom = chatroomobjects.getJSONObject(i);

                    Integer chatroomid = chatroom.getIntValue("chatroomid");

                    if (materialLib.getBEnable().equals(1)) {
                        arrEnableChatroomId.add(chatroomid);

                        int index = arrDisableChatroomId.indexOf(chatroomid);
                        if (index > -1) {
                            arrDisableChatroomId.remove(index);
                        }
                    } else {
                        int index = arrEnableChatroomId.indexOf(chatroomid);
                        if (index == -1) {
                            arrDisableChatroomId.add(chatroomid);
                        }
                    }

                    JSONArray arrMemberId = mapChatroomMember.get(chatroomid);
                    if (arrMemberId == null) {
                        mapChatroomMember.put(chatroomid, chatroom.getJSONArray("objectid"));
                    } else {
                        arrMemberId.addAll(chatroom.getJSONArray("objectid"));
                        mapChatroomMember.put(chatroomid, arrMemberId);
                    }
                }
            }
        }

        LinkedHashSet<Integer> hashSet = new LinkedHashSet<>(arrDisableFriendId);
        arrDisableFriendId = new ArrayList<>(hashSet);
        for (Integer friendId : arrDisableFriendId) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "moment");
            jsonObject.put("isfull", true);
            jsonObject.put("enable", false);

            WeChatFriend weChatFriend = weChatFriendService.getById(friendId);
            jsonObject.put("accountid", weChatFriend.getWeChatId());
            jsonObject.put("friendid", friendId);

            jsonObject.put("username", weChatFriend.getSWechatId() + "," + weChatFriend.getSAlias());

            pushObjects.add(jsonObject);
        }

        hashSet = new LinkedHashSet<>(arrEnableFriendId);
        arrEnableFriendId = new ArrayList<>(hashSet);
        for (Integer friendId : arrEnableFriendId) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "moment");
            jsonObject.put("isfull", true);
            jsonObject.put("enable", true);

            WeChatFriend weChatFriend = weChatFriendService.getById(friendId);
            jsonObject.put("accountid", weChatFriend.getWeChatId());
            jsonObject.put("friendid", friendId);

            jsonObject.put("username", weChatFriend.getSWechatId() + "," + weChatFriend.getSAlias());

            pushObjects.add(jsonObject);
        }

        hashSet = new LinkedHashSet<>(arrDisableChatroomId);
        arrDisableChatroomId = new ArrayList<>(hashSet);
        for (Integer chatroomId : arrDisableChatroomId) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "chatroom");
            jsonObject.put("enable", false);
            jsonObject.put("chatroomid", chatroomId);

            if (mapChatroomMember.get(chatroomId).size() > 0) {
                JSONArray arrMemberWechatId = new JSONArray();
                QueryWrapper<WeChatRoomMember> queryWrapper = new QueryWrapper<>();
                List<WeChatRoomMember> weChatRoomMembers = weChatRoomMemberService.listByIds(mapChatroomMember.get(chatroomId).toJavaList(Integer.class));
                for (WeChatRoomMember weChatRoomMember : weChatRoomMembers) {
                    arrMemberWechatId.add(weChatRoomMember.getSWechatId());
                }
                jsonObject.put("memberwechatids", arrMemberWechatId);
            } else {
                jsonObject.put("memberwechatids", new JSONArray());
            }

            pushObjects.add(jsonObject);
        }

        hashSet = new LinkedHashSet<>(arrEnableChatroomId);
        arrEnableChatroomId = new ArrayList<>(hashSet);
        for (Integer chatroomId : arrEnableChatroomId) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "chatroom");
            jsonObject.put("enable", true);

            jsonObject.put("chatroomid", chatroomId);

            if (mapChatroomMember.get(chatroomId).size() > 0) {
                JSONArray arrMemberWechatId = new JSONArray();
                QueryWrapper<WeChatRoomMember> queryWrapper = new QueryWrapper<>();
                List<WeChatRoomMember> weChatRoomMembers = weChatRoomMemberService.listByIds(mapChatroomMember.get(chatroomId).toJavaList(Integer.class));
                for (WeChatRoomMember weChatRoomMember : weChatRoomMembers) {
                    arrMemberWechatId.add(weChatRoomMember.getSWechatId());
                }
                jsonObject.put("memberwechatids", arrMemberWechatId);
            } else {
                jsonObject.put("memberwechatids", new JSONArray());
            }

            pushObjects.add(jsonObject);
        }

        customerTouchApi.pushCollectObject(pushObjects);
    }

    /**
     * 获得所有内容库的采集对象
     */
    public JSONArray getCollectObjects() {
        JSONArray jsonArray = new JSONArray();

        List<MaterialLib> materialLibList = list();
        for (MaterialLib materialLib : materialLibList) {
            jsonArray.addAll(JSONArray.parseArray(materialLib.getSCollectObjectJson()));
        }

        return jsonArray;
    }

    /**
     * 获取某个内容库的采集对象
     *
     * @param materialLib
     * @return
     */
    public List<MaterialCollectObject> getCollectObjects(MaterialLib materialLib) {

        List<MaterialCollectObject> listCollectObject = new ArrayList<>();

        JSONObject jsonCfg = JSONObject.parseObject(materialLib.getSConfigJson());

        JSONObject moment = jsonCfg.getJSONObject("moment");
        JSONArray arrObjectId = moment.getJSONArray("objectid");

        if (arrObjectId.size() > 0) {
            List<WeChatFriend> weChatFriends = weChatFriendService.listByIds(arrObjectId.toJavaList(Integer.class));
            for (int i = 0; i < arrObjectId.size(); i++) {
                MaterialCollectObject materialCollectObject = new MaterialCollectObject();
                materialCollectObject.setMaterialLibId(materialLib.getLId());
                materialCollectObject.setSKeyWord(materialLib.getSKeyWord());
                materialCollectObject.setSExclude(materialLib.getSExclude());
                materialCollectObject.setSTimeLimit(materialLib.getSTimeLimit());
                materialCollectObject.setSCollectObjectId("/moment/" + arrObjectId.getString(i));
                materialCollectObject.setNewUserId(materialLib.getNewUserId());

                for (WeChatFriend weChatFriend : weChatFriends) {
                    if (weChatFriend.getLId().equals(arrObjectId.getIntValue(i))) {
                        materialCollectObject.setSNickName(weChatFriend.getSName());
                        materialCollectObject.setSAvatar(weChatFriend.getSAvatar());
                        break;
                    }
                }

                listCollectObject.add(materialCollectObject);
            }
        }

        JSONArray chatroomobjects = jsonCfg.getJSONObject("chatroom").getJSONArray("objects");
        for (int i = 0; i < chatroomobjects.size(); i++) {
            JSONObject chatroom = chatroomobjects.getJSONObject(i);
            Integer chatroomid = chatroom.getIntValue("chatroomid");
            arrObjectId = chatroom.getJSONArray("objectid");

            if (arrObjectId.size() > 0) {
                QueryWrapper<WeChatRoomMember> queryWrapper = new QueryWrapper<>();
                List<WeChatRoomMember> weChatRoomMembers = weChatRoomMemberService.listByIds(chatroom.getJSONArray("objectid").toJavaList(Integer.class));
                for (WeChatRoomMember weChatRoomMember : weChatRoomMembers) {
                    MaterialCollectObject materialCollectObject = new MaterialCollectObject();
                    materialCollectObject.setMaterialLibId(materialLib.getLId());
                    materialCollectObject.setSCollectObjectId("/chatroom/" + chatroomid + "/" + weChatRoomMember.getSWechatId());
                    materialCollectObject.setChatroomId(chatroomid);
                    materialCollectObject.setSWechatId(weChatRoomMember.getSWechatId());
                    materialCollectObject.setNewUserId(materialLib.getNewUserId());
                    materialCollectObject.setSAvatar(weChatRoomMember.getSAvatar());
                    materialCollectObject.setSNickName(weChatRoomMember.getSName());
                    materialCollectObject.setSKeyWord(materialLib.getSKeyWord());
                    materialCollectObject.setSExclude(materialLib.getSExclude());

                    listCollectObject.add(materialCollectObject);
                }
            }
        }

        return listCollectObject;
    }

    /**
     * 获取某个指定ID的内容库采集对象
     *
     * @param MaterialLibId
     * @return
     */
    public List<MaterialCollectObject> getCollectObjects(Integer MaterialLibId) {
        return getCollectObjects(getById(MaterialLibId));
    }

    public String appendWhereSql(ListParam listParam) {
        if (getCurrUser().getSysRoleId() > 1) {
            return super.appendWhereSql(listParam) + " AND NewUserId='" + getCurrUser().getLID() + "'";
        }

        return super.appendWhereSql(listParam);
    }
}
