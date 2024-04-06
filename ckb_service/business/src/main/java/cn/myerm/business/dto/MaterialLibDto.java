package cn.myerm.business.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class MaterialLibDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "NewUserId")
    private Integer NewUserId;

    @JSONField(name = "sTimeLimit")
    private String sTimeLimit;

    @JSONField(name = "sKeyWord")
    private String sKeyWord;

    @JSONField(name = "sExclude")
    private String sExclude;

    @JSONField(name = "sConfigJson")
    private String sConfigJson;

    @JSONField(name = "lAmt")
    private Integer lAmt;

    @JSONField(name = "bEnable")
    private Integer bEnable;

    @JSONField(name = "config")
    private Object config;

    @JSONField(name = "sCollectObjectJson")
    private String sCollectObjectJson;

    @JSONField(name = "sAIRequire")
    private String sAIRequire;

    @JSONField(name = "bAIEnable")
    private Integer bAIEnable;
}
