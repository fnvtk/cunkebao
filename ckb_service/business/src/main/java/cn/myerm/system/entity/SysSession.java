package cn.myerm.system.entity;

import com.alibaba.fastjson.annotation.JSONField;
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
@TableName("SysSession")
public class SysSession extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    @JSONField(name = "ID")
    private String ID;

    @TableField("SysUserId")
    @JSONField(name = "SysUserId")
    private Integer SysUserId;

    @TableField("dLogin")
    private LocalDateTime dLogin;

    @TableField("dLastActivity")
    private LocalDateTime dLastActivity;

    @TableField("sIP")
    private String sIP;

    @TableField("sDevice")
    private String sDevice;

    @TableField("TypeId")
    @JSONField(name = "TypeId")
    private String TypeId;

    /**
     * 关联用户实体
     */
    @TableField(exist = false)
    private SysUser sysUser;
}
