package ${basePackageUrl}.${moduleId}.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("${entityName}")
public class ${entityName} extends BusinessEntity {

<#if columns??>
<#--循环生成变量-->
    <#list columns as col>
    <#if (col["key"]=="PRI") && (col["isautoincrement"]=="auto_increment")>
    @TableId(value = "${col["columnName"]}", type = IdType.AUTO)
    <#else>
    @TableField("${col["columnName"]}")
    </#if>
    private ${col["columnType"]} ${col["entityColumnNo"]};

    </#list>
</#if>
}
