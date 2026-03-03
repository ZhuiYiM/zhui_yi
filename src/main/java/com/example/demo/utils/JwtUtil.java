package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:myVeryLongSecretKey123456789012345678901234567890123456789012345678901234567890}")
    private String secret;

    @Value("${jwt.expiration:86400}")
    private Long expiration; // 默认24小时
    
    @Value("${jwt.clock-skew:300}")
    private Long clockSkew; // 时钟偏差容忍度，默认5分钟

    private SecretKey getSigningKey() {
        // 将密钥字符串转换为字节数组
        byte[] keyBytes = secret.getBytes();

        // 确保密钥长度至少为512位（64字节）以满足HS512要求
        if (keyBytes.length < 64) {
            // 创建一个64字节的数组
            byte[] extendedKey = new byte[64];
            // 循环填充密钥直到达到64字节
            for (int i = 0; i < 64; i++) {
                extendedKey[i] = keyBytes[i % keyBytes.length];
            }
            return Keys.hmacShaKeyFor(extendedKey);
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成Token
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }

            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            System.err.println("获取Token用户名时发生错误: " + e.getMessage());
            return null;
        }
    }


    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token, String username) {
        try {
            String tokenUsername = getUsernameFromToken(token);
            return tokenUsername != null && tokenUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("验证Token时发生错误: " + e.getMessage());
            return false;
        }
    }

    /**
     * 判断Token是否过期
     */
    private boolean isTokenExpired(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            Date now = new Date();
            // 考虑时钟偏差
            Date adjustedNow = new Date(now.getTime() - clockSkew * 1000);
            return expirationDate.before(adjustedNow);
        } catch (Exception e) {
            // 记录错误日志
            System.err.println("检查Token过期时发生错误: " + e.getMessage());
            return true; // 如果无法判断，视为已过期
        }
    }

    /**
     * 从Token中获取过期时间
     */
    private Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    /**
     * 从Token中获取Claims
     */
    // 替换旧的解析器创建方式
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 生成包含用户 ID 的 Token
     */
    public String generateToken(String username, Integer userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);

        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)  // 添加用户 ID 作为 claim
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    // 从Token中获取用户ID
    public Integer getUserIdFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }
            Claims claims = getClaimsFromToken(token);
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            System.err.println("获取Token用户ID时发生错误: " + e.getMessage());
            return null;
        }
    }

    // 验证Token是否有效（基于用户ID）
    public boolean validateTokenUsingId(String token, Integer userId) {
        try {
            Integer tokenUserId = getUserIdFromToken(token);
            return tokenUserId != null && tokenUserId.equals(userId) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通用Token验证方法 - 支持用户名或用户ID验证
     */
    public boolean validateTokenWithIdOrUsername(String token) {
        try {
            String username = getUsernameFromToken(token);
            Integer userId = getUserIdFromToken(token);
            boolean tokenNotExpired = !isTokenExpired(token);

            // 只要用户名或用户ID存在且未过期即可
            return (username != null || userId != null) && tokenNotExpired;
        } catch (Exception e) {
            System.err.println("验证Token时发生错误: " + e.getMessage());
            return false;
        }
    }
    /**
     * 刷新Token
     */
    public String refreshToken(String oldToken) {
        try {
            if (oldToken == null || oldToken.trim().isEmpty()) {
                return null;
            }

            Claims claims = getClaimsFromToken(oldToken);
            String username = claims.getSubject();
            Integer userId = claims.get("userId", Integer.class);

            // 生成新的Token
            if (userId != null) {
                return generateToken(username, userId);
            } else {
                return generateToken(username);
            }
        } catch (Exception e) {
            System.err.println("刷新Token时发生错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查Token是否需要刷新（接近过期时）
     */
    public boolean shouldRefreshToken(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            Date now = new Date();
            // 如果距离过期还有不到1天，建议刷新
            long timeToExpire = expirationDate.getTime() - now.getTime();
            return timeToExpire < 24 * 60 * 60 * 1000; // 24小时
        } catch (Exception e) {
            return true; // 出错时也建议刷新
        }
    }
}
