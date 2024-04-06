package cn.myerm.alertcenter.entity;

import cn.myerm.common.entity.CommonEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("Cache")
public class Cache {

    @TableId("sKey")
    private String sKey;

    @TableField("sName")
    private String sName;

    @TableField("sValue")
    private String sValue;

}
