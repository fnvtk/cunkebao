package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2021-08-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysRelatedList")
public class SysRelatedList extends CommonEntity {

    private static final long serialVersionUID = 1L;

    @TableId("lID")
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("sRelatedObjectName")
    private String sRelatedObjectName;

    @TableField("SysListId")
    private Integer SysListId;

    @TableField("sRelatedField")
    private String sRelatedField;

    @TableField("lIndex")
    private Integer lIndex;

    @TableField(exist = false)
    private Object opera;
}
