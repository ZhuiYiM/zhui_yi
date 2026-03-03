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

        // 检查是否为排除路径
        String uri = request.getRequestURI();
        if (uri.startsWith("/auth/") || 
            uri.startsWith("/number/") || 
            uri.startsWith("/upload/")) {
            return true; // 直接放行
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
            // 验证token有效性
            if (jwtUtil.validateTokenWithIdOrUsername(token)) {
                // Token有效，继续处理请求
                return true;
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");

                PrintWriter out = response.getWriter();
                out.print(Result.error("Token无效"));
                out.flush();
                out.close();
                return false;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");

            PrintWriter out = response.getWriter();
            out.print(Result.error("Token验证失败"));
            out.flush();
            out.close();
            return false;
        }
    }


}
