package com.weishixiang.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expire}")
    private long expire;

    @Value("${app.jwt.admin-expire}")
    private long adminExpire;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateUserToken(Long userId) {
        return generateToken(userId, "user", expire);
    }

    public String generateAdminToken(Long adminId) {
        return generateToken(adminId, "admin", adminExpire);
    }

    private String generateToken(Long id, String type, long ttlSeconds) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(id))
                .claim("type", type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlSeconds * 1000))
                .signWith(key())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
