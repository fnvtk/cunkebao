package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.myerm.business.entity.BusinessEntity;
import com.baomidou.mybatisplus.annotation.TableId;
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
@TableName("SysApproveConfigDetail")
public class SysApproveConfigDetail extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    /**
     * 对象
     */
    @TableField("sObjectName")
    private String sObjectName;

    /**
     * 审批层级
     */
    @TableField("lIndex")
    private Integer lIndex;

    /**
     * 审批人	superadmin=超管	manager=上级主管	fixed=指定审批人
     */
    @TableField("sApprover")
    private String sApprover;

    /**
     * 审批模式	single=单独审批，默认	joint=会签	如果是single，任意一个审批人审批通过即通过该层级；如果是joint，要全部人审批通过才算通过。
     */
    @TableField("ModeId")
    private String ModeId;

    /**
     * 当sApprover=fixed时，这个字段要取值SysUser，可以多
     */
    @TableField("FixedApproverUserId")
    private String FixedApproverUserId;


}
