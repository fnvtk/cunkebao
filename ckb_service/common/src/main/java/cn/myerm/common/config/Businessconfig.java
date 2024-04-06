package cn.myerm.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;


@Primary
@Configuration
@EnableConfigurationProperties(Businessconfig.class)
@ConfigurationProperties(prefix = "businessconfig")
@Data
public class Businessconfig {
    private String dbname;
    private String uploadsavepath;
    private String uploadurl;
    private String appid;
    private String appsecret;
    private String customertouchapiurl;
}
