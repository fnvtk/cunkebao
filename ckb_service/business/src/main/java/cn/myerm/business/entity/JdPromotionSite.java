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
@TableName("JdPromotionSite")
public class JdPromotionSite extends BusinessEntity {

    @TableId(value = "lId")
    private Long lId;

    @TableField("sName")
    private String sName;

    @TableField("JdSocialMediaId")
    private Long JdSocialMediaId;

    @TableField("dNewTime")
    private LocalDateTime dNewTime;

}
