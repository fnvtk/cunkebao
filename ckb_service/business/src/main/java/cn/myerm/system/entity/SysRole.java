package cn.myerm.system.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import cn.myerm.business.entity.BusinessEntity;
import cn.myerm.common.entity.CommonEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author Mars
 * @since 2021-04-05
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysRole")
public class SysRole extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("UpId")
    @JSONField(name = "UpId")
    private Integer UpId;

    @TableField("sPathId")
    private String sPathId;

    @TableField("SysSolutionId")
    @JSONField(name = "SysSolutionId")
    private Integer SysSolutionId;

    @TableField("bActive")
    private Integer bActive;
}
