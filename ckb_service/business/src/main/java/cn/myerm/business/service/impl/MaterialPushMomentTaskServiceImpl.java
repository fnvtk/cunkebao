package cn.myerm.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.myerm.business.dto.WeChatDto;
import cn.myerm.business.entity.*;
import cn.myerm.business.mapper.MaterialPushMomentTaskMapper;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.param.MomentTaskParam;
import cn.myerm.business.param.PushTaskParam;
import cn.myerm.business.service.IMaterialPushMomentTaskService;
import cn.myerm.common.utils.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class MaterialPushMomentTaskServiceImpl extends BusinessServiceImpl<MaterialPushMomentTaskMapper, MaterialPushMomentTask> implements IMaterialPushMomentTaskService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialPushMomentTaskServiceImpl.class);

    private final WeChatServiceImpl weChatService;

    private final MaterialLibServiceImpl materialLibService;

    @Autowired
    public MaterialPushMomentTaskServiceImpl(WeChatServiceImpl weChatService, MaterialLibServiceImpl materialLibService) {
        this.weChatService = weChatService;
        this.materialLibService = materialLibService;
    }

    public String appendWhereSql(ListParam listParam) {
        if (getCurrUser().getSysRoleId() > 1) {
            return super.appendWhereSql(listParam) + " AND NewUserId='" + getCurrUser().getLID() + "'";
        }

        return super.appendWhereSql(listParam);
    }

    protected void handleListData(List<Map<String, Object>> listObjectData) {
        ArrayList<String> arrWechatId = new ArrayList<>();
        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);

            if (objectData.get("PushWechatIds") != null) {
                arrWechatId.addAll(Arrays.stream(((String) objectData.get("PushWechatIds")).split(",")).toList());
            }
        }
        arrWechatId.add("0");

        HashMap<String, WeChatDto> mapWechat = new HashMap<>();
        List<WeChat> weChats = weChatService.listByIds(arrWechatId);
        for (WeChat weChat : weChats) {
            mapWechat.put(weChat.getLId() + "", BeanUtils.transform(WeChatDto.class, weChat));
        }

        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);

            List<WeChatDto> listWechat = new ArrayList<>();
            if (objectData.get("PushWechatIds") != null) {
                String[] wechatIds = ((String) objectData.get("PushWechatIds")).split(",");
                for (String wechatId : wechatIds) {
                    listWechat.add(mapWechat.get(wechatId));
                }
            }
            objectData.put("PushWechatIds", listWechat);
        }
    }

    /**
     * 保存
     *
     * @param momentTaskParam
     */
    public void save(MomentTaskParam momentTaskParam) {
        MaterialPushMomentTask materialPushMomentTask = new MaterialPushMomentTask();
        materialPushMomentTask.setLId(momentTaskParam.getLId());
        materialPushMomentTask.setSName(momentTaskParam.getSName());
        materialPushMomentTask.setNewUserId(getCurrUser().getLID());
        materialPushMomentTask.setDNewTime(LocalDateTime.now());
        materialPushMomentTask.setLPushAmtPerDay(momentTaskParam.getLPushAmtPerDay());
        materialPushMomentTask.setSPeriod(momentTaskParam.getSPeriod());

        //通过时间段设置+每天发布的朋友圈数量，共同确定每条朋友圈的间隔
        String[] arrPeriod = momentTaskParam.getSPeriod().split("-");

        LocalTime startTime = LocalTime.parse(arrPeriod[0]);
        LocalTime endTime = LocalTime.parse(arrPeriod[1]);

        // 计算时间段的小时数和分钟数
        long hours = endTime.getHour() - startTime.getHour();
        long minutes = endTime.getMinute() - startTime.getMinute();

        // 总分钟数 = 小时数 * 60 + 分钟数
        long totalMinutes = hours * 60 + minutes;

        materialPushMomentTask.setLInterval((int) (totalMinutes / momentTaskParam.getLPushAmtPerDay()));

        materialPushMomentTask.setMaterialLibIds(momentTaskParam.getArrMaterialLibId());
        materialPushMomentTask.setPushWechatIds(momentTaskParam.getArrWechatId());
        materialPushMomentTask.setTypeId(momentTaskParam.getTypeId());
        materialPushMomentTask.setBEnable(momentTaskParam.getBEnable() ? 1 : 0);
        materialPushMomentTask.setBLoop(momentTaskParam.getTypeId() == 1 ? 1 : 0);

        if (StrUtil.isEmpty(materialPushMomentTask.getSPushSchedule())) {
            materialPushMomentTask.setSPushSchedule(execSchedule(materialPushMomentTask));
        }

        saveOrUpdate(materialPushMomentTask);
    }

    public void updateById(IMaterialPushTask materialPushTask) {
        updateById((MaterialPushMomentTask) materialPushTask);
    }

    /**
     * 更新状态
     */
    public void updateStatus(PushTaskParam param) {
        UpdateWrapper<MaterialPushMomentTask> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.set("lPushQueueAmt", param.getLPushedAmt());
        objectUpdateWrapper.set("dLastPushTime", param.getDLastPushTime());
        objectUpdateWrapper.eq("lId", param.getTaskId());
        update(objectUpdateWrapper);
    }

    /**
     * 执行指定任务的推送排程
     */
    private String execSchedule(MaterialPushMomentTask materialPushMomentTask) {
        String[] arrPeriod = materialPushMomentTask.getSPeriod().split("-");

        // 创建一个 Random 对象
        Random random = new Random();
        LocalTime nextTime = LocalTime.parse(arrPeriod[0]);
        ArrayList<String> listSchedule = new ArrayList<>();
        while (true) {
            //这里特意只取间隔的一半，是为了防止因为随机导致相隔的推送太接近
            int lRandomMinute = random.nextInt(materialPushMomentTask.getLInterval());

            LocalTime nextPustTime = nextTime.plusMinutes(lRandomMinute);
            nextTime = nextTime.plusMinutes(materialPushMomentTask.getLInterval());

            if (listSchedule.size() == materialPushMomentTask.getLPushAmtPerDay()) {
                break;
            }

            listSchedule.add(nextPustTime.toString());
        }

        return String.join(",", listSchedule);
    }

    /**
     * 执行全部已启用任务的排程
     */
    public void execSchedule() {
        QueryWrapper<MaterialPushMomentTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bDel", 0);
        queryWrapper.eq("bEnable", 1);
        List<MaterialPushMomentTask> momentTaskList = list(queryWrapper);
        for (MaterialPushMomentTask materialPushMomentTask : momentTaskList) {
            materialPushMomentTask.setSPushSchedule(execSchedule(materialPushMomentTask));
            updateById(materialPushMomentTask);
        }
    }

    /**
     * 获取所有已启用的任务
     *
     * @return
     */
    public List<IMaterialPushTask> getEnableList(boolean bImmediately) {
        QueryWrapper<MaterialPushMomentTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bDel", 0);
        queryWrapper.eq("bEnable", 1);
        List<MaterialPushMomentTask> momentTaskList = list(queryWrapper);

        List<IMaterialPushTask> materialPushTaskList = new ArrayList<>();
        for (MaterialPushMomentTask materialPushMomentTask : momentTaskList) {
            materialPushTaskList.add((IMaterialPushTask) materialPushMomentTask);
        }

        return materialPushTaskList;
    }
}
