package com.example.demo.interceptor;

import com.example.demo.utils.JwtUtil;
import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 检查是否为排除路径（包括 API 前缀和特定的公开接口）
        String uri = request.getRequestURI();
        if (uri.startsWith("/auth/") || 
            uri.startsWith("/number/") || 
            uri.startsWith("/upload/") ||
            // 排除所有非 /api/ 开头的路径（前端路由不拦截）
            (!uri.startsWith("/api/") && !uri.startsWith("/products/") && 
             !uri.startsWith("/topics/") && !uri.startsWith("/messages/") && 
             !uri.startsWith("/campus/"))) {
            return true; // 直接放行
        }

        // 放行话题公开的 GET 请求（获取列表、详情）
        if (uri.startsWith("/api/topics") && "GET".equalsIgnoreCase(request.getMethod())) {
            // 放行 /api/topics 和 /api/topics/{id}，但不放行 /api/topics/{id}/forward 等操作
            if (!uri.matches(".*/topics/\\d+/(forward|like|collect|top|essence).*$")) {
                return true;
            }
        }

        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();
            out.print(Result.error("缺少Token"));
            out.flush();
            out.close();
            return false;
        }

        try {
            // 验证 token 有效性
            if (jwtUtil.validateTokenWithIdOrUsername(token)) {
                // Token 有效，提取 userId 并设置到 request attribute 中
                Integer userId = jwtUtil.getUserIdFromToken(token);
                if (userId != null) {
                    request.setAttribute("userId", userId.longValue());
                }
                // 继续处理请求
                return true;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
        
                PrintWriter out = response.getWriter();
                out.print(Result.error("Token 无效"));
                out.flush();
                out.close();
                return false;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
        
            PrintWriter out = response.getWriter();
            out.print(Result.error("Token 验证失败"));
            out.flush();
            out.close();
            return false;
        }
    }


}
