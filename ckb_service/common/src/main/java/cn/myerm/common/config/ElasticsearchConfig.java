package cn.myerm.common.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    private ElasticsearchTransport transport;

    /**
     * 客户端
     *
     * @return
     * @throws IOException
     */
    public ElasticsearchClient configClint() throws IOException {
        // Create the low-level client
        RestClient restClient = RestClient.builder(
                new HttpHost(host, port)).build();

        // Create the transport with a Jackson mapper
        transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // 客户端
        return new ElasticsearchClient(transport);
    }

    /**
     * 关闭客户端
     * @throws IOException
     */
    public void close() throws IOException {
        transport.close();
    }
}
