package cn.myerm.alertcenter.api;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class MyAuthenticationInterceptor implements Interceptor {

    private final String authToken;

    public MyAuthenticationInterceptor(String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // 添加自定义的Token或其他认证信息到请求头中
        Request.Builder builder = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + authToken);

        Request newRequest = builder.build();
        return chain.proceed(newRequest);
    }
}
