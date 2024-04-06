package cn.myerm.customertouch;

import cn.myerm.common.CommonConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
@ComponentScan(basePackageClasses = {CommonConfig.class, CustomerTouchConfig.class})
public class CustomerTouchApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerTouchApplication.class, args);
    }
}
