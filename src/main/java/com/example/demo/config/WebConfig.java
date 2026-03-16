package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类 - 配置静态资源映射
 * 将本地文件路径映射到 URL，使上传的图片可以通过浏览器访问
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 添加静态资源处理器
     * 将 /images/** 的 URL 映射到本地的 uploads/images/ 目录
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射图片资源
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/images/");
        
        // 可以添加更多资源映射，如视频、文档等
        // registry.addResourceHandler("/videos/**")
        //         .addResourceLocations("file:uploads/videos/");
    }
}
