package cn.myerm.business.dto;

import cn.myerm.system.entity.SysAttach;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

@Data
public class MaterialDto {

    @JSONField(name = "lId")
    private Integer lId;

    @JSONField(name = "sName")
    private String sName;

    @JSONField(name = "dNewTime")
    private LocalDateTime dNewTime;

    @JSONField(name = "dEditTime")
    private LocalDateTime dEditTime;

    @JSONField(name = "dCollectTime")
    private LocalDateTime dCollectTime;

    @JSONField(name = "sContent")
    private String sContent;

    @JSONField(name = "sContentAI")
    private String sContentAI;

    @JSONField(name = "sTitle")
    private String sTitle;

    @JSONField(name = "sLink")
    private String sLink;

    @JSONField(name = "sHead")
    private String sHead;

    @JSONField(name = "sFoot")
    private String sFoot;

    @JSONField(name = "TypeId")
    private String TypeId;

    @JSONField(name = "MaterialLibId")
    private Integer MaterialLibId;

    @JSONField(name = "MiniProgrameLibId")
    private Integer MiniProgrameLibId;

    @JSONField(name = "sThumb")
    private String sThumb;

    @JSONField(name = "sPicJson")
    private String sPicJson;

    @JSONField(name = "SourceId")
    private String SourceId;

    @JSONField(name = "lUse")
    private Integer lUse;

    @JSONField(name = "object")
    private Object object;

    @JSONField(name = "arrPic")
    private List<SysAttach> arrPic;

    @JSONField(name = "video")
    private SysAttach video;
}
