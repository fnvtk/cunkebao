package cn.myerm.customertouch.wechat.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("WeChatRoomCollectCron")
public class WeChatRoomCollectCron {
    @TableId(value = "sId")
    private String sId;

    @TableField("WeChatRoomId")
    private Integer WeChatRoomId;

    @TableField("sWeChatId")
    private String sWeChatId;

    @TableField("bEnable")
    private Integer bEnable;
}