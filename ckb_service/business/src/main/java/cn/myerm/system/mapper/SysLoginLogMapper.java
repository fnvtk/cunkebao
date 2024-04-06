package cn.myerm.system.mapper;

import cn.myerm.business.mapper.BusinessMapper;
import cn.myerm.system.entity.SysLoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Mars
 * @since 2021-04-20
 */
@Mapper
public interface SysLoginLogMapper extends BusinessMapper<SysLoginLog> {

}
