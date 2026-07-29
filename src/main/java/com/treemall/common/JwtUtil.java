package com.treemall.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类
 * 两大功能：
 * 1. generateToken() — 登录成功后生成 Token
 * 2. parseToken()   — 每次请求时解析 Token，提取 userId 和 role
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 获取签名密钥
     * HS256 算法要求密钥长度 >= 256 位（32 字节）
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT Token
     *
     * @param userId 用户ID
     * @param role   用户角色（consumer / merchant）
     * @return Token 字符串
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))       // 主题：存 userId
                .claim("role", role)                   // 自定义字段：存角色
                .issuedAt(now)                         // 签发时间
                .expiration(expireDate)                // 过期时间
                .signWith(getKey())                    // 签名
                .compact();
    }

    /**
     * 解析 Token 中的 Claims（声明信息）
     *
     * @param token JWT Token 字符串
     * @return Claims 对象，可从中提取 userId 和 role
     * @throws io.jsonwebtoken.JwtException Token 无效或过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中提取 userId
     */
    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    /**
     * 从 Token 中提取用户角色
     */
    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }
}