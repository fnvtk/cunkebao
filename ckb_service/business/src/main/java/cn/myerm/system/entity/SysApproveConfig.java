package cn.myerm.system.entity;

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
@TableName("SysApproveConfig")
public class SysApproveConfig extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 对象
     */
    @TableId("sObjectName")
    private String sObjectName;

    @TableField("CopyToUserId")
    private String CopyToUserId;
}
