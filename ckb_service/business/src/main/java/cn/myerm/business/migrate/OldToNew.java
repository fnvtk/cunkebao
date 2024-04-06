package cn.myerm.business.migrate;

import cn.hutool.core.util.StrUtil;
import cn.myerm.business.entity.*;
import cn.myerm.business.service.impl.*;
import cn.myerm.system.entity.SysAttach;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.service.impl.SysAttachServiceImpl;
import cn.myerm.system.service.impl.SysUserServiceImpl;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OldToNew {

    private static final Logger logger = LoggerFactory.getLogger(OldToNew.class);

    private final JdbcTemplate jdbcTemplate;

    private final AccountServiceImpl accountService;
    private final SysUserServiceImpl sysUserService;
    private final AccountWechatServiceImpl accountWechatService;
    private final FriendRequestTaskServiceImpl friendRequestTaskService;
    private final PosterServiceImpl posterService;
    private final SysAttachServiceImpl sysAttachService;
    private final FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService;

    public OldToNew(JdbcTemplate jdbcTemplate, AccountServiceImpl accountService, SysUserServiceImpl sysUserService, AccountWechatServiceImpl accountWechatService, FriendRequestTaskServiceImpl friendRequestTaskService, PosterServiceImpl posterService, SysAttachServiceImpl sysAttachService, FriendRequestTaskDetailServiceImpl friendRequestTaskDetailService) {
        this.jdbcTemplate = jdbcTemplate;
        this.accountService = accountService;
        this.sysUserService = sysUserService;
        this.accountWechatService = accountWechatService;
        this.friendRequestTaskService = friendRequestTaskService;

        this.posterService = posterService;
        this.sysAttachService = sysAttachService;
        this.friendRequestTaskDetailService = friendRequestTaskDetailService;
    }

    public synchronized void exec() {
        QueryWrapper<Account> accountQueryWrapper = new QueryWrapper<>();
        accountService.remove(accountQueryWrapper);

        QueryWrapper<SysUser> sysUserQueryWrapper = new QueryWrapper<>();
        sysUserQueryWrapper.gt("lId", 1);
        sysUserService.remove(sysUserQueryWrapper);

        QueryWrapper<AccountWechat> accountWechatQueryWrapper = new QueryWrapper<>();
        accountWechatService.remove(accountWechatQueryWrapper);

        String sql = "select * from `hx-private-server_v2`.Company";
        List<Map<String, Object>> listCompany = jdbcTemplate.queryForList(sql);

        sql = "select * from `hx-private-server_v2`.CompanyAccount where Id>1";
        List<Map<String, Object>> listCompanyAccount = jdbcTemplate.queryForList(sql);

        sql = "select * from `hx-private-server_v2`.CompanyWechat";
        List<Map<String, Object>> listCompanyWechat = jdbcTemplate.queryForList(sql);

        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("cunkebao.Account");
        for (Map<String, Object> company : listCompany) {
            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("lId", company.get("Id"));
            parameters.put("sName", company.get("Name"));
            parameters.put("NewUserId", 1);
            parameters.put("EditUserId", 1);
            parameters.put("OwnerId", 1);
            parameters.put("dNewTime", company.get("CreatedTime"));
            parameters.put("dEditTime", company.get("CreatedTime"));

            Integer isDel = (Integer) company.get("IsDel");
            parameters.put("bActive", isDel == 1 ? 0 : 1);
            parameters.put("bDel", isDel);
            simpleJdbcInsert.execute(parameters);
        }

        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("cunkebao.SysUser");
        for (Map<String, Object> companyAccount : listCompanyAccount) {
            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("lID", companyAccount.get("Id"));
            parameters.put("sName", companyAccount.get("Username"));
            parameters.put("AccountId", companyAccount.get("CompanyId"));
            //parameters.put("dNewTime", LocalDateTime.now().toString());
            // parameters.put("dEditTime", LocalDateTime.now().toString());

            if ((int) companyAccount.get("PurviewAdmin") == 1) {
                parameters.put("SysRoleId", 4);

                sql = "UPDATE cunkebao.Account SET ManagerUserId = ? WHERE lId = ?";
                jdbcTemplate.update(sql, companyAccount.get("Id"), companyAccount.get("CompanyId"));
            } else {
                parameters.put("SysRoleId", 5);
            }

            parameters.put("sLoginName", companyAccount.get("Username"));
            parameters.put("sPassword", "123456");
            parameters.put("SysSolutionId", "3");
            parameters.put("bActive", "1");
            parameters.put("bMain", companyAccount.get("PurviewAdmin"));
            simpleJdbcInsert.execute(parameters);
        }

        simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("cunkebao.AccountWechat");
        for (Map<String, Object> wechat : listCompanyWechat) {
            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("AccountId", wechat.get("CompanyId"));

            sql = "select WechatStringId from `hx-private-server_v2`.Wechat where Id=?";
            Map<String, Object> wechatId = jdbcTemplate.queryForMap(sql, wechat.get("WechatId"));

            sql = "select * from `hx-private-server_v2`.Kefure_WechatAccount where WechatId=?";
            Map<String, Object> wechatStringId = jdbcTemplate.queryForMap(sql, wechatId.get("WechatStringId"));

            parameters.put("WeChatId", wechatStringId.get("Id"));
            simpleJdbcInsert.execute(parameters);
        }

        for (Map<String, Object> company : listCompany) {
            Long id = (Long) company.get("Id");
            accountService.updateAmt(id.intValue());
        }

        QueryWrapper<FriendRequestTask> friendRequestTaskQueryWrapper = new QueryWrapper<>();
        friendRequestTaskService.remove(friendRequestTaskQueryWrapper);

        QueryWrapper<Poster> posterQueryWrapper = new QueryWrapper<>();
        posterService.remove(posterQueryWrapper);

        SimpleJdbcInsert simpleJdbcInsert1 = new SimpleJdbcInsert(jdbcTemplate).withTableName("cunkebao.FriendRequestTask");
        SimpleJdbcInsert simpleJdbcInsert2 = new SimpleJdbcInsert(jdbcTemplate).withTableName("cunkebao.FriendRequestTaskDetail");

        sql = "select * from `hx-private-server_v2`.WechatFriendRequestTask";
        List<Map<String, Object>> listTask = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> task : listTask) {
            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("lId", task.get("Id"));
            parameters.put("sName", task.get("Name"));
            parameters.put("AccountId", task.get("CompanyId"));
            parameters.put("dNewTime", task.get("CreatedTime"));
            parameters.put("dEditTime", task.get("UpdateTime"));
            parameters.put("NewUserId", task.get("AccountId"));
            parameters.put("EditUserId", task.get("AccountId"));

            parameters.put("MemoTypeId", "1");
            parameters.put("sHello", task.get("Hello"));
            parameters.put("sTheme", task.get("Topic"));
            parameters.put("sTip", task.get("posterTips"));
            parameters.put("lRequestInterval", task.get("MinFriendRequestInterval"));
            parameters.put("lMaxPerDayRequestAmt", task.get("MaxPerDayFriendRequestCount"));

            Integer status = (Integer) task.get("Status");
            if (status == 0) {
                parameters.put("bEnable", 0);
            } else {
                parameters.put("bEnable", 1);
            }

            String poster = (String) task.get("poster");
            if (StrUtil.isNotEmpty(poster)) {
                parameters.put("TypeId", "poster");
                JSONArray jsonArray = JSONArray.parseArray(poster);
                for (int i = 0; i < jsonArray.size(); i++) {
                    String string = jsonArray.getString(i);

                    QueryWrapper<SysAttach> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("sCdnUrl", string);
                    queryWrapper.last("LIMIT 1");
                    SysAttach sysAttach = sysAttachService.getOne(queryWrapper);
                    if (sysAttach == null) {
                        sysAttach = new SysAttach();
                        sysAttach.setSName(null);
                        sysAttach.setSObjectName("business/poster");
                        sysAttach.setSFilePath(string);
                        sysAttach.setDNewTime(LocalDateTime.now());
                        sysAttach.setNewUserId(1);
                        sysAttach.setSCdnUrl(string);
                        sysAttach.setBImage(1);
                        sysAttachService.save(sysAttach);
                    }

                    Poster poster1 = new Poster();
                    poster1.setSName((String) task.get("Name"));

                    Long AccountId = (Long) task.get("AccountId");
                    poster1.setNewUserId(AccountId.intValue());

                    poster1.setDNewTime(LocalDateTime.now());
                    poster1.setSThumb(sysAttach.getLID());
                    posterService.save(poster1);

                    parameters.put("PosterId", poster1.getLId());
                }
            } else {
                parameters.put("TypeId", "other");
            }

            jdbcTemplate.execute("UPDATE `hx-private-server_v2`.WechatFriendRequestWechat SET UpdateTime=NULL WHERE UpdateTime='0000-00-00 00:00:00.000000'");

            StringBuilder sWechat = new StringBuilder();
            sql = "select * from `hx-private-server_v2`.WechatFriendRequestWechat WHERE WechatFriendRequestTaskId=?";
            List<Map<String, Object>> listReqWechat = jdbcTemplate.queryForList(sql, task.get("Id"));
            for (Map<String, Object> reqWechat : listReqWechat) {
                try {
                    sql = "select WechatStringId from `hx-private-server_v2`.Wechat where Id=?";
                    Map<String, Object> wechatId = jdbcTemplate.queryForMap(sql, reqWechat.get("WechatId"));

                    sql = "select * from `hx-private-server_v2`.Kefure_WechatAccount where WechatId=?";
                    Map<String, Object> wechatStringId = jdbcTemplate.queryForMap(sql, wechatId.get("WechatStringId"));

                    sWechat.append(";");
                    sWechat.append(wechatStringId.get("Id") + "");

                } catch (EmptyResultDataAccessException e) {
                    // Handle the case where no results were found
                }
            }

            if (listReqWechat.size() > 0) {
                sWechat.append(";");
            }
            parameters.put("WeChatId", sWechat.toString());

            JSONArray jsonArray = new JSONArray();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("TypeId", "1");
            jsonObject.put("sName", "回复模板1");
            jsonObject.put("jCond", new JSONArray());
            jsonObject.put("jEffectDate", new JSONObject());
            jsonObject.put("jEffectTime", new JSONObject());

            JSONArray jMessage = new JSONArray();

            jdbcTemplate.execute("UPDATE `hx-private-server_v2`.WechatFriendRequestTaskPassedReplyTemplate SET sendTime=NULL WHERE sendTime='00:00:00'");
            jdbcTemplate.execute("UPDATE `hx-private-server_v2`.WechatFriendRequestTaskPassedReplyTemplate SET dateTime=NULL WHERE dateTime='0000-00-00'");

            sql = "select * from `hx-private-server_v2`.WechatFriendRequestTaskPassedReplyTemplate WHERE WechatFriendRequestTaskId=? ORDER BY Id";
            List<Map<String, Object>> listTpl = jdbcTemplate.queryForList(sql, task.get("Id"));
            for (Map<String, Object> tpl : listTpl) {

                JSONObject message = new JSONObject();
                message.put("interval", (Integer) tpl.get("Interval"));
                message.put("mp", new JSONObject());
                Integer msgType = (Integer) tpl.get("MsgType");
                if (msgType == 1) {//纯文字
                    message.put("type", 1);

                    message.put("attachid", null);
                    message.put("videourl", null);
                    message.put("imgurl", null);
                    message.put("chatroomid", new JSONArray());
                    message.put("chatrooms", new JSONArray());
                    message.put("content", (String) tpl.get("Content"));
                } else if (msgType == 3) {//图片
                    message.put("type", 2);

                    QueryWrapper<SysAttach> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("sCdnUrl", (String) tpl.get("Content"));
                    queryWrapper.last("LIMIT 1");
                    SysAttach sysAttach = sysAttachService.getOne(queryWrapper);
                    if (sysAttach == null) {
                        sysAttach = new SysAttach();
                        sysAttach.setSName(null);
                        sysAttach.setSObjectName("business/FriendRequestTask");
                        sysAttach.setSFilePath((String) tpl.get("Content"));
                        sysAttach.setDNewTime(LocalDateTime.now());
                        sysAttach.setNewUserId(1);
                        sysAttach.setSCdnUrl((String) tpl.get("Content"));
                        sysAttach.setBImage(1);
                        sysAttachService.save(sysAttach);
                    }

                    message.put("attachid", sysAttach.getLID());
                    message.put("videourl", null);
                    message.put("imgurl", (String) tpl.get("Content"));
                    message.put("chatroomid", new JSONArray());
                    message.put("chatrooms", new JSONArray());
                    message.put("content", null);
                } else if (msgType == 43) {//图片
                    message.put("type", 3);

                    QueryWrapper<SysAttach> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("sCdnUrl", (String) tpl.get("Content"));
                    queryWrapper.last("LIMIT 1");
                    SysAttach sysAttach = sysAttachService.getOne(queryWrapper);
                    if (sysAttach == null) {
                        sysAttach = new SysAttach();
                        sysAttach.setSName(null);
                        sysAttach.setSObjectName("business/FriendRequestTask");
                        sysAttach.setSFilePath((String) tpl.get("Content"));
                        sysAttach.setDNewTime(LocalDateTime.now());
                        sysAttach.setNewUserId(1);
                        sysAttach.setSCdnUrl((String) tpl.get("Content"));
                        sysAttach.setBImage(0);
                        sysAttachService.save(sysAttach);
                    }

                    message.put("attachid", sysAttach.getLID());
                    message.put("videourl", (String) tpl.get("Content"));
                    message.put("imgurl", null);
                    message.put("chatroomid", new JSONArray());
                    message.put("chatrooms", new JSONArray());
                    message.put("content", null);
                }

                jMessage.add(message);
            }
            jsonObject.put("jMessage", jMessage);

            jsonArray.add(jsonObject);
            parameters.put("jTask", jsonArray.toJSONString());

            simpleJdbcInsert1.execute(parameters);
        }

        QueryWrapper<FriendRequestTaskDetail> friendRequestTaskDetailQueryWrapper = new QueryWrapper<>();
        friendRequestTaskDetailService.remove(friendRequestTaskDetailQueryWrapper);

        jdbcTemplate.execute("UPDATE `hx-private-server_v2`.WechatFriendRequestItem SET FailedTime=NULL WHERE FailedTime='0000-00-00 00:00:00.000000'");

        sql = "select * from `hx-private-server_v2`.WechatFriendRequestItem";
        List<Map<String, Object>> listItem = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> item : listItem) {
            Map<String, Object> parameters = new HashMap<String, Object>();

            parameters.put("lId", item.get("Id"));
            parameters.put("dNewTime", item.get("CreatedTime"));
            parameters.put("FriendRequestTaskId", item.get("WechatFriendRequestTaskId"));
            parameters.put("sPhoneOrWechatId", item.get("PhoneOrWechatId"));
            parameters.put("StatusId", item.get("status"));
            parameters.put("dSendTime", item.get("SendTime"));
            parameters.put("ThirdTaskId", item.get("ThirdTaskId"));
            parameters.put("dPassedTime", item.get("PassedTime"));
            parameters.put("dFailedTime", item.get("FailedTime"));
            parameters.put("sFailMessage", item.get("FailMessage"));
            parameters.put("dSuccessTime", item.get("SuccessTime"));

            if (item.get("WechatStringId") != null) {
                sql = "select * from `hx-private-server_v2`.Kefure_WechatAccount where WechatId=?";
                Map<String, Object> wechatStringId = jdbcTemplate.queryForMap(sql, item.get("WechatStringId"));
                parameters.put("WeChatId", wechatStringId.get("Id"));
            }

            if (item.get("FriendWechatId") != null && parameters.get("WeChatId") != null) {
                try {
                    sql = "select lId from cunkebao.WeChatFriend where WechatId=? AND sWechatId=?";
                    Map<String, Object> friend = jdbcTemplate.queryForMap(sql, parameters.get("WeChatId"), item.get("FriendWechatId"));
                    parameters.put("WeChatFriendId", friend.get("lId"));
                } catch (EmptyResultDataAccessException e) {
                    // Handle the case where no results were found
                }
            }

            simpleJdbcInsert2.execute(parameters);
        }

        for (Map<String, Object> task : listTask) {
            Long id = (Long) task.get("Id");
            friendRequestTaskService.updateStatus(id.intValue());
        }
    }
}
