package com.homebeauty.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtUtil {

    private static final String SECRET_KEY = "homebeauty_secret_key_2024_jwt_token_very_long_secret_key_for_security";
    private static final long EXPIRE_TIME = 24 * 60 * 60 * 1000;

    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    public static String generateToken(Long userId, String role) {
        log.debug("生成token: userId={}, role={}", userId, role);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + EXPIRE_TIME);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();

        log.debug("token生成成功");
        return token;
    }

    public static Claims parseToken(String token) {
        log.debug("解析token: {}", token);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("token解析成功: userId={}, role={}", claims.get("userId"), claims.get("role"));
            return claims;
        } catch (Exception e) {
            log.error("token解析失败: {}", e.getMessage());
            return null;
        }
    }

    public static boolean validateToken(String token) {
        log.debug("验证token: {}", token);
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return false;
            }
            Date expiration = claims.getExpiration();
            boolean valid = !expiration.before(new Date());
            log.debug("token验证结果: {}", valid);
            return valid;
        } catch (Exception e) {
            log.error("token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object userId = claims.get("userId");
            if (userId != null) {
                return Long.valueOf(userId.toString());
            }
        }
        return null;
    }

    public static String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            Object role = claims.get("role");
            if (role != null) {
                return role.toString();
            }
        }
        return null;
    }
}
