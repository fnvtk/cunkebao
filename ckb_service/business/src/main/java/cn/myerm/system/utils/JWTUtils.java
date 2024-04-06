package cn.myerm.system.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Calendar;

public class JWTUtils {
    private static final String KEY = "12313ERWQER231234123";

    //获取token
    public static String getToken(String stoken) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DATE, 7); //默认令牌过期时间7天

        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("token", stoken);

        String token = builder.withExpiresAt(instance.getTime())
                .sign(Algorithm.HMAC256(KEY));
        return token;
    }

    //验证token合法性 成功返回token
    public static DecodedJWT verify(String token) {
        JWTVerifier build = JWT.require(Algorithm.HMAC256(KEY)).build();
        DecodedJWT verify = build.verify(token);
        return verify;
    }
}
