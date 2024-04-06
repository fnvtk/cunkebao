package cn.myerm.customertouch.wechat.mapper;

import cn.myerm.customertouch.wechat.entity.WeChatMessage;
import cn.myerm.customertouch.wechat.entity.WeChatMoment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface WeChatMessageMapper extends BaseMapper<WeChatMessage> {
}
