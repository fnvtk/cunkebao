package cn.myerm.openapi;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.myerm.business.MyermBusinessConfig;
import cn.myerm.common.CommonConfig;
import cn.myerm.objectbuilder.ObjectBuilderConfig;
import cn.myerm.openapi.config.MyFastjsonConfig;
import cn.myerm.system.SystemConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

@EnableTransactionManagement
@SpringBootApplication
//@EnableEurekaClient
@ServletComponentScan
@ComponentScan(basePackageClasses = {OpenApiConfig.class, ObjectBuilderConfig.class, MyermBusinessConfig.class, CommonConfig.class, MyFastjsonConfig.class, SystemConfig.class})
public class OpenApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiApplication.class, args);
    }
}
