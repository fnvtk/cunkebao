package cn.myerm.business.service.impl;

import cn.myerm.business.entity.WeChatFriendLabel;
import cn.myerm.business.mapper.WeChatFriendLabelMapper;
import cn.myerm.business.param.WechatFriendLabelParam;
import cn.myerm.business.service.IWeChatFriendLabelService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
public class WeChatFriendLabelServiceImpl extends BusinessServiceImpl<WeChatFriendLabelMapper, WeChatFriendLabel> implements IWeChatFriendLabelService {
    private static final Logger logger = LoggerFactory.getLogger(WeChatFriendLabelServiceImpl.class);

    /**
     * 为好友打标签
     *
     * @param param
     */
    public void markLabel(WechatFriendLabelParam param) {
        if (param.getLabelId() == null) {//没有设置标签，目的是为了更新WeChatFriendId
            if (param.getWeChatFriendId() != null) {
                UpdateWrapper<WeChatFriendLabel> objectUpdateWrapper = new UpdateWrapper<>();
                objectUpdateWrapper.set("WeChatFriendId", param.getWeChatFriendId());
                objectUpdateWrapper.eq("FriendRequestTaskDetailId", param.getFriendRequestTaskDetailId());
                update(objectUpdateWrapper);
            }
        } else {
            if (param.getIsUnique()) {
                QueryWrapper<WeChatFriendLabel> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("FriendRequestTaskDetailId", param.getFriendRequestTaskDetailId());
                queryWrapper.eq("LabelId", param.getLabelId());
                remove(queryWrapper);
            } else {
                QueryWrapper<WeChatFriendLabel> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("FriendRequestTaskDetailId", param.getFriendRequestTaskDetailId());
                queryWrapper.eq("LabelId", param.getLabelId());
                queryWrapper.eq("sValue", param.getSValue());
                remove(queryWrapper);
            }

            WeChatFriendLabel weChatFriendLabel = new WeChatFriendLabel();
            weChatFriendLabel.setFriendRequestTaskId(param.getFriendRequestTaskId());
            weChatFriendLabel.setFriendRequestTaskDetailId(param.getFriendRequestTaskDetailId());
            weChatFriendLabel.setLabelId(param.getLabelId());
            weChatFriendLabel.setWeChatFriendId(param.getWeChatFriendId());
            weChatFriendLabel.setSValue(param.getSValue());
            save(weChatFriendLabel);
        }
    }
}
