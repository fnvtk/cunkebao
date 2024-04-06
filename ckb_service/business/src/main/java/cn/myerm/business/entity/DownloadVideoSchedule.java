package cn.myerm.business.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("DownloadVideoSchedule")
public class DownloadVideoSchedule extends BusinessEntity {

    @TableId("lId")
    private Integer lId;

    @TableField("sName")
    private String sName;

    @TableField("sType")
    private String sType;

    @TableField("WechatId")
    private Integer WechatId;

    @TableField("ChatroomId")
    private Integer ChatroomId;

    @TableField("MessageId")
    private Integer MessageId;

    @TableField("sTencentUrl")
    private String sTencentUrl;

    @TableField("dSendTime")
    private LocalDateTime dSendTime;

    @TableField("bSend")
    private Integer bSend;

    @TableField("bDownload")
    private Integer bDownload;

}
