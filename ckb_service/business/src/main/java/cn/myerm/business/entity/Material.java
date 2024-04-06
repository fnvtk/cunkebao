package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("Material")
public class Material extends BusinessEntity implements Cloneable {

    @TableId(value = "lId", type = IdType.AUTO)
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

    @TableField("dEditTime")
    private LocalDateTime dEditTime;

    @TableField("dCollectTime")
    private LocalDateTime dCollectTime;

    @TableField("lWechatTime")
    private Long lWechatTime;

    @TableField("sContent")
    private String sContent;

    @TableField("sContentAI")
    private String sContentAI;

    @TableField("sSourceContent")
    private String sSourceContent;

    @TableField("sTitle")
    private String sTitle;

    @TableField("sLink")
    private String sLink;

    @TableField("sHead")
    private String sHead;

    @TableField("sFoot")
    private String sFoot;

    @TableField("sComment")
    private String sComment;

    @TableField("TypeId")
    private Integer TypeId;

    @TableField("NewUserId")
    private Integer NewUserId;

    @TableField("QueueId")
    private Integer QueueId;

    @TableField("sThumb")
    private String sThumb;

    @TableField("sPic")
    private String sPic;

    @TableField("VideoId")
    private String VideoId;

    @TableField("SourceId")
    private String SourceId;

    @TableField("MaterialId")
    private Integer MaterialId;

    @TableField(exist = false)
    private Integer lCount;

    @TableField("SourceDataId")
    private String SourceDataId;

    @TableField("CollectObjectId")
    private String CollectObjectId;

    @TableField("MaterialLibId")
    private Integer MaterialLibId;

    @TableField("bClassify")
    private Integer bClassify;

    @TableField("bManual")
    private Integer bManual;

    @TableField("MiniProgrameLibId")
    private Integer MiniProgrameLibId;

    @TableField("bAIGenerated")
    private Integer bAIGenerated;

    public Material clone() throws CloneNotSupportedException {
        return (Material) super.clone();
    }
}
