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

import java.time.LocalDateTime;

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
@TableName("SysUser")
public class SysUser extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("sCode")
    private String sCode;

    @TableField("UpId")
    private Integer UpId;

    @TableField("sPathId")
    private String sPathId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("dEditTime")
    private LocalDateTime dEditTime;

    @TableField("SysRoleId")
    private Integer SysRoleId;

    @TableField("SysDepId")
    private Integer SysDepId;

    @TableField("sLoginName")
    private String sLoginName;

    @TableField("sPassword")
    private String sPassword;

    @TableField("sEMail")
    private String sEMail;

    @TableField("sMobile")
    private String sMobile;

    @TableField("SysSolutionId")
    private Integer SysSolutionId;

    @TableField("bActive")
    private Integer bActive;

    @TableField("bMain")
    private Integer bMain;

    @TableField("AccountId")
    private Integer AccountId;

    @TableField(exist = false)
    private SysDep sysDep;

    @TableField(exist = false)
    private SysRole sysRole;
}
