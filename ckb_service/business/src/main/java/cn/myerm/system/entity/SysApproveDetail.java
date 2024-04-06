package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import cn.myerm.business.entity.BusinessEntity;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author Mars
 * @since 2021-09-29
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysApproveDetail")
public class SysApproveDetail extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    /**
     * 审批编号
     */
    @TableField("sName")
    private String sName;

    /**
     * 关联审批主体
     */
    @TableField("ApproveId")
    private Integer ApproveId;

    /**
     * 审批的序号，同一个审批主体，序号相同的审批过程代表是会签。
     */
    @TableField("lIndex")
    private Integer lIndex;

    /**
     * 审批通过时间
     */
    @TableField("dApproved")
    private LocalDateTime dApproved;

    /**
     * 审批人
     */
    @TableField("ApproveUserId")
    private Integer ApproveUserId;

    /**
     * 状态	rejected=已驳回	passed=审批通过
     */
    @TableField("StatusId")
    private String StatusId;

    /**
     * 是否最后一个审核节点
     */
    @TableField("bLastApprove")
    private Boolean bLastApprove;

    @TableField("bJoint")
    private Boolean bJoint;
}
