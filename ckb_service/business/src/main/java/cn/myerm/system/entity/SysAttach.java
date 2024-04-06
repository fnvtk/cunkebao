package cn.myerm.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;

import cn.myerm.business.entity.BusinessEntity;
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
 * @since 2021-04-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("SysAttach")
public class SysAttach extends BusinessEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "lID", type = IdType.AUTO)
    private Integer lID;

    @TableField("sName")
    private String sName;

    @TableField("sObjectName")
    private String sObjectName;

    @TableField("sFilePath")
    private String sFilePath;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("bImage")
    private Integer bImage;

    @TableField("sCdnUrl")
    private String sCdnUrl;
}
