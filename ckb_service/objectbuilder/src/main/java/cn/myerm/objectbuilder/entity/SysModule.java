package cn.myerm.objectbuilder.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("SysModule")
public class SysModule extends CommonEntity {

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    @JSONField(name = "ID")
    private String ID;

    @TableField("sName")
    private String sName;
}
