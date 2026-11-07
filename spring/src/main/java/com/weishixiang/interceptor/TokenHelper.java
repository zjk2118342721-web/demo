package com.weishixiang.interceptor;

import jakarta.servlet.http.HttpServletRequest;

public class TokenHelper {

    private TokenHelper() {
    }

    public static String extract(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && !authorization.isBlank()) {
            return authorization.startsWith("Bearer ")
                    ? authorization.substring(7).trim()
                    : authorization.trim();
        }
        String token = request.getHeader("token");
        return token == null ? null : token.trim();
    }
}
