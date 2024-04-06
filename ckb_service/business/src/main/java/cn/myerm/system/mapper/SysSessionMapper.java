package cn.myerm.system.mapper;

import cn.myerm.business.mapper.BusinessMapper;
import cn.myerm.common.mapper.CommonMapper;
import cn.myerm.system.entity.SysSession;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Mars
 * @since 2021-04-05
 */
@Mapper
public interface SysSessionMapper extends BusinessMapper<SysSession> {
    SysSession getById(String id);
}
