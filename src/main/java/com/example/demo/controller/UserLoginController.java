package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.LoginDTO;
import com.example.demo.entity.dto.RegisterDTO;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户登录控制器
 * 处理 /user 路径下的登录相关请求，提供向前兼容性
 */
@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录接口
     * POST /user/login
     */
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    /**
     * 用户注册接口
     * POST /user/register
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    /**
     * 发送邮箱验证码
     * POST /user/send-verification-code
     */
    @PostMapping("/send-verification-code")
    public Result sendVerificationCode(@RequestParam String email) {
        // 这里需要注入MailService，暂时返回模拟响应
        return Result.success("验证码已发送");
    }

    /**
     * 手机号登录
     * POST /user/login/phone
     */
    @PostMapping("/login/phone")
    public Result loginByPhone(@RequestBody java.util.Map<String, String> request) {
        return userService.loginByPhone(request);
    }

    /**
     * 用户登出
     * POST /user/logout
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // TODO: 实现 Token 黑名单机制或清除 Redis 中的 Token
        return Result.success("登出成功");
    }

    /**
     * 刷新 Token
     * POST /user/refresh
     */
    @PostMapping("/refresh")
    public Result refreshToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        try {
            // 验证 Token 是否有效
            if (!jwtUtil.validateTokenWithIdOrUsername(token)) {
                return Result.error(401, "Token 已过期");
            }
            
            // 获取用户信息并生成新 Token
            String username = jwtUtil.getUsernameFromToken(token);
            Integer userId = jwtUtil.getUserIdFromToken(token);
            
            // 使用 refreshToken 方法刷新 Token
            String newToken = jwtUtil.refreshToken(token);
            
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("token", newToken);
            
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(401, "Token 刷新失败：" + e.getMessage());
        }
    }
}