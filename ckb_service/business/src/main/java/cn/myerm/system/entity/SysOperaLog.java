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
@TableName("SysOperaLog")
public class SysOperaLog extends CommonEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("SysUserId")
    private Integer SysUserId;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("ObjectId")
    private String ObjectId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("sAction")
    private String sAction;

    @TableField("sDevice")
    private String sDevice;

    @TableField("sIP")
    private String sIP;

    @TableField("sUri")
    private String sUri;

    @TableField("sParamJson")
    private String sParamJson;

    @TableField("sHeaderJson")
    private String sHeaderJson;

    @TableField("sRespone")
    private String sRespone;
}
