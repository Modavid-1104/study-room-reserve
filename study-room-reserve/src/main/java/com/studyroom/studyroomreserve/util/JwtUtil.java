package com.studyroom.studyroomreserve.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    // 密钥，生产环境不要写死！测试先用这个
    private static final String SECRET = "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz";
    // 24小时过期，单位毫秒
    private static final long EXPIRE_TIME =24 * 60 * 60 * 1000;

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

    // 生成token，存入userId
    public static String generateToken(Long userId) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + EXPIRE_TIME);
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(now)
                .expiration(expire)
                .signWith(getKey())
                .compact();
    }

    // 解析token，拿到userId
    public static Long getUserId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    // 判断token是否合法
    public static boolean verifyToken(String token) {
        try {
            getUserId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
