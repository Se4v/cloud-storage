package org.example.backend.common.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @return jwt
     */
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(
                        Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)),
                        Jwts.SIG.HS256
                )
                .compact();
    }

    /**
     * 解析JWT令牌
     * @param token JWT令牌
     * @return 令牌参数
     * @throws JwtException 解析异常
     */
    public Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 校验JWT令牌
     * @param token JWT令牌
     * @return 校验结果
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            // Token已过期
            return false;
        } catch (UnsupportedJwtException | MalformedJwtException |
                 SecurityException | IllegalArgumentException e) {
            // Token格式错误或签名无效
            return false;
        }
    }
}
