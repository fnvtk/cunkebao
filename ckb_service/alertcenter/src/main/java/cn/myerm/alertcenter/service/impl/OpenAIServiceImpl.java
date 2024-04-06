package cn.myerm.alertcenter.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.myerm.alertcenter.api.MyAuthenticationInterceptor;
import cn.myerm.alertcenter.service.IOpenAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.client.OpenAiApi;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Retrofit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static com.theokanning.openai.service.OpenAiService.defaultRetrofit;

@Service
public class OpenAIServiceImpl implements IOpenAIService {

    @Value("${openai.proxyhost}")
    private String proxyHost;

    @Value("${openai.proxyport}")
    private int proxyPort;

    @Value("${openai.apikey}")
    private String apikey;

    public static OkHttpClient defaultClient(String token, Duration timeout) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addInterceptor(new MyAuthenticationInterceptor(token))
                .connectionPool(new ConnectionPool(5, 1L, TimeUnit.SECONDS))
                .readTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);

//        builder.addInterceptor(new HttpLogInterceptor());

        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        OkHttpClient build = builder.build();
        return build;
    }

    public List<ChatCompletionChoice> getAiResult(String prompt) {
        //构建HTTP代理
        Proxy proxy = null;
        if (StrUtil.isNotBlank(proxyHost)) {
            proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        }
        //构建HTTP客户端
        OkHttpClient client = defaultClient(apikey, Duration.of(300, ChronoUnit.SECONDS))
                .newBuilder()
                .proxy(proxy)
                .build();
        ObjectMapper mapper = defaultObjectMapper();
        Retrofit retrofit = defaultRetrofit(client, mapper);
        OpenAiApi api = retrofit.create(OpenAiApi.class);
        OpenAiService openAiService = new OpenAiService(api, client.dispatcher().executorService());

        List<ChatMessage> messages = new ArrayList<>();
        ChatMessage chatMessage = new ChatMessage(ChatMessageRole.USER.value(), prompt);
        messages.add(chatMessage);

        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .user(ChatMessageRole.USER.value())
                .n(5)
                .build();
        ChatCompletionResult chatCompletion = openAiService.createChatCompletion(completionRequest);
        return chatCompletion.getChoices();
    }
}
