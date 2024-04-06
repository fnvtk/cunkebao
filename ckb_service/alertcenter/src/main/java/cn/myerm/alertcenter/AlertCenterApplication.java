package cn.myerm.alertcenter;

import cn.myerm.common.CommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@ComponentScan(basePackageClasses = {CommonConfig.class, AlertCenterConfig.class})
public class AlertCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(AlertCenterApplication.class, args);
    }
}
