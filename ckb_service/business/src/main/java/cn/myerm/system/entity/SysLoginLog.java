package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
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
 * @since 2021-04-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysLoginLog")
public class SysLoginLog extends CommonEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("SysUserId")
    private Integer SysUserId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("sDevice")
    private String sDevice;

    @TableField("sIP")
    private String sIP;


}
