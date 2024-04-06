package cn.myerm.objectbuilder.mapper;

import cn.myerm.common.mapper.CommonMapper;
import cn.myerm.objectbuilder.entity.SysField;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Mars
 * @since 2021-04-08
 */
@Mapper
public interface SysFieldMapper extends CommonMapper<SysField> {
    Integer newSave(SysField sysField);

    SysField getFieldById(int id);
}
