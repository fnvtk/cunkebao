package cn.myerm.alertcenter.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("MaterialLib")
public class MaterialLib {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("sConfigJson")
    private String sConfigJson;

    @TableField("sTimeLimit")
    private String sTimeLimit;

    @TableField("sKeyWord")
    private String sKeyWord;

    @TableField("sExclude")
    private String sExclude;

    @TableField("sCollectObjectJson")
    private String sCollectObjectJson;

    @TableField("lAmt")
    private Integer lAmt;

    @TableField("sAIRequire")
    private String sAIRequire;

    @TableField("bAIEnable")
    private Integer bAIEnable;

    @TableField("bEnable")
    private Integer bEnable;
}
