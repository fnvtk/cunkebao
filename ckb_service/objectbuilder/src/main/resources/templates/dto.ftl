package ${basePackageUrl}.${moduleId}.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class ${entityName}Dto {

<#if columns??>
<#--循环生成变量-->
    <#list columns as col>
    @JSONField(name = "${col["columnName"]}")
    private ${col["columnType"]} ${col["entityColumnNo"]};

    </#list>
</#if>
}
