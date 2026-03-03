// config/WebConfig.java
package com.example.demo.config;

import com.example.demo.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 对需要验证Token的接口添加拦截器
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**", "/products/**", "/topics/**", "/messages/**", "/campus/**")
                .excludePathPatterns("/auth/**", "/number/**", "/upload/**");
    }


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*") // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                .allowedHeaders("*") // 允许的头部
                .allowCredentials(false); // 是否允许携带凭证（如 Cookie）
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 忽略 API 路径的静态资源检查，防止 NoResourceFoundException
        registry.addResourceHandler(
                "/user/profile",
                "/user/**", 
                "/topics/**", 
                "/products/**", 
                "/messages/**", 
                "/campus/**",
                "/tags/**",
                "/auth/**",
                "/number/**",
                "/upload/**",
                "/api/**"
        )
                .addResourceLocations("classpath:/static/");
        
        // 不处理其他路径的静态资源请求，让前后端分离架构更清晰
        // 前端应由独立的开发服务器（如 Vite）提供服务
    }
}
