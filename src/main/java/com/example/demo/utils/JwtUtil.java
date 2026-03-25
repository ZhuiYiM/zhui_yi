package com.example.demo.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
     * 从 Token 中获取 Claims
     */
    // 替换旧的解析器创建方式
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .setAllowedClockSkewSeconds(clockSkew) // 设置时钟偏差容忍度
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

    /**
     * 生成包含用户 ID 和身份信息的 Token
     */
    public String generateToken(String username, Integer userId, String role, List<Map<String, Object>> identities) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration * 1000);
    
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role != null ? role : "user")
                .claim("identities", identities != null ? identities : Collections.emptyList())
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
        
    /**
     * 从 Token 中获取角色信息
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            System.err.println("获取 Token 角色时发生错误：" + e.getMessage());
            return null;
        }
    }
        
    /**
     * 从 Token 中获取身份信息列表
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getIdentitiesFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return (List<Map<String, Object>>) claims.get("identities");
        } catch (Exception e) {
            System.err.println("获取 Token 身份信息时发生错误：" + e.getMessage());
            return null;
        }
    }
        
    /**
     * 检查用户是否具有指定身份
     */
    public boolean hasIdentity(String token, String identityType, boolean requireVerified) {
        try {
            List<Map<String, Object>> identities = getIdentitiesFromToken(token);
            if (identities == null || identities.isEmpty()) {
                return false;
            }
                
            for (Map<String, Object> identity : identities) {
                String type = (String) identity.get("type");
                Boolean verified = (Boolean) identity.get("verified");
                    
                if (identityType.equals(type)) {
                    if (!requireVerified || (verified != null && verified)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("检查身份时发生错误：" + e.getMessage());
            return false;
        }
    }
    /**
     * 从 Token 中获取用户 ID
     */
    public Integer getUserIdFromToken(String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return null;
            }
            Claims claims = getClaimsFromToken(token);
            return claims.get("userId", Integer.class);
        } catch (Exception e) {
            System.err.println("获取 Token 用户 ID 时发生错误：" + e.getMessage());
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
