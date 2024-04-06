package cn.myerm.alertcenter.mapper;

import cn.myerm.alertcenter.entity.Event;
import cn.myerm.alertcenter.entity.EventTrigger;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EventTriggerMapper extends BaseMapper<EventTrigger> {

}
