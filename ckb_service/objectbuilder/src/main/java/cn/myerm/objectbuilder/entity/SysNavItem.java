package cn.myerm.objectbuilder.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import cn.myerm.common.entity.CommonEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author Mars
 * @since 2021-04-08
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysNavItem")
public class SysNavItem extends CommonEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("sRoute")
    private String sRoute;

    @TableField("sIcon")
    private String sIcon;
}
