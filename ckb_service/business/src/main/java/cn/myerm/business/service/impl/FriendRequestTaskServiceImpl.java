package cn.myerm.business.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.myerm.business.api.CustomerTouchApi;
import cn.myerm.business.entity.*;
import cn.myerm.business.event.EventApi;
import cn.myerm.business.mapper.FriendRequestTaskMapper;
import cn.myerm.business.param.*;
import cn.myerm.business.service.IFriendRequestTaskService;
import cn.myerm.common.exception.SystemException;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import cn.myerm.system.service.impl.SysUserServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static cn.hutool.core.util.RandomUtil.randomNumbers;

@Service
public class FriendRequestTaskServiceImpl extends BusinessServiceImpl<FriendRequestTaskMapper, FriendRequestTask> implements IFriendRequestTaskService {
    private static final Logger logger = LoggerFactory.getLogger(FriendRequestTaskServiceImpl.class);

    @Resource
    private EventApi eventApi;

    @Resource
    private FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService;

    @Resource
    private FriendRequestConfigServiceImpl friendRequestConfigService;

    @Resource
    private WeChatFriendLabelServiceImpl weChatFriendLabelService;

    @Resource
    private WeChatFriendLabelLibServiceImpl weChatFriendLabelLibService;

    @Resource
    private CustomerTouchApi customerTouchApi;

    @Resource
    private WeChatServiceImpl weChatService;

    @Resource
    private WeChatFriendServiceImpl weChatFriendService;

    @Resource
    private FriendMessageTaskServiceImpl friendMessageTaskService;

    @Resource
    private SysAttachServiceImpl sysAttachService;

    @Resource
    private MiniProgrameLibServiceImpl miniProgrameLibService;

    @Resource
    private SysUserServiceImpl sysUserService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private FriendRequestTaskScanServiceImpl friendRequestTaskScanService;

    @Resource
    private UserIncomeServiceImpl userIncomeService;

    public static boolean
    isCurrentTimeWithinRange(String timeRange) {
        if (StrUtil.isEmpty(timeRange) || timeRange.equals("-")) {
            return true;
        }

        String[] times = timeRange.split("-");
        LocalTime startTime = LocalTime.parse(times[0]);
        LocalTime endTime = LocalTime.parse(times[1]);
        LocalTime currentTime = LocalTime.now();

        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }

    /**
     * 新建编辑保存计划
     *
     * @param friendReqTaskParam
     */
    public void save(FriendReqTaskParam friendReqTaskParam) {

        SysUser currUser = getCurrUser();

        FriendRequestTask friendRequestTask;
        if (friendReqTaskParam.getLId() != null) {
            friendRequestTask = getById(friendReqTaskParam.getLId());
            friendRequestTask.setDEditTime(LocalDateTime.now());
            friendRequestTask.setEditUserId(currUser.getLID());
        } else {
            friendRequestTask = new FriendRequestTask();
            friendRequestTask.setDNewTime(LocalDateTime.now());
            friendRequestTask.setNewUserId(currUser.getLID());
        }

        friendRequestTask.setSName(friendReqTaskParam.getSName());
        friendRequestTask.setTypeId(friendReqTaskParam.getTypeId());
        friendRequestTask.setAccountId(currUser.getAccountId());

        List<String> listWechatId = JSONArray.parseArray(friendReqTaskParam.getArrWechatId()).toJavaList(String.class);
        friendRequestTask.setWeChatId(";" + String.join(";", listWechatId) + ";");

        friendRequestTask.setIncomeTypeId(friendReqTaskParam.getIncomeTypeId());
        friendRequestTask.setFIncome(friendReqTaskParam.getFIncome());
        friendRequestTask.setPosterId(friendReqTaskParam.getPosterId());
        friendRequestTask.setMemoTypeId(friendReqTaskParam.getMemoTypeId());
        friendRequestTask.setMemoParamId(friendReqTaskParam.getMemoParamId());
        friendRequestTask.setSHello(friendReqTaskParam.getSHello());
        friendRequestTask.setSTheme(friendReqTaskParam.getSTheme());
        friendRequestTask.setSTip(friendReqTaskParam.getSTip());
        friendRequestTask.setJTask(friendReqTaskParam.getArrTask());
        friendRequestTask.setSPeriod(friendReqTaskParam.getSPeriod());
        friendRequestTask.setLRequestInterval(friendReqTaskParam.getLRequestInterval());
        friendRequestTask.setLMaxPerDayRequestAmt(friendReqTaskParam.getLMaxPerDayRequestAmt());
        friendRequestTask.setBEnable(friendReqTaskParam.getBEnable() ? 1 : 0);
        saveOrUpdate(friendRequestTask);

        EventParam eventParam = new EventParam();
        eventParam.setEvent("friendreqtasksave");
        eventParam.setName("加友计划保存");
        eventParam.setPath("/ckbservice/friendrequesttask/save");
        eventParam.setParam("{\"taskid\":" + friendRequestTask.getLId() + "}");
        eventApi.triggle(eventParam);
    }

    public String appendWhereSql(ListParam listParam) {
        SysUser currUser = getCurrUser();
        if (currUser.getSysRoleId() > 2) {
            return super.appendWhereSql(listParam) +
                    " AND (AccountId='" + currUser.getAccountId() + "' OR NewUserId='" + currUser.getLID() +
                    "' OR lId IN (SELECT ObjectId FROM `SysShare` WHERE sObjectName='Business/FriendRequestTask' AND ToUserId='" + currUser.getLID() + "'))";
        }

        return super.appendWhereSql(listParam);
    }

    @Transactional
    public void alloc(ListParam listParam) {
        SysUser currUser = getCurrUser();

        //设置全部出来，不要分页
        listParam.setCanpage(0);

        //只要id和name字段
        List<String> listDispCol = new ArrayList<>();
        listDispCol.add("lId");
        listDispCol.add("sName");
        listDispCol.add("NewUserId");
        listParam.setDispcol(listDispCol);

        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        for (Map<String, Object> mapObjectData : listMapObjectData) {
            Map<String, Object> data = (Map<String, Object>) mapObjectData.get("data");
            String objectId = String.valueOf(data.get("lId"));
            Map<String, Object> mapNewUser = (Map<String, Object>) data.get("NewUserId");
            String sName = String.valueOf(data.get("sName"));

            if (currUser.getLID().equals((Integer) mapNewUser.get("ID")) || currUser.getSysRoleId() < 3) {
                String sSql = "INSERT INTO `SysShare` (sObjectName, ObjectId, ToUserId) " +
                        "VALUES ('Business/FriendRequestTask', '" + objectId + "', '" + listParam.getKeyword() + "')";
                jdbcTemplate.execute(sSql);
            } else {
                throw new SystemException("您没有权限分配【" + sName + "】");
            }
        }
    }

    public void saveWechat(Integer id, String wechatid) {
        FriendRequestTask friendRequestTask = new FriendRequestTask();
        friendRequestTask.setLId(id);
        friendRequestTask.setWeChatId(";" + wechatid.replace(",", ";") + ";");
        updateById(friendRequestTask);
    }

    @Transactional
    public void del(ListParam listParam) {
        SysUser currUser = getCurrUser();

        //设置全部出来，不要分页
        listParam.setCanpage(0);

        //只要id和name字段
        List<String> listDispCol = new ArrayList<>();
        listDispCol.add("lId");
        listDispCol.add("sName");
        listDispCol.add("NewUserId");
        listParam.setDispcol(listDispCol);

        Map<String, Object> mapResult = getListData(listParam);
        List<Map<String, Object>> listMapObjectData = (List<Map<String, Object>>) mapResult.get("arrData");

        for (Map<String, Object> mapObjectData : listMapObjectData) {
            Map<String, Object> data = (Map<String, Object>) mapObjectData.get("data");
            String objectId = String.valueOf(data.get("lId"));
            Map<String, Object> mapNewUser = (Map<String, Object>) data.get("NewUserId");
            String sName = String.valueOf(data.get("sName"));

            if (currUser.getLID().equals(mapNewUser.get("ID")) || currUser.getSysRoleId() < 3) {
                removeById(objectId);
                afterDel(objectId);
            } else {
                jdbcTemplate.execute("DELETE FROM `SysShare` WHERE sObjectName='Business/FriendRequestTask' " +
                        "AND ObjectId='" + objectId + "' AND ToUserId='" + currUser.getLID() + "'");
            }
        }
    }

    public void afterDel(String objectId) {
        QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", objectId);
        friendRequestTaskDetailService.remove(queryWrapper);

        QueryWrapper<FriendMessageTask> friendMessageTaskQueryWrapper = new QueryWrapper<>();
        friendMessageTaskQueryWrapper.eq("FriendRequestTaskId", objectId);
        friendMessageTaskService.remove(friendMessageTaskQueryWrapper);

        QueryWrapper<FriendRequestConfig> friendRequestConfigQueryWrapper = new QueryWrapper<>();
        friendRequestConfigQueryWrapper.eq("FriendRequestTaskId", objectId);
        friendRequestConfigService.remove(friendRequestConfigQueryWrapper);
    }

    /**
     * 更新计划的一些统计状态
     *
     * @param id
     */
    public void updateStatus(Integer id) {
        QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        int lTargetAmt = friendRequestTaskDetailService.count(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.eq("StatusId", 0);
        int lNoExecAmt = friendRequestTaskDetailService.count(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.eq("StatusId", 2);
        int lPassAmt = friendRequestTaskDetailService.count(queryWrapper);

        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.inSql("StatusId", "2,4");
        int lSuccessAmt = friendRequestTaskDetailService.count(queryWrapper);

        UpdateWrapper<FriendRequestTask> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.set("lTargetAmt", lTargetAmt);
        objectUpdateWrapper.set("lNoExecAmt", lNoExecAmt);
        objectUpdateWrapper.set("lSuccessAmt", lSuccessAmt);
        objectUpdateWrapper.set("lPassAmt", lPassAmt);
        objectUpdateWrapper.eq("lId", id);

        update(objectUpdateWrapper);

        caluNextExec(id);
    }

    public void updateConfig(Integer id) {
        FriendRequestTask friendRequestTask = getById(id);

        //清空原有的数据
        QueryWrapper<FriendRequestConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        friendRequestConfigService.remove(queryWrapper);

        JSONArray arrTask = JSONArray.parseArray(friendRequestTask.getJTask());
        for (int i = 0; i < arrTask.size(); i++) {
            JSONObject task = arrTask.getJSONObject(i);

            FriendRequestConfig friendRequestConfig = new FriendRequestConfig();
            friendRequestConfig.setSId(id + "-" + i);
            friendRequestConfig.setFriendRequestTaskId(id);
            friendRequestConfig.setSName(task.getString("sName"));
            friendRequestConfig.setSUUId(task.getString("id"));
            friendRequestConfig.setTypeId(task.getIntValue("TypeId"));
            friendRequestConfig.setJCond(task.getString("jCond"));
            friendRequestConfig.setJMessage(task.getString("jMessage"));
            friendRequestConfig.setJEffectDate(task.getString("jEffectDate"));
            friendRequestConfig.setJEffectTime(task.getString("jEffectTime"));
            friendRequestConfig.setBEnable(friendRequestTask.getBEnable());
            friendRequestConfigService.save(friendRequestConfig);
        }
    }

    /**
     * 导入保存
     *
     * @param friendReqImportParam
     */
    public void importSave(FriendReqImportParam friendReqImportParam) {

        FriendRequestTask friendRequestTask = getById(friendReqImportParam.getObjectid());

        ArrayList<Integer> arrTagId = new ArrayList<>();

        if (StrUtil.isNotEmpty(friendReqImportParam.getTags())) {
            String[] arrTag = friendReqImportParam.getTags().trim().split("\n");
            for (String tag : arrTag) {
                if (StrUtil.isNotEmpty(tag.trim())) {
                    QueryWrapper<WeChatFriendLabelLib> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("FriendRequestTaskId", friendReqImportParam.getObjectid());
                    queryWrapper.eq("sName", tag);
                    WeChatFriendLabelLib tagLib = weChatFriendLabelLibService.getOne(queryWrapper);
                    if (tagLib == null) {
                        tagLib = new WeChatFriendLabelLib();
                        tagLib.setSName(tag);
                        tagLib.setFriendRequestTaskId(friendReqImportParam.getObjectid());
                        tagLib.setValueTypeId("Bool");
                        tagLib.setBSys(0);
                        tagLib.setBLeaf(1);
                        weChatFriendLabelLibService.save(tagLib);
                    }

                    arrTagId.add(tagLib.getLId());
                }
            }
        }

        ArrayList<String> listPhone = new ArrayList<>();

        if (StrUtil.isNotEmpty(friendReqImportParam.getPhone())) {
            String[] arrPhone = friendReqImportParam.getPhone().trim().split("\n");
            listPhone.addAll(Arrays.asList(arrPhone));
        }

        if (StrUtil.isNotEmpty(friendReqImportParam.getFile())) {
            BufferedReader reader = null;

            try {
                reader = new BufferedReader(new FileReader(businessconfig.getUploadsavepath() + "/tmp/" + friendReqImportParam.getFile()));
                String line;
                while ((line = reader.readLine()) != null) {
                    listPhone.add(line);
                }
            } catch (IOException e) {
                throw new SystemException(10001L, e.getMessage());
            } finally {
                try {
                    if (reader != null) {
                        reader.close(); // 关闭文件读取器
                    }

                    File fileToDelete = new File(businessconfig.getUploadsavepath() + "/tmp/" + friendReqImportParam.getFile());
                    if (!fileToDelete.delete()) {
                        throw new SystemException(10001L, "上传的临时文件删除失败");
                    }
                } catch (IOException e) {
                    throw new SystemException(10001L, e.getMessage());
                }
            }
        }

        SysUser currUser = null;
        try {
            currUser = getCurrUser();
            if (currUser == null) {
                currUser = sysUserService.getById(friendRequestTask.getNewUserId());
            }
        } catch (SystemException e) {
            currUser = sysUserService.getById(friendRequestTask.getNewUserId());
        }

        for (String sPhone : listPhone) {
            sPhone = StrUtil.trim(sPhone);
            if (StrUtil.isNotEmpty(sPhone)) {
                QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("FriendRequestTaskId", friendReqImportParam.getObjectid());
                queryWrapper.eq("sPhoneOrWechatId", sPhone);
                FriendRequestTaskDetail friendRequestTaskDetail = friendRequestTaskDetailService.getOne(queryWrapper);

                boolean isNew = false;
                if (friendRequestTaskDetail == null) {
                    friendRequestTaskDetail = new FriendRequestTaskDetail();
                    friendRequestTaskDetail.setFriendRequestTaskId(friendReqImportParam.getObjectid());
                    friendRequestTaskDetail.setSPhoneOrWechatId(sPhone);
                    friendRequestTaskDetail.setStatusId(0);
                    friendRequestTaskDetail.setNewUserId(currUser.getLID());

                    isNew = true;
                }

                friendRequestTaskDetail.setDNewTime(LocalDateTime.now());//后进入的先加好友

                friendRequestTaskDetailService.saveOrUpdate(friendRequestTaskDetail);

                if (isNew && friendRequestTask.getIncomeTypeId() == 1) {
                    //兼职人员的表单收益
                    UserIncome userIncome = new UserIncome();
                    userIncome.setSName("导入表单：" + friendRequestTaskDetail.getSPhoneOrWechatId());
                    userIncome.setFIncome(friendRequestTask.getFIncome());
                    userIncome.setDNewTime(LocalDateTime.now());
                    userIncome.setSObjectName("Business/FriendRequestTaskDetail");
                    userIncome.setObjectId(friendRequestTaskDetail.getLId());
                    userIncomeService.save(userIncome);
                }

                for (Integer tagId : arrTagId) {
                    QueryWrapper<WeChatFriendLabel> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("FriendRequestTaskDetailId", friendRequestTaskDetail.getLId());
                    queryWrapper1.eq("LabelId", tagId);
                    if (weChatFriendLabelService.count(queryWrapper1) == 0) {
                        WeChatFriendLabel weChatFriendLabel = new WeChatFriendLabel();
                        weChatFriendLabel.setFriendRequestTaskId(friendReqImportParam.getObjectid());
                        weChatFriendLabel.setFriendRequestTaskDetailId(friendRequestTaskDetail.getLId());
                        weChatFriendLabel.setWeChatFriendId(friendRequestTaskDetail.getWeChatId());
                        weChatFriendLabel.setLabelId(tagId);
                        weChatFriendLabel.setSValue("1");
                        weChatFriendLabelService.save(weChatFriendLabel);
                    }
                }
            }
        }

        updateStatus(friendReqImportParam.getObjectid());

        EventParam eventParam = new EventParam();
        eventParam.setEvent("friendreqtaskimport");
        eventParam.setName("加友计划导入客户资料");
        eventParam.setPath("/ckbservice/friendrequesttask/import");
        eventParam.setParam("{\"taskid\":" + friendReqImportParam.getObjectid() + "}");
        eventApi.triggle(eventParam);
    }

    public void savePhone(Integer id, String phone, HashMap<Integer, String> mapLabel) {
        QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.eq("sPhoneOrWechatId", phone);
        FriendRequestTaskDetail friendRequestTaskDetail = friendRequestTaskDetailService.getOne(queryWrapper);
        if (friendRequestTaskDetail == null) {
            friendRequestTaskDetail = new FriendRequestTaskDetail();
            friendRequestTaskDetail.setFriendRequestTaskId(id);
            friendRequestTaskDetail.setSPhoneOrWechatId(phone);
            friendRequestTaskDetail.setDNewTime(LocalDateTime.now());
            friendRequestTaskDetail.setStatusId(0);
            friendRequestTaskDetailService.save(friendRequestTaskDetail);

            EventParam eventParam = new EventParam();
            eventParam.setEvent("friendreqtask-getphone-fromfrontend-once");
            eventParam.setName("首次获得手机号");
            eventParam.setPath("/ckbservice/friendrequesttask/getphone/fromfrontend/once");
            eventParam.setParam("{\"taskdetailid\":" + friendRequestTaskDetail.getLId() + ",\"taskid\":" + friendRequestTaskDetail.getFriendRequestTaskId() + ",\"action\":\"once\"}");
            eventApi.triggle(eventParam);
        } else {
            EventParam eventParam = new EventParam();
            eventParam.setEvent("friendreqtask-getphone-fromfrontend-repeat");
            eventParam.setName("重复获得手机号");
            eventParam.setPath("/ckbservice/friendrequesttask/getphone/fromfrontend/repeat");
            eventParam.setParam("{\"taskdetailid\":" + friendRequestTaskDetail.getLId() + ",\"taskid\":" + friendRequestTaskDetail.getFriendRequestTaskId() + ",\"action\":\"repeat\"}");
            eventApi.triggle(eventParam);
        }

        if (mapLabel != null) {
            QueryWrapper<WeChatFriendLabel> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("FriendRequestTaskDetailId", friendRequestTaskDetail.getLId());
            weChatFriendLabelService.remove(queryWrapper1);

            Set<Integer> labelIdSet = mapLabel.keySet();
            for (Integer labelId : labelIdSet) {
                WeChatFriendLabel weChatFriendLabel = new WeChatFriendLabel();
                weChatFriendLabel.setFriendRequestTaskId(id);
                weChatFriendLabel.setFriendRequestTaskDetailId(friendRequestTaskDetail.getLId());
                weChatFriendLabel.setWeChatFriendId(friendRequestTaskDetail.getWeChatFriendId());
                weChatFriendLabel.setLabelId(labelId);
                weChatFriendLabel.setSValue(mapLabel.get(labelId));
                weChatFriendLabelService.save(weChatFriendLabel);
            }
        }
    }

    /**
     * 由前端保存手机号
     */
    public void savePhoneFromFrontend(Integer id, String sPhone) {
        savePhone(id, sPhone, null);
    }

    /**
     * 上传导入的文件
     *
     * @param file
     * @param id
     */
    public String importUpload(MultipartFile file, Integer id) {
        String sFileNameExt = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
        if (!sFileNameExt.equals("txt")) {
            throw new SystemException(10001L, "您上传的文件非法，请重新选择。");
        }

        String sFileName = randomNumbers(32) + "." + sFileNameExt.toLowerCase(Locale.ROOT);
        String sFullDirPath = businessconfig.getUploadsavepath() + "/tmp";

        //创建文件夹
        File saveDir = new File(sFullDirPath);
        if (!saveDir.exists() && !saveDir.mkdirs()) {
            throw new SystemException(10001L, sFullDirPath + "创建失败");
        }

        //保存文件
        File dest = new File(sFullDirPath + "/" + sFileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new SystemException(10001L, "文件上传失败：" + e.getMessage());
        }

        return sFileName;
    }

    /**
     * 计算下一次执行时间
     */
    public void caluNextExec(Integer id) {
        FriendRequestTask friendRequestTask = getById(id);

        if (friendRequestTask.getDLastExecTime() == null) {
            friendRequestTask.setDNextExecTime(LocalDateTime.now());
        } else {
            // 创建一个随机数生成器
            Random random = new Random();

            // 生成随机索引
            int randomSecond = random.nextInt(60);

            //计算下次执行时间
            friendRequestTask.setDNextExecTime(friendRequestTask.getDLastExecTime().plusMinutes(friendRequestTask.getLRequestInterval()).plusSeconds(randomSecond));
        }

        updateById(friendRequestTask);
    }

    /**
     * 检查是否已通过好友的申请
     */
    public synchronized void checkIsPass() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("ispass", true);
        mapParam.put("keyword", "");
        mapParam.put("accountid", "");
        mapParam.put("page", 0);
        mapParam.put("pagesize", 30);
        mapParam.put("preFriendId", "");

        try {
            JSONArray arrFriend = customerTouchApi.getWechatFriendList(mapParam);
            for (int i = 0; i < arrFriend.size(); i++) {
                JSONObject friend = arrFriend.getJSONObject(i);

                //好友入库，这样可以保持动态更新
                WeChatFriend weChatFriend = weChatFriendService.saveWechatFriend(friend);
                weChatFriendService.saveOrUpdate(weChatFriend);

                String wechatId = friend.getString("wechatId");
                String alias = friend.getString("alias");
                String phone = friend.getString("phone");

                ArrayList<String> listCond = new ArrayList<>();
                if (StrUtil.isNotEmpty(phone)) {
                    String[] split = phone.split("，");
                    listCond.add(split[0]);
                }

                if (StrUtil.isNotEmpty(wechatId)) {
                    listCond.add(wechatId);
                }

                if (StrUtil.isNotEmpty(alias)) {
                    listCond.add(alias);
                }

                QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
                queryWrapper.in("sPhoneOrWechatId", listCond);
                queryWrapper.eq("WeChatId", friend.getIntValue("wechatAccountId"));
                queryWrapper.eq("StatusId", 4);//确保是通过存客宝发送请求添加的
                queryWrapper.orderByDesc("dSendTime");//查找最新的一条
                queryWrapper.last("LIMIT 1");
                FriendRequestTaskDetail requestTaskDetail = friendRequestTaskDetailService.getOne(queryWrapper);
                if (requestTaskDetail != null) {//说明匹配到了申请好友的任务
                    requestTaskDetail.setStatusId(2);
                    requestTaskDetail.setWeChatFriendId(friend.getIntValue("id"));
                    requestTaskDetail.setDPassedTime(LocalDateTimeUtil.parse(friend.getString("createTime")));
                    friendRequestTaskDetailService.updateById(requestTaskDetail);

                    EventParam eventParam = new EventParam();
                    eventParam.setEvent("friendreqtask-addfriend-req-pass");
                    eventParam.setName("申请已通过为好友");
                    eventParam.setPath("/ckbservice/friendrequesttask/addfriend/req/pass");
                    eventParam.setParam("{\"taskdetailid\":" + requestTaskDetail.getLId() + ",\"taskid\":" + requestTaskDetail.getFriendRequestTaskId() + ",\"action\":\"pass\"}");
                    eventApi.triggle(eventParam);
                }
            }
        } catch (SystemException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理用户收益录入
     * @param param
     */
    public void handleUserIncome(JSONObject param) {
        FriendRequestTaskDetail friendRequestTaskDetail = friendRequestTaskDetailService.getById(param.getIntValue("taskdetailid"));
        FriendRequestTask friendRequestTask = getById(param.getIntValue("taskid"));

        SysUser newUser = sysUserService.getById(friendRequestTaskDetail.getNewUserId());
        if (newUser != null && friendRequestTask.getIncomeTypeId() == 2 && newUser.getSysRoleId() == 6) {
            //兼职人员的表单收益
            UserIncome userIncome = new UserIncome();
            userIncome.setSName("好友通过：" + friendRequestTaskDetail.getSPhoneOrWechatId());
            userIncome.setFIncome(friendRequestTask.getFIncome());
            userIncome.setDNewTime(LocalDateTime.now());
            userIncome.setSObjectName("Business/FriendRequestTaskDetail");
            userIncome.setObjectId(friendRequestTaskDetail.getLId());
            userIncomeService.save(userIncome);
        }
    }

    /**
     * 更新加友计划的状态
     */
    @Transactional
    public synchronized void updateTaskRemoteStatus() {
        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("pageIndex", 0);
        mapParam.put("pageSize", 30);
        mapParam.put("keyword", "");
        mapParam.put("status", "");

        JSONArray addFriendByPhoneList = customerTouchApi.getAddFriendByPhoneList(mapParam);
        for (int i = 0; i < addFriendByPhoneList.size(); i++) {
            JSONObject jsonObject = addFriendByPhoneList.getJSONObject(i);
            Integer wechatAccountId = jsonObject.getIntValue("wechatAccountId");
            String phone = jsonObject.getString("phone");
            int status = jsonObject.getIntValue("status");

            if (status > 0) {
                QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("sPhoneOrWechatId", phone);
                queryWrapper.eq("WeChatId", wechatAccountId);
                queryWrapper.eq("StatusId", 1);
                List<FriendRequestTaskDetail> friendRequestTaskDetails = friendRequestTaskDetailService.list(queryWrapper);
                for (FriendRequestTaskDetail detail : friendRequestTaskDetails) {
                    //执行成功
                    if (status == 1) {
                        detail.setStatusId(4);//请求成功
                        detail.setThirdTaskId(jsonObject.getIntValue("id"));
                        detail.setDSuccessTime(LocalDateTime.now());

                        EventParam eventParam = new EventParam();
                        eventParam.setEvent("friendreqtask-addfriend-req-success");
                        eventParam.setName("添加好友申请发送成功");
                        eventParam.setPath("/ckbservice/friendrequesttask/addfriend/req/success");
                        eventParam.setParam("{\"taskdetailid\":" + detail.getLId() + ",\"taskid\":" + detail.getFriendRequestTaskId() + "}");
                        eventApi.triggle(eventParam);
                    } else if (status == 2) {
                        detail.setStatusId(3);//请求失败
                        detail.setThirdTaskId(jsonObject.getIntValue("id"));
                        detail.setSFailMessage(jsonObject.getString("extra") == null ? "无失败原因" : jsonObject.getString("extra"));
                        detail.setDFailedTime(LocalDateTime.now());

                        EventParam eventParam = new EventParam();
                        eventParam.setEvent("friendreqtask-addfriend-req-fail");
                        eventParam.setName("添加好友申请发送失败");
                        eventParam.setPath("/ckbservice/friendrequesttask/addfriend/req/fail");
                        eventParam.setParam("{\"taskdetailid\":" + detail.getLId() + ",\"taskid\":" + detail.getFriendRequestTaskId() + "}");
                        eventApi.triggle(eventParam);
                    }
                }

                friendRequestTaskDetailService.updateBatchById(friendRequestTaskDetails);
            }

        }
    }

    /**
     * 开始执行加友的计划
     */
    public synchronized void execReq() {

        //查询出所有的系统标签，为了后面排除系统标签做备注而准备
        ArrayList<Integer> listSysLableId = new ArrayList<>();
        QueryWrapper<WeChatFriendLabelLib> labelLibQueryWrapper = new QueryWrapper<>();
        labelLibQueryWrapper.eq("bSys", 1);
        List<WeChatFriendLabelLib> labelLibs = weChatFriendLabelLibService.list(labelLibQueryWrapper);
        for (WeChatFriendLabelLib labelLib : labelLibs) {
            listSysLableId.add(labelLib.getLId());
        }

        QueryWrapper<FriendRequestTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("dNextExecTime", LocalDateTime.now());
        queryWrapper.eq("bEnable", 1);
        queryWrapper.gt("lNoExecAmt", 0);
        List<FriendRequestTask> listTask = list(queryWrapper);
        for (FriendRequestTask friendRequestTask : listTask) {

            if (!isCurrentTimeWithinRange(friendRequestTask.getSPeriod())) {
                continue;//还没到达时间
            }

            List<String> listWechatId = StrUtil.splitTrim(friendRequestTask.getWeChatId(), ";");
            listWechatId.add("-1");

            List<WeChat> weChatList = weChatService.listByIds(listWechatId);
            weChatList.removeIf(weChat -> !weChatService.canReq(weChat));//剔除不能加好友的客服号：在线且符合风控规则
            if (weChatList.size() == 0) {//无号可用
                continue;
            }

            /**
             * 这段查询今天发了多少条，不能超过限制
             */

            // 获取今天的日期
            LocalDate today = LocalDate.now();

            // 获取今天的开始时间（00:00:00）
            LocalTime startOfDay = LocalTime.MIN;

            // 获取今天的结束时间（23:59:59.999999999）
            LocalTime endOfDay = LocalTime.MAX;

            for (WeChat weChat : weChatList) {
                QueryWrapper<FriendRequestTaskDetail> detailQueryWrapper = new QueryWrapper<>();
                detailQueryWrapper.eq("FriendRequestTaskId", friendRequestTask.getLId());
                detailQueryWrapper.eq("WeChatId", weChat.getLId());
                detailQueryWrapper.ge("dSendTime", LocalDateTime.of(today, startOfDay));
                detailQueryWrapper.le("dSendTime", LocalDateTime.of(today, endOfDay));
                int count = friendRequestTaskDetailService.count(detailQueryWrapper);
                if (count >= friendRequestTask.getLMaxPerDayRequestAmt()) {//超过一天的限制了
                    continue;
                }

                while (true) {
                    QueryWrapper<FriendRequestTaskDetail> queryWrapper1 = new QueryWrapper<>();
                    queryWrapper1.eq("FriendRequestTaskId", friendRequestTask.getLId());
                    queryWrapper1.eq("StatusId", 0);
                    queryWrapper1.orderByDesc("dNewTime");
                    queryWrapper1.last("LIMIT 1");
                    FriendRequestTaskDetail req = friendRequestTaskDetailService.getOne(queryWrapper1);
                    if (req == null) {
                        break;//任务里已经没有可加的好友了
                    }

                    //判断是否好友
                    JSONObject friend = weChatService.getFriendByPhone(weChat.getLId(), req.getSPhoneOrWechatId());

                    //控制一下速度，不然会太频繁
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (friend != null) {//是他的好友
                        req.setStatusId(6);//二次申请
                        req.setWeChatId(weChat.getLId());
                        req.setWeChatFriendId(friend.getIntValue("id"));
                        req.setDSendTime(LocalDateTime.now());
                        friendRequestTaskDetailService.updateById(req);

                        EventParam eventParam = new EventParam();
                        eventParam.setEvent("friendreqtask-addfriend-isfriend");
                        eventParam.setName("已是好友");
                        eventParam.setPath("/ckbservice/friendrequesttask/addfriend/isfriend");
                        eventParam.setParam("{\"taskdetailid\":" + req.getLId() + ",\"taskid\":" + friendRequestTask.getLId() + ",\"action\":\"isfriend\"}");
                        eventApi.triggle(eventParam);

                        //这里没有实际请求申请好友，所以要跳过
                    } else {//不是好友，那就要发送好友的申请
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("message", friendRequestTask.getSHello());
                        jsonObject.put("phone", req.getSPhoneOrWechatId());
                        jsonObject.put("wechatAccountId", weChat.getLId());

                        //打标签
                        JSONArray labels = new JSONArray();

                        QueryWrapper<WeChatFriendLabel> labelQueryWrapper = new QueryWrapper<>();
                        labelQueryWrapper.select("LabelId", "sValue");
                        labelQueryWrapper.eq("FriendRequestTaskDetailId", req.getLId());
                        labelQueryWrapper.notIn("LabelId", listSysLableId);
                        List<WeChatFriendLabel> friendLabels = weChatFriendLabelService.list(labelQueryWrapper);
                        for (WeChatFriendLabel friendLabel : friendLabels) {
                            WeChatFriendLabelLib labelLib = weChatFriendLabelLibService.getById(friendLabel.getLabelId());
                            switch (labelLib.getValueTypeId()) {
                                case "Bool":
                                    labels.add(labelLib.getSName());
                                    break;
                                case "Num":
                                case "Date":
                                case "Text":
                                    labels.add(labelLib.getSName() + ":" + friendLabel.getSValue());
                                    break;
                                case "List":
                                    //暂不实现
                                    break;
                            }
                        }
                        jsonObject.put("labels", labels);

                        //备注
                        StringBuilder sRemark = new StringBuilder();
                        sRemark.append(friendRequestTask.getSTheme());
                        sRemark.append("(");
                        sRemark.append(req.getSPhoneOrWechatId());
                        if (friendRequestTask.getMemoTypeId() == 2) {//自定义备注
                            //自定义备注格式：主题词(手机号+参数1+。。。+参数n)
                            String[] split = friendRequestTask.getMemoParamId().split(",");
                            if (split.length > 0) {
                                labelQueryWrapper = new QueryWrapper<>();
                                labelQueryWrapper.select("LabelId", "sValue");
                                labelQueryWrapper.eq("FriendRequestTaskDetailId", req.getLId());
                                labelQueryWrapper.in("LabelId", split);
                                friendLabels = weChatFriendLabelService.list(labelQueryWrapper);

                                for (String sId : split) {
                                    for (WeChatFriendLabel friendLabel : friendLabels) {
                                        if (friendLabel.getLabelId() == Integer.parseInt(sId)) {
                                            WeChatFriendLabelLib labelLib = weChatFriendLabelLibService.getById(friendLabel.getLabelId());
                                            switch (labelLib.getValueTypeId()) {
                                                case "Bool":
                                                    sRemark.append(labelLib.getSName());
                                                    sRemark.append("+");
                                                    break;
                                                case "Num":
                                                case "Date":
                                                case "Text":
                                                    sRemark.append(friendLabel.getSValue());
                                                    sRemark.append("+");
                                                    break;
                                                case "List":
                                                    //暂不实现
                                                    break;
                                            }
                                        }
                                    }
                                }
                            }

                            // 检查StringBuilder不为空并且最后一个字符是+
                            int lastIndex = sRemark.length() - 1;
                            if (lastIndex >= 0 && sRemark.charAt(lastIndex) == '+') {
                                // 删除最后一个字符
                                sRemark.deleteCharAt(lastIndex);
                            }
                        }

                        sRemark.append(")");
                        jsonObject.put("remark", sRemark.toString());

                        try {
                            customerTouchApi.addFriend(jsonObject);//调用触客端进行好友添加
                        } catch (SystemException e) {
                            if (e.getErrorCode() == 11000L) {//系统限制
                                logger.error(e.getMessage());//系统限制，不做处理，只提示。
                            } else {
                                req.setStatusId(3);//请求失败
                                req.setSFailMessage(e.getMessage());
                                req.setDFailedTime(LocalDateTime.now());
                                friendRequestTaskDetailService.updateById(req);

                                EventParam eventParam = new EventParam();
                                eventParam.setEvent("friendreqtask-addfriend-req-fail");
                                eventParam.setName("添加好友申请发送失败");
                                eventParam.setPath("/ckbservice/friendrequesttask/addfriend/req/fail");
                                eventParam.setParam("{\"taskdetailid\":" + req.getLId() + ",\"taskid\":" + req.getFriendRequestTaskId() + "}");
                                eventApi.triggle(eventParam);
                            }

                            continue;
                        }

                        req.setStatusId(1);
                        req.setWeChatId(weChat.getLId());
                        req.setDSendTime(LocalDateTime.now());
                        friendRequestTaskDetailService.updateById(req);

                        friendRequestTask.setDLastExecTime(LocalDateTime.now());
                        updateById(friendRequestTask);

                        EventParam eventParam = new EventParam();
                        eventParam.setEvent("friendreqtask-addfriend-send");
                        eventParam.setName("申请添加好友");
                        eventParam.setPath("/ckbservice/friendrequesttask/addfriend/send");
                        eventParam.setParam("{\"taskdetailid\":" + req.getLId() + ",\"taskid\":" + friendRequestTask.getLId() + ",\"action\":\"once\"}");
                        eventApi.triggle(eventParam);

                        break;
                    }
                }
            }
        }
    }

    private void saveToFriendMessageTask() {

    }

    /**
     * 处理好友通过的事件
     * 这里根据私信任务的配置生成每条私信的数据，给计划任务执行
     *
     * @param param
     */
    public void handleAddFriendReqPass(JSONObject param) {
        FriendRequestTaskDetail requestTaskDetail = friendRequestTaskDetailService.getById(param.getIntValue("taskdetailid"));
        FriendRequestTask requestTask = getById(param.getIntValue("taskid"));

        QueryWrapper<FriendRequestConfig> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("FriendRequestTaskId", param.getIntValue("taskid"));
        objectQueryWrapper.eq("TypeId", 1);
        List<FriendRequestConfig> friendRequestConfigList = friendRequestConfigService.list(objectQueryWrapper);

        LocalDateTime dStart = LocalDateTime.now();
        for (FriendRequestConfig friendRequestConfig : friendRequestConfigList) {
            JSONArray arrMessage = JSONArray.parseArray(friendRequestConfig.getJMessage());
            for (int i = 0; i < arrMessage.size(); i++) {
                JSONObject message = arrMessage.getJSONObject(i);

                dStart = dStart.plusSeconds(message.getIntValue("interval"));

                FriendMessageTask friendMessageTask = new FriendMessageTask();
                friendMessageTask.setSName(friendRequestConfig.getSName());
                friendMessageTask.setSUUId(friendRequestConfig.getSUUId());
                friendMessageTask.setFriendRequestTaskId(requestTask.getLId());
                friendMessageTask.setFriendRequestTaskDetailId(requestTaskDetail.getLId());
                friendMessageTask.setDNewTime(LocalDateTime.now());
                friendMessageTask.setDSendTimePlan(dStart);
                friendMessageTask.setTypeId(1);
                friendMessageTask.setWeChatId(requestTaskDetail.getWeChatId());
                friendMessageTask.setWeChatFriendId(requestTaskDetail.getWeChatFriendId());
                friendMessageTask.setJMessage(message.toString());
                friendMessageTask.setStatusId(0);

                friendMessageTaskService.save(friendMessageTask);
            }
        }

        //把标签的归属好友ID赋值
        UpdateWrapper<WeChatFriendLabel> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("FriendRequestTaskDetailId", param.getIntValue("taskdetailid"));
        objectUpdateWrapper.set("WeChatFriendId", requestTaskDetail.getWeChatFriendId());
        weChatFriendLabelService.update(objectUpdateWrapper);
    }

    public void sendMessage(FriendMessageTask friendMessageTask) {

        if (friendMessageTask.getStatusId() != 0) {
            return;
        }

        Map<String, Object> mapParam = new HashMap<>();
        mapParam.put("accountid", friendMessageTask.getWeChatId());
        mapParam.put("receiver", friendMessageTask.getWeChatFriendId());

        JSONObject jMessage = JSONObject.parseObject(friendMessageTask.getJMessage());
        switch (jMessage.getIntValue("type")) {
            case 1 -> {//文本
                mapParam.put("msgtype", 1);
                mapParam.put("content", jMessage.getString("content"));
                customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tofriend", mapParam);
            }
            case 2 -> {//图片
                mapParam.put("msgtype", 3);
                mapParam.put("content", sysAttachService.getCdnUrl(jMessage.getIntValue("attachid")));
                customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tofriend", mapParam);
            }
            case 3 -> {//视频
                mapParam.put("msgtype", 43);
                mapParam.put("content", sysAttachService.getCdnUrl(jMessage.getIntValue("attachid")));
                customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tofriend", mapParam);
            }
            case 4 -> {//文件
                mapParam.put("msgtype", 49);

                JSONArray files = jMessage.getJSONArray("files");
                if (files != null) {
                    for (int i = 0; i < files.size(); i++) {
                        JSONObject file = files.getJSONObject(i);
                        JSONObject content = new JSONObject();
                        content.put("type", "file");
                        content.put("title", file.getString("name"));
                        content.put("url", sysAttachService.getCdnUrl(file.getIntValue("id")));
                        mapParam.put("content", content.toJSONString());
                        customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tofriend", mapParam);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            case 5 -> {//小程序
                mapParam.put("msgtype", 49);
                MiniProgrameLib miniProgrameLib = miniProgrameLibService.getById(jMessage.getJSONObject("mp").getIntValue("id"));
                JSONObject jsonObject = JSONObject.parseObject(miniProgrameLib.getSContent());
                mapParam.put("content", jsonObject.toJSONString());
                customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tofriend", mapParam);
            }
            case 6 -> {//链接
                mapParam.put("msgtype", 49);

                JSONObject link = jMessage.getJSONObject("link");

                JSONObject content = new JSONObject();
                content.put("title", link.getString("title"));
                content.put("desc", link.getString("desc"));
                content.put("type", "link");
                content.put("url", link.getString("url"));
                content.put("thumbPath", sysAttachService.getCdnUrl(link.getIntValue("attachid")));
                mapParam.put("content", content.toJSONString());
                customerTouchApi.post("/v1/customertouch/wechat/sendmessage/tofriend", mapParam);
            }
            case 10 -> {//邀请入群
                JSONArray chatrooms = jMessage.getJSONArray("chatroomid");
                for (int i = 0; i < chatrooms.size(); i++) {
                    Integer chatroomid = chatrooms.getIntValue(i);

                    mapParam = new HashMap<>();
                    mapParam.put("wechatChatroomId", chatroomid);

                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(friendMessageTask.getWeChatFriendId());
                    mapParam.put("wechatFriendIds", jsonArray);
                    customerTouchApi.post("/v1/customertouch/wechat/chatroom/invite", mapParam);
                }
            }
        }

        friendMessageTask.setDSendTime(LocalDateTime.now());
        friendMessageTask.setStatusId(1);
        friendMessageTaskService.updateById(friendMessageTask);
    }

    /**
     * 根据消息的任务，发送消息给好友
     */
    public synchronized void sendMessage() {
        QueryWrapper<FriendMessageTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("StatusId", 0);
        queryWrapper.lt("dSendTimePlan", LocalDateTime.now());
        List<FriendMessageTask> friendMessageTasks = friendMessageTaskService.list(queryWrapper);
        for (FriendMessageTask friendMessageTask : friendMessageTasks) {
            sendMessage(friendMessageTask);
        }
    }

    /**
     * 主动私发给通过的好友
     */
    public synchronized void sendMessageToFriend() {
        QueryWrapper<FriendRequestConfig> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("bEnable", 1);
        objectQueryWrapper.eq("TypeId", 2);
        List<FriendRequestConfig> requestConfigList = friendRequestConfigService.list(objectQueryWrapper);
        for (FriendRequestConfig friendRequestConfig : requestConfigList) {
            JSONObject jTime = JSONObject.parseObject(friendRequestConfig.getJEffectTime());
            JSONObject jDate = JSONObject.parseObject(friendRequestConfig.getJEffectDate());

            LocalTime currentTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
            LocalTime targetTime = LocalTime.parse(jTime.getString("time")).truncatedTo(ChronoUnit.MINUTES);
            if (currentTime.equals(targetTime)) {//只比较分钟，不比较秒和毫秒
                List<FriendRequestTaskDetail> requestTaskDetails = null;
                if (jDate.getIntValue("type") == 2) {//指定时间发送
                    LocalDate currDate = LocalDate.now();
                    LocalDate targetDate = LocalDate.parse(jDate.getString("date"));
                    if (currDate.isEqual(targetDate)) {
                        QueryWrapper<FriendRequestTaskDetail> detailQueryWrapper = new QueryWrapper<>();
                        detailQueryWrapper.eq("FriendRequestTaskId", friendRequestConfig.getFriendRequestTaskId());
                        detailQueryWrapper.eq("StatusId", 2);
                        detailQueryWrapper.notIn("lId", "SELECT FriendRequestTaskDetailId FROM FriendMessageTask WHERE sUUId='" + friendRequestConfig.getSUUId() + "'");
                        requestTaskDetails = friendRequestTaskDetailService.list(detailQueryWrapper);
                    }
                } else {//固定时间之后
                    //查找通过时间之后X天的微信好友，且没有
                    QueryWrapper<FriendRequestTaskDetail> detailQueryWrapper = new QueryWrapper<>();
                    detailQueryWrapper.eq("FriendRequestTaskId", friendRequestConfig.getFriendRequestTaskId());
                    detailQueryWrapper.eq("StatusId", 2);

                    LocalDateTime startOfDay = LocalDate.now().minusDays(jDate.getIntValue("num")).atStartOfDay();
                    detailQueryWrapper.ge("dPassedTime", startOfDay);

                    LocalDateTime endOfDay = LocalDate.now().minusDays(jDate.getIntValue("num")).atTime(23, 59, 59);
                    detailQueryWrapper.le("dPassedTime", endOfDay);

                    detailQueryWrapper.notIn("lId", "SELECT FriendRequestTaskDetailId FROM FriendMessageTask WHERE sUUId='" + friendRequestConfig.getSUUId() + "'");
                    requestTaskDetails = friendRequestTaskDetailService.list(detailQueryWrapper);
                }
                JSONArray arrCond = JSONArray.parseArray(friendRequestConfig.getJCond());
                JSONArray arrMessage = JSONArray.parseArray(friendRequestConfig.getJMessage());

                if (requestTaskDetails != null) {
                    LocalDateTime dStart = LocalDateTime.now();
                    for (FriendRequestTaskDetail requestTaskDetail : requestTaskDetails) {
                        if (arrCond.size() > 0) {
                            boolean b = true;//是否符合条件
                            for (int i = 0; i < arrCond.size(); i++) {
                                JSONObject jCond = arrCond.getJSONObject(i);

                                JSONArray jsonArray = jCond.getJSONArray("value");
                                Collection<String> collection = new ArrayList<>();
                                for (int j = 0; j < jsonArray.size(); j++) {
                                    collection.add(jsonArray.getString(i));
                                }
                                collection.add("-------------------");

                                QueryWrapper<WeChatFriendLabel> labelQueryWrapper = new QueryWrapper<>();
                                labelQueryWrapper.eq("FriendRequestTaskDetailId", requestTaskDetail.getLId());
                                labelQueryWrapper.eq("LabelId", jCond.getIntValue("tagid"));
                                labelQueryWrapper.in("sValue", collection);
                                if (weChatFriendLabelService.count(labelQueryWrapper) == 0) {
                                    b = false;
                                }
                            }

                            if (!b) {//不符合条件
                                continue;
                            }
                        }

                        //都符合条件，准备发信息
                        dStart = dStart.plusSeconds(2);//特意间隔2秒，这样才不会同一时间群发太多消息
                        for (int i = 0; i < arrMessage.size(); i++) {
                            JSONObject message = arrMessage.getJSONObject(i);

                            dStart = dStart.plusSeconds(message.getIntValue("interval"));

                            FriendMessageTask friendMessageTask = new FriendMessageTask();
                            friendMessageTask.setSName(friendRequestConfig.getSName());
                            friendMessageTask.setSUUId(friendRequestConfig.getSUUId());
                            friendMessageTask.setFriendRequestTaskId(requestTaskDetail.getFriendRequestTaskId());
                            friendMessageTask.setFriendRequestTaskDetailId(requestTaskDetail.getLId());
                            friendMessageTask.setDNewTime(LocalDateTime.now());
                            friendMessageTask.setDSendTimePlan(dStart);
                            friendMessageTask.setTypeId(1);
                            friendMessageTask.setWeChatId(requestTaskDetail.getWeChatId());
                            friendMessageTask.setWeChatFriendId(requestTaskDetail.getWeChatFriendId());
                            friendMessageTask.setJMessage(message.toString());
                            friendMessageTask.setStatusId(0);

                            friendMessageTaskService.save(friendMessageTask);
                        }
                    }
                }
            }
        }
    }

    /**
     * 统计数据
     */
    public JSONObject stats(Integer id, String time) {
        JSONArray arrTime = JSONArray.parseArray(time);

        String start = arrTime.getString(0);
        String end = arrTime.getString(1);

        JSONObject stats = new JSONObject();

        JSONObject global = new JSONObject();

        //总数
        QueryWrapper<FriendRequestTaskDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.ge("dNewTime", start + " 00:00:00");
        queryWrapper.le("dNewTime", end + " 23:59:59");
        global.put("total", friendRequestTaskDetailService.count(queryWrapper));

        //请求成功
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.eq("StatusId", 4);
        queryWrapper.ge("dSuccessTime", start + " 00:00:00");
        queryWrapper.le("dSuccessTime", end + " 23:59:59");
        global.put("success", friendRequestTaskDetailService.count(queryWrapper));

        //请求失败
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.eq("StatusId", 3);
        queryWrapper.ge("dFailedTime", start + " 00:00:00");
        queryWrapper.le("dFailedTime", end + " 23:59:59");
        global.put("fail", friendRequestTaskDetailService.count(queryWrapper));

        //通过好友
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("FriendRequestTaskId", id);
        queryWrapper.eq("StatusId", 2);
        queryWrapper.ge("dPassedTime", start + " 00:00:00");
        queryWrapper.le("dPassedTime", end + " 23:59:59");
        global.put("pass", friendRequestTaskDetailService.count(queryWrapper));

        //扫码次数
        QueryWrapper<FriendRequestTaskScan> friendRequestTaskScanQueryWrapper = new QueryWrapper<>();
        friendRequestTaskScanQueryWrapper.eq("FriendRequestTaskId", id);
        friendRequestTaskScanQueryWrapper.ge("dNewTime", start + " 00:00:00");
        friendRequestTaskScanQueryWrapper.le("dNewTime", end + " 23:59:59");
        global.put("scan", friendRequestTaskScanService.count(friendRequestTaskScanQueryWrapper));

        if (global.getIntValue("total") == 0) {
            global.put("successpercent", 0);
        } else {
            global.put("successpercent", NumberUtil.round(100 * (double) global.getIntValue("success") / (double) global.getIntValue("total") * 100, 2));
        }

        if (global.getIntValue("success") == 0) {
            global.put("passpercent", 0);
        } else {
            global.put("passpercent", NumberUtil.round(100 * (double) global.getIntValue("pass") / (double) global.getIntValue("success"), 2));
        }

        stats.put("global", global);
        stats.put("task", getById(id));


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate startDate = LocalDate.parse(start, formatter);
        LocalDate endDate = LocalDate.parse(end, formatter);

        ArrayList<String> shortdates = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        while (!startDate.isAfter(endDate)) {
            dates.add(startDate.format(formatter));
            shortdates.add(startDate.format(outputFormatter));
            startDate = startDate.plusDays(1);
        }

        JSONArray arrTableData = new JSONArray();
        for (String date : shortdates) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("date", date);
            arrTableData.add(jsonObject);
        }

        JSONArray series = new JSONArray();

        JSONObject serie = new JSONObject();
        serie.put("name", "总计");
        serie.put("type", "line");
        serie.put("stack", "Total");
        serie.put("smooth", true);

        ArrayList<Integer> totaldata = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("FriendRequestTaskId", id);
            queryWrapper.ge("dNewTime", dates.get(i) + " 00:00:00");
            queryWrapper.le("dNewTime", dates.get(i) + " 23:59:59");

            int count = friendRequestTaskDetailService.count(queryWrapper);
            totaldata.add(count);

            arrTableData.getJSONObject(i).put("total", count);
        }
        serie.put("data", totaldata);
        series.add(serie);

        serie = new JSONObject();
        serie.put("name", "扫码");
        serie.put("type", "line");
        serie.put("stack", "Total");
        serie.put("smooth", true);

        ArrayList<Integer> scandata = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {

            friendRequestTaskScanQueryWrapper = new QueryWrapper<>();
            friendRequestTaskScanQueryWrapper.eq("FriendRequestTaskId", id);
            friendRequestTaskScanQueryWrapper.ge("dNewTime", dates.get(i) + " 00:00:00");
            friendRequestTaskScanQueryWrapper.le("dNewTime", dates.get(i) + " 23:59:59");

            int count = friendRequestTaskScanService.count(friendRequestTaskScanQueryWrapper);
            scandata.add(count);

            arrTableData.getJSONObject(i).put("scan", count);
        }
        serie.put("data", scandata);
        series.add(serie);


        serie = new JSONObject();
        serie.put("name", "成功");
        serie.put("type", "line");
        serie.put("stack", "Total");
        serie.put("smooth", true);

        List<Integer> successdata = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("FriendRequestTaskId", id);
            queryWrapper.eq("StatusId", 4);
            queryWrapper.ge("dSuccessTime", dates.get(i) + " 00:00:00");
            queryWrapper.le("dSuccessTime", dates.get(i) + " 23:59:59");

            int count = friendRequestTaskDetailService.count(queryWrapper);
            successdata.add(count);

            arrTableData.getJSONObject(i).put("success", count);
        }
        serie.put("data", successdata);
        series.add(serie);

        serie = new JSONObject();
        serie.put("name", "失败");
        serie.put("type", "line");
        serie.put("stack", "Total");
        serie.put("smooth", true);

        ArrayList<Integer> data = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("FriendRequestTaskId", id);
            queryWrapper.eq("StatusId", 3);
            queryWrapper.ge("dFailedTime", dates.get(i) + " 00:00:00");
            queryWrapper.le("dFailedTime", dates.get(i) + " 23:59:59");

            int count = friendRequestTaskDetailService.count(queryWrapper);
            data.add(count);

            arrTableData.getJSONObject(i).put("fail", count);
        }
        serie.put("data", data);
        series.add(serie);

        serie = new JSONObject();
        serie.put("name", "通过");
        serie.put("type", "line");
        serie.put("stack", "Total");
        serie.put("smooth", true);

        ArrayList<Integer> passdata = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("FriendRequestTaskId", id);
            queryWrapper.eq("StatusId", 2);
            queryWrapper.ge("dPassedTime", dates.get(i) + " 00:00:00");
            queryWrapper.le("dPassedTime", dates.get(i) + " 23:59:59");

            int count = friendRequestTaskDetailService.count(queryWrapper);
            passdata.add(count);

            arrTableData.getJSONObject(i).put("pass", count);
        }
        serie.put("data", passdata);
        series.add(serie);

        serie = new JSONObject();
        serie.put("name", "成功率");
        serie.put("type", "line");
        serie.put("stack", "Total");
        serie.put("smooth", true);
        List<Double> successpercent = new ArrayList<>();
        for (int i = 0; i < totaldata.size(); i++) {
            if (totaldata.get(i) > 0) {
                Double total = totaldata.get(i).doubleValue();
                Double success = successdata.get(i).doubleValue();
                successpercent.add(100 * NumberUtil.round(success / total, 2).doubleValue());

                arrTableData.getJSONObject(i).put("successpercent", 100 * NumberUtil.round(success / total, 2).doubleValue());
            } else {
                successpercent.add((double) 0);
                arrTableData.getJSONObject(i).put("successpercent", 0);
            }
        }
        serie.put("data", successpercent);
        series.add(serie);

        serie = new JSONObject();
        serie.put("name", "通过率");
        serie.put("type", "line");
        serie.put("stack", "Total");
        serie.put("smooth", true);
        List<Double> passpercent = new ArrayList<>();
        for (int i = 0; i < totaldata.size(); i++) {
            if (successdata.get(i) > 0) {
                Double total = successdata.get(i).doubleValue();
                Double pass = passdata.get(i).doubleValue();
                passpercent.add(100 * NumberUtil.round(pass / total, 2).doubleValue());

                arrTableData.getJSONObject(i).put("passpercent", 100 * NumberUtil.round(pass / total, 2).doubleValue());
            } else {
                passpercent.add((double) 0);
                arrTableData.getJSONObject(i).put("passpercent", 0);
            }
        }
        serie.put("data", passpercent);
        series.add(serie);


        stats.put("series", series);
        stats.put("dates", shortdates);
        stats.put("arrTableData", arrTableData);


        return stats;
    }

    /**
     * 前端扫码海报计数
     *
     * @param id
     */
    public synchronized void scanCount(Integer id) {
        FriendRequestTaskScan friendRequestTaskScan = new FriendRequestTaskScan();
        friendRequestTaskScan.setFriendRequestTaskId(id);
        friendRequestTaskScan.setDNewTime(LocalDateTime.now());
        friendRequestTaskScanService.save(friendRequestTaskScan);

        UpdateWrapper<FriendRequestTask> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("lId", id);
        objectUpdateWrapper.setSql("lScanAmt=lScanAmt+1");
        update(objectUpdateWrapper);
    }

    public FriendRequestTask getByKey(String sKey) {
        QueryWrapper<FriendRequestTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sKey", sKey);
        queryWrapper.last("LIMIT 1");
        return getOne(queryWrapper);
    }

    /**
     * 处理打标签的事件响应
     *
     * @param param
     */
    public void handleMarkLabel(JSONObject param) {
        FriendRequestTaskDetail requestTaskDetail = friendRequestTaskDetailService.getById(param.getIntValue("taskdetailid"));
        FriendRequestTask requestTask = getById(param.getIntValue("taskid"));

        WechatFriendLabelParam wechatFriendLabelParam = new WechatFriendLabelParam();
        wechatFriendLabelParam.setFriendRequestTaskId(requestTask.getLId());
        wechatFriendLabelParam.setFriendRequestTaskDetailId(requestTaskDetail.getLId());
        wechatFriendLabelParam.setWeChatFriendId(requestTaskDetail.getWeChatFriendId());

        if (param.getString("action").equals("repeat")) {//多次触达
            wechatFriendLabelParam.setLabelId(1);
            wechatFriendLabelParam.setSValue("second");
        } else if (param.getString("action").equals("isfriend") || param.getString("action").equals("pass")) {//好友通过或已是好友
            wechatFriendLabelParam.setLabelId(1);
            wechatFriendLabelParam.setSValue("second");
        } else if (param.getString("action").equals("once")) {//首次触达
            wechatFriendLabelParam.setLabelId(1);
            wechatFriendLabelParam.setSValue("first");
        }

        weChatFriendLabelService.markLabel(wechatFriendLabelParam);
    }

    /**
     * 执行测试
     *
     * @param friendReqTaskParam
     */
    public void test(FriendReqTaskParam friendReqTaskParam) {
        JSONObject test = JSONObject.parseObject(friendReqTaskParam.getTest());

        if (test.getString("action").equals("req")) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", friendReqTaskParam.getSHello());
            jsonObject.put("phone", test.getJSONObject("req").getString("phone"));
            jsonObject.put("wechatAccountId", test.getJSONObject("req").getString("wechatid"));

            //备注
            String sRemark = friendReqTaskParam.getSTheme() +
                    "(" +
                    test.getJSONObject("req").getString("phone") +
                    ")";
            jsonObject.put("remark", sRemark);
            customerTouchApi.addFriend(jsonObject);
        } else {
            JSONArray arrFriendId = test.getJSONArray("reply");
            for (int k = 0; k < arrFriendId.size(); k++) {
                int friendId = arrFriendId.getIntValue(k);
                WeChatFriend weChatFriend = weChatFriendService.getById(friendId);

                LocalDateTime dStart = LocalDateTime.now();
                JSONArray arrTask = JSONArray.parseArray(friendReqTaskParam.getArrTask());
                for (int j = 0; j < arrTask.size(); j++) {
                    JSONObject task = arrTask.getJSONObject(j);
                    if (task.getIntValue("TypeId") == 1) {
                        JSONArray arrMessage = task.getJSONArray("jMessage");
                        for (int i = 0; i < arrMessage.size(); i++) {
                            JSONObject message = arrMessage.getJSONObject(i);

                            dStart = dStart.plusSeconds(message.getIntValue("interval"));

                            FriendMessageTask friendMessageTask = new FriendMessageTask();
                            friendMessageTask.setSName(task.getString("sName"));
                            friendMessageTask.setFriendRequestTaskId(friendReqTaskParam.getLId());
                            friendMessageTask.setDNewTime(LocalDateTime.now());
                            friendMessageTask.setDSendTimePlan(dStart);
                            friendMessageTask.setTypeId(1);
                            friendMessageTask.setWeChatId(weChatFriend.getWeChatId());
                            friendMessageTask.setWeChatFriendId(weChatFriend.getLId());
                            friendMessageTask.setJMessage(message.toString());
                            friendMessageTask.setStatusId(0);

                            friendMessageTaskService.save(friendMessageTask);
                        }
                    }
                }
            }
        }
    }

    /**
     * 打标签：好友交互频次
     */
    public void markFriendCommunicate() {

    }
}
