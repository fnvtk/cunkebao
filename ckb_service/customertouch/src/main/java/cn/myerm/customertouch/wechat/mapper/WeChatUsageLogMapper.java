package cn.myerm.customertouch.wechat.mapper;

import cn.myerm.customertouch.wechat.entity.WeChatRoomCollectCron;
import cn.myerm.customertouch.wechat.entity.WeChatUsageLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeChatUsageLogMapper extends BaseMapper<WeChatUsageLog> {
}
