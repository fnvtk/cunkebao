package cn.myerm.openapi.config;

import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MyFastjsonConfig {
    @Bean
    FastJsonHttpMessageConverter fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter converter = new FastJsonHttpMessageConverter();
        FastJsonConfig config = new FastJsonConfig();
        //升级之后的fastjson（1.2.28之后的版本）需要手动配置MediaType，否则会报错
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        supportedMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);
        supportedMediaTypes.add(MediaType.APPLICATION_ATOM_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        supportedMediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        supportedMediaTypes.add(MediaType.APPLICATION_PDF);
        supportedMediaTypes.add(MediaType.APPLICATION_RSS_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XHTML_XML);
        supportedMediaTypes.add(MediaType.APPLICATION_XML);
        supportedMediaTypes.add(MediaType.IMAGE_GIF);
        supportedMediaTypes.add(MediaType.IMAGE_JPEG);
        supportedMediaTypes.add(MediaType.IMAGE_PNG);
        supportedMediaTypes.add(MediaType.TEXT_EVENT_STREAM);
        supportedMediaTypes.add(MediaType.TEXT_HTML);
        supportedMediaTypes.add(MediaType.TEXT_MARKDOWN);
        supportedMediaTypes.add(MediaType.TEXT_PLAIN);
        supportedMediaTypes.add(MediaType.TEXT_XML);
        converter.setSupportedMediaTypes(supportedMediaTypes);

        SerializeConfig serializeConfig = SerializeConfig.globalInstance;
        serializeConfig.put(BigInteger.class, ToStringSerializer.instance);
        serializeConfig.put(BigDecimal.class, ToStringSerializer.instance);
        serializeConfig.put(Float.class, ToStringSerializer.instance);
        serializeConfig.put(Long.class, ToStringSerializer.instance);
        serializeConfig.put(Long.TYPE, ToStringSerializer.instance);
        config.setSerializeConfig(serializeConfig);

        config.setDateFormat("yyyy-MM-dd HH:mm:ss");
        config.setCharset(Charset.forName("UTF-8"));
        config.setSerializerFeatures(
                //json格式化
                //SerializerFeature.PrettyFormat,
                SerializerFeature.DisableCircularReferenceDetect,
                //输出value为null的数据
                SerializerFeature.WriteMapNullValue,
                SerializerFeature.SortField
        );
        converter.setFastJsonConfig(config);
        return converter;
    }
}
