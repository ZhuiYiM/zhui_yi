package com.example.demo.aspect;

import com.example.demo.annotation.RequireIdentity;
import com.example.demo.annotation.RequireRole;
import com.example.demo.common.Result;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Map;

/**
 * 权限检查切面
 */
@Aspect
@Component
public class PermissionAspect {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 处理@RequireIdentity 注解
     */
    @Around("@annotation(requireIdentity)")
    public Object checkIdentity(ProceedingJoinPoint pjp, RequireIdentity requireIdentity) throws Throwable {
        // 获取请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        // 从请求头获取 Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.trim().isEmpty()) {
            return Result.error("请先登录");
        }
        
        // 验证 Token 有效性
        if (!jwtUtil.validateTokenWithIdOrUsername(token)) {
            return Result.error("登录已过期");
        }
        
        // 获取需要的身份类型
        String[] requiredIdentities = requireIdentity.value();
        boolean requireVerified = requireIdentity.verified();
        
        if (requiredIdentities.length == 0) {
            // 没有指定具体身份，只需要有任一身份即可
            List<Map<String, Object>> identities = jwtUtil.getIdentitiesFromToken(token);
            if (identities == null || identities.isEmpty()) {
                return Result.error("需要完成身份认证");
            }
            
            if (requireVerified) {
                boolean hasVerifiedIdentity = identities.stream()
                    .anyMatch(identity -> Boolean.TRUE.equals(identity.get("verified")));
                if (!hasVerifiedIdentity) {
                    return Result.error("需要已完成认证的身份");
                }
            }
        } else {
            // 检查是否具有指定的任一身份
            boolean hasRequiredIdentity = false;
            for (String identityType : requiredIdentities) {
                if (jwtUtil.hasIdentity(token, identityType, requireVerified)) {
                    hasRequiredIdentity = true;
                    break;
                }
            }
            
            if (!hasRequiredIdentity) {
                String message = requireVerified ? "需要已认证的" : "需要";
                return Result.error(message + getIdentityNames(requiredIdentities));
            }
        }
        
        // 权限检查通过，继续执行
        return pjp.proceed();
    }
    
    /**
     * 处理@RequireRole 注解
     */
    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint pjp, RequireRole requireRole) throws Throwable {
        // 获取请求
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        
        // 从请求头获取 Token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token == null || token.trim().isEmpty()) {
            return Result.error("请先登录");
        }
        
        // 验证 Token 有效性
        if (!jwtUtil.validateTokenWithIdOrUsername(token)) {
            return Result.error("登录已过期");
        }
        
        // 获取用户角色
        String userRole = jwtUtil.getRoleFromToken(token);
        
        // 获取需要的角色列表
        String[] requiredRoles = requireRole.value();
        
        if (requiredRoles.length == 0) {
            // 没有指定具体角色，默认需要 admin 角色
            if (!"admin".equals(userRole)) {
                return Result.error("需要管理员权限");
            }
        } else {
            // 检查是否具有指定的任一角色
            boolean hasRequiredRole = false;
            for (String role : requiredRoles) {
                if (role.equals(userRole)) {
                    hasRequiredRole = true;
                    break;
                }
            }
            
            if (!hasRequiredRole) {
                return Result.error("权限不足");
            }
        }
        
        // 权限检查通过，继续执行
        return pjp.proceed();
    }
    
    /**
     * 获取身份名称列表（用于错误提示）
     */
    private String getIdentityNames(String[] identityTypes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < identityTypes.length; i++) {
            switch (identityTypes[i]) {
                case "student":
                    sb.append("学生");
                    break;
                case "staff":
                    sb.append("教职工");
                    break;
                case "merchant":
                    sb.append("商户");
                    break;
                case "organization":
                    sb.append("团体/部门");
                    break;
                default:
                    sb.append(identityTypes[i]);
            }
            
            if (i < identityTypes.length - 1) {
                sb.append("或");
            }
        }
        return sb.toString();
    }
}
