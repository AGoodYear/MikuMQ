package com.ivmiku.mikumq.utils;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTException;
import cn.hutool.jwt.JWTUtil;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * token授权验证
 * @author Aurora
 */
public class AuthUtil {
    private static String key = "default";
    private static Long expireTime = 1000L * 60 * 60 * 24 * 30;

    static {
        HashMap<String, String> params = ConfigUtil.getAuthConfig();
        key = params.get("key");
        expireTime = Integer.parseInt(params.get("expireTime")) * 1000L;
    }

    /**
     * 创建token
     * @param username 用户名
     * @return 创建的token
     */
    public static String getToken(String username) {
        Map<String, Object> map = new HashMap<>(2) {
            @Serial
            private static final long serialVersionUID = 1L;
            {
                put("username", username);
                put("expire_time", System.currentTimeMillis() + expireTime);
            }
        };
        return JWTUtil.createToken(map, key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证token是否有效
     * @param token 要验证的token
     * @return 验证结果
     */
    public static boolean validate(String token) {
        try {
            return JWT.of(token).setKey(key.getBytes(StandardCharsets.UTF_8)).validate(300);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从token中获取用户名
     * @param token token
     * @return token包含的用户名
     */
    public static String getUsername(String token) {
        return (String) JWT.of(token).setKey(key.getBytes(StandardCharsets.UTF_8)).getPayload("username");
    }
}
