package cn.myerm.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import cn.myerm.system.entity.SysApproveDetail;
import cn.myerm.system.entity.SysUser;
import cn.myerm.system.mapper.SysApproveDetailMapper;
import cn.myerm.system.service.ISysApproveDetailService;
import cn.myerm.business.service.impl.BusinessServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@Service
public class SysApproveDetailServiceImpl extends BusinessServiceImpl<SysApproveDetailMapper, SysApproveDetail> implements ISysApproveDetailService {
    public List<SysApproveDetail> queryByParentId(Integer ParentId) {
        QueryWrapper<SysApproveDetail> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ApproveId", ParentId);
        queryWrapper.orderByAsc("lIndex");

        return list(queryWrapper);
    }

    /**
     * 将当前其他待审批节点，都设置成已过时的状态
     * @param ApproveId
     * @param lIndex
     */
    public void setPast(Integer ApproveId, Integer lIndex) {
        UpdateWrapper<SysApproveDetail> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("StatusId", "past");
        updateWrapper.eq("ApproveId", ApproveId);
        updateWrapper.eq("lIndex", lIndex);
        updateWrapper.eq("StatusId", "approving");
        update(updateWrapper);
    }

    /**
     * 设置节点已审核
     * @param ApproveId
     * @param lIndex
     */
    public void setApproveing(Integer ApproveId, Integer lIndex) {
        UpdateWrapper<SysApproveDetail> updateWrapper = new UpdateWrapper<>();
        updateWrapper.set("StatusId", "approving");
        updateWrapper.eq("ApproveId", ApproveId);
        updateWrapper.eq("lIndex", lIndex);
        update(updateWrapper);
    }
}
