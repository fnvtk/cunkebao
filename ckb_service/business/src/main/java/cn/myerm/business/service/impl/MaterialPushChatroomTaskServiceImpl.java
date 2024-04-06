package cn.myerm.business.service.impl;

import cn.myerm.business.dto.WeChatRoomDto;
import cn.myerm.business.entity.IMaterialPushTask;
import cn.myerm.business.entity.MaterialPushChatroomTask;
import cn.myerm.business.entity.WeChatRoom;
import cn.myerm.business.mapper.MaterialPushChatroomTaskMapper;
import cn.myerm.business.param.ChatroomTaskParam;
import cn.myerm.business.param.ListParam;
import cn.myerm.business.param.PushTaskParam;
import cn.myerm.business.service.IMaterialPushChatroomTaskService;
import cn.myerm.common.utils.BeanUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class MaterialPushChatroomTaskServiceImpl extends BusinessServiceImpl<MaterialPushChatroomTaskMapper, MaterialPushChatroomTask> implements IMaterialPushChatroomTaskService {

    private static final Logger logger = LoggerFactory.getLogger(MaterialPushChatroomTaskServiceImpl.class);

    @Resource
    private WeChatRoomServiceImpl weChatRoomService;

    public String appendWhereSql(ListParam listParam) {
        if (getCurrUser().getSysRoleId() > 1) {
            return super.appendWhereSql(listParam) + " AND NewUserId='" + getCurrUser().getLID() + "'";
        }

        return super.appendWhereSql(listParam);
    }

    protected void handleListData(List<Map<String, Object>> listObjectData) {
        ArrayList<String> arrWechatroomId = new ArrayList<>();
        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);

            if (objectData.get("PushWechatroomIds") != null) {
                arrWechatroomId.addAll(Arrays.stream(((String) objectData.get("PushWechatroomIds")).split(",")).toList());
            }
        }
        arrWechatroomId.add("0");

        HashMap<String, WeChatRoomDto> mapWechatroom = new HashMap<>();
        List<WeChatRoom> weChatRooms = weChatRoomService.listByIds(arrWechatroomId);
        for (WeChatRoom weChatRoom : weChatRooms) {
            mapWechatroom.put(weChatRoom.getLId() + "", BeanUtils.transform(WeChatRoomDto.class, weChatRoom));
        }

        for (int i = 0; i < listObjectData.size(); i++) {
            Map<String, Object> objectData = listObjectData.get(i);

            List<WeChatRoomDto> listWechatRoom = new ArrayList<>();
            if (objectData.get("PushWechatroomIds") != null) {
                String[] wechatroomIds = ((String) objectData.get("PushWechatroomIds")).split(",");
                for (String wechatroomId : wechatroomIds) {
                    listWechatRoom.add(mapWechatroom.get(wechatroomId));
                }
            }
            objectData.put("PushWechatroomIds", listWechatRoom);
        }
    }

    /**
     * 保存
     *
     * @param chatroomTaskParam
     */
    public void save(ChatroomTaskParam chatroomTaskParam) {
        MaterialPushChatroomTask materialPushChatroomTask = new MaterialPushChatroomTask();
        materialPushChatroomTask.setLId(chatroomTaskParam.getLId());
        materialPushChatroomTask.setSName(chatroomTaskParam.getSName());
        materialPushChatroomTask.setNewUserId(getCurrUser().getLID());
        materialPushChatroomTask.setDNewTime(LocalDateTime.now());
        materialPushChatroomTask.setLPushAmtPerDay(chatroomTaskParam.getLPushAmtPerDay());
        materialPushChatroomTask.setLPushDelay(chatroomTaskParam.getLPushDelay());
        materialPushChatroomTask.setSPeriod(chatroomTaskParam.getSPeriod());

        //通过时间段设置+每天发布的朋友圈数量，共同确定每条朋友圈的间隔
        String[] arrPeriod = chatroomTaskParam.getSPeriod().split("-");

        LocalTime startTime = LocalTime.parse(arrPeriod[0]);
        LocalTime endTime = LocalTime.parse(arrPeriod[1]);

        // 计算时间段的小时数和分钟数
        long hours = endTime.getHour() - startTime.getHour();
        long minutes = endTime.getMinute() - startTime.getMinute();

        // 总分钟数 = 小时数 * 60 + 分钟数
        long totalMinutes = hours * 60 + minutes;

        materialPushChatroomTask.setLInterval((int) (totalMinutes / chatroomTaskParam.getLPushAmtPerDay()));

        materialPushChatroomTask.setMaterialLibIds(chatroomTaskParam.getArrMaterialLibId());
        materialPushChatroomTask.setPushWechatroomIds(chatroomTaskParam.getArrWechatroomId());
        materialPushChatroomTask.setTypeId(chatroomTaskParam.getTypeId());
        materialPushChatroomTask.setBEnable(chatroomTaskParam.getBEnable() ? 1 : 0);
        materialPushChatroomTask.setBLoop(chatroomTaskParam.getBLoop() ? 1 : 0);
        materialPushChatroomTask.setBImmediately(chatroomTaskParam.getBImmediately() ? 1 : 0);
        materialPushChatroomTask.setJdPromotionSiteId(chatroomTaskParam.getJdPromotionSiteId());

        if (materialPushChatroomTask.getLId() == null && materialPushChatroomTask.getBImmediately().equals(0)) {
            materialPushChatroomTask.setSPushSchedule(execSchedule(materialPushChatroomTask));
        }

        saveOrUpdate(materialPushChatroomTask);
    }

    /**
     * 获取所有已启用的任务
     *
     * @return
     */
    public List<IMaterialPushTask> getEnableList(boolean bImmediately) {
        QueryWrapper<MaterialPushChatroomTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bDel", 0);
        queryWrapper.eq("bEnable", 1);
        queryWrapper.eq("bImmediately", bImmediately ? 1 : 0);
        List<MaterialPushChatroomTask> chatroomTaskList = list(queryWrapper);

        List<IMaterialPushTask> materialPushTaskList = new ArrayList<>();
        for (MaterialPushChatroomTask materialPushChatroomTask : chatroomTaskList) {
            materialPushTaskList.add((IMaterialPushTask) materialPushChatroomTask);
        }

        return materialPushTaskList;
    }

    public void updateById(IMaterialPushTask materialPushTask) {
        updateById((MaterialPushChatroomTask) materialPushTask);
    }

    /**
     * 更新状态
     */
    public void updateStatus(PushTaskParam param) {
        UpdateWrapper<MaterialPushChatroomTask> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.set("lPushQueueAmt", param.getLPushedAmt());
        objectUpdateWrapper.set("dLastPushTime", param.getDLastPushTime());
        objectUpdateWrapper.eq("lId", param.getTaskId());
        update(objectUpdateWrapper);
    }


    /**
     * 执行指定任务的推送排程
     */
    private String execSchedule(MaterialPushChatroomTask materialPushChatroomTask) {
        String[] arrPeriod = materialPushChatroomTask.getSPeriod().split("-");

        // 创建一个 Random 对象
        Random random = new Random();
        LocalTime nextTime = LocalTime.parse(arrPeriod[0]);
        ArrayList<String> listSchedule = new ArrayList<>();
        while (true) {
            //这里特意只取间隔的一半，是为了防止因为随机导致相隔的推送太接近
            int lRandomMinute = random.nextInt(materialPushChatroomTask.getLInterval());

            LocalTime nextPustTime = nextTime.plusMinutes(lRandomMinute);
            nextTime = nextTime.plusMinutes(materialPushChatroomTask.getLInterval());

            if (listSchedule.size() == materialPushChatroomTask.getLPushAmtPerDay()) {
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
        QueryWrapper<MaterialPushChatroomTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("bDel", 0);
        queryWrapper.eq("bEnable", 1);
        List<MaterialPushChatroomTask> chatroomTaskList = list(queryWrapper);
        for (MaterialPushChatroomTask materialPushChatroomTask : chatroomTaskList) {
            materialPushChatroomTask.setSPushSchedule(execSchedule(materialPushChatroomTask));
            updateById(materialPushChatroomTask);
        }
    }
}
