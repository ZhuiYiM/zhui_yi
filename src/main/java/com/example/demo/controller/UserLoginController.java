package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.LoginDTO;
import com.example.demo.entity.dto.RegisterDTO;
import com.example.demo.service.UserService;
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
}