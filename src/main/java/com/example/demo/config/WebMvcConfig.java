package com.example.demo.config;

import com.example.demo.interceptor.TokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类 - 注册拦截器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private TokenInterceptor tokenInterceptor;

    /**
     * 添加拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/api/**")  // 拦截所有 API 请求
                .excludePathPatterns(
                    "/api/user/login",           // 登录
                    "/api/user/register",        // 注册
                    "/api/user/send-verification-code",  // 发送验证码
                    "/api/user/verify-code",    // 验证码验证
                    "/api/user/password/recovery/code",  // 找回密码验证码
                    "/api/user/password/reset", // 重置密码
                    "/api/user/login/phone",    // 手机号登录
                    "/api/user/send-phone-code", // 发送手机验证码
                    "/api/user/wechat/**",      // 微信登录相关
                    "/api/campuses/**",         // 校区相关接口
                    "/api/products/**",         // 商品相关（部分公开）
                    "/api/messages/**",         // 消息相关（部分公开）
                    "/api/tags/level1",         // 获取身份标签（公开）
                    "/api/tags/topics",         // 获取话题标签（公开）
                    "/api/tags/locations",      // 获取地点标签（公开）
                    "/api/tags/products",       // 获取商品标签（公开）
                    "/api/tags/**/hot"          // 获取热门标签（公开）
                );
    }
}
