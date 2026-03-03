// config/WebConfig.java
package com.example.demo.config;

import com.example.demo.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry; // 添加此行

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
}
