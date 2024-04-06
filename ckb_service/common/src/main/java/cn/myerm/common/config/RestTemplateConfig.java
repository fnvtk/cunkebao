package cn.myerm.common.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    @ConditionalOnMissingBean({RestOperations.class, RestTemplate.class})
    //Spring Boot的自动配置机制依靠@ConditionalOnMissingBean注解判断是否执行初始化代码，
    // 即如果用户已经创建了bean，则相关的初始化代码不再执行。
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        // 使用 utf-8 编码集的 conver 替换默认的 conver（默认的 string conver 的编码集为"ISO-8859-1"）
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
//        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
//        while (iterator.hasNext()) {
//            HttpMessageConverter<?> converter = iterator.next();
//            if (converter instanceof StringHttpMessageConverter) {
//                iterator.remove();
//            }
//        }

        messageConverters.add(new MappingJackson2HttpMessageConverter());
        //messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        // messageConverters.add(new FormHttpMessageConverter());


        return restTemplate;
    }

    @Bean
    @ConditionalOnMissingBean({ClientHttpRequestFactory.class})
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() throws Exception {
        //SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        TrustStrategy acceptingTrustStrategy = (chain, authType) -> true;
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        HttpClientBuilder clientBuilder = HttpClients.custom();

        CloseableHttpClient httpClient = clientBuilder.setSSLSocketFactory(sslsf).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);


        //设置超时时间
        requestFactory.setReadTimeout(15000);// ms
        requestFactory.setConnectTimeout(15000);// ms
        return requestFactory;
    }
}
