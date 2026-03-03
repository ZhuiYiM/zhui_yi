package com.example.demo.controller;

import com.example.demo.common.Result;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 首页控制器
 * 处理根路径访问，返回 API 信息提示
 */
@RestController
@CrossOrigin
public class IndexController {

    /**
     * 根路径处理
     * 返回 API 欢迎信息和基本说明
     */
    @GetMapping("/")
    public Result index() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "校园信息平台 API");
        info.put("version", "1.0.0");
        info.put("description", "欢迎使用校园信息平台后端 API 服务");
        
        return Result.success(info);
    }
}
