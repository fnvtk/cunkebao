package cn.myerm.objectbuilder.service;

import cn.myerm.common.service.ICommonService;
import cn.myerm.objectbuilder.entity.SysList;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.myerm.objectbuilder.params.ListParams;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Mars
 * @since 2021-04-09
 */
public interface ISysListService extends ICommonService<SysList> {
        List<Map<String,Object>> sysListList(String sObjectName);
        boolean newsave(ListParams listParams);
        boolean editsave(ListParams listParams);
        boolean del(Integer id);
        void clone(Integer id);
        boolean editlistfield(Integer id,String slistselectid);
}
