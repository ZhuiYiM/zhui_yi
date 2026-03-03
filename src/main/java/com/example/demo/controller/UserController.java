// controller/UserController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.LoginDTO;
import com.example.demo.entity.dto.RegisterDTO;
import com.example.demo.entity.dto.UserProfileDTO;
import com.example.demo.service.MailService;
import com.example.demo.service.UserService;
import com.example.demo.service.SmsService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;
    
    @Autowired
    private SmsService smsService;
    
    @Autowired
    private JwtUtil jwtUtil;
    // 发送邮箱验证码
    @PostMapping("/send-verification-code")
    public Result sendVerificationCode(@RequestParam String email) {
        mailService.sendVerificationCode(email);
        return Result.success("验证码已发送");
    }

    // 用户登录
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    // 用户注册
    @PostMapping("/register")
    @SuppressWarnings("unchecked")
    public Result register(@RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    // 获取用户资料
    @GetMapping("/profile")
    public Result getUserProfile(HttpServletRequest request) {
        return userService.getUserProfile(request);
    }

    // 更新用户资料
    @PutMapping("/profile")
    public Result updateUserProfile(@RequestBody UserProfileDTO profileDTO, HttpServletRequest request) {
        return userService.updateUserProfile(profileDTO, request);
    }

    // 获取用户隐私设置
    @GetMapping("/{userId}/privacy")
    public Result getPrivacySettings(@PathVariable Integer userId) {
        return userService.getPrivacySettings(userId);
    }

    // 更新用户隐私设置
    @PostMapping("/{userId}/privacy")
    @SuppressWarnings("unchecked")
    public Result updatePrivacySettings(@PathVariable Integer userId,
                                        @RequestBody Map<String, Object> requestBody) {
        Map<String, Boolean> privacySettings = (Map<String, Boolean>) requestBody.get("privacy_settings");
        String defaultLevel = (String) requestBody.get("default_privacy_level");
        return userService.updatePrivacySettings(userId, privacySettings, defaultLevel);
    }

    // 上传头像
    @PostMapping("/{userId}/avatar")
    public Result uploadAvatar(@PathVariable Integer userId,
                               @RequestParam MultipartFile file) {
        return userService.uploadAvatar(file, userId);
    }

    // 更新用户电话
    @PutMapping("/profile/phone")
    public Result updatePhoneNumber(@RequestParam String phoneNumber, HttpServletRequest request) {
        return userService.updatePhoneNumber(phoneNumber, request);
    }

    // 更新用户邮箱
    @PutMapping("/profile/email")
    public Result updateEmail(@RequestParam String email, HttpServletRequest request) {
        return userService.updateEmail(email, request);
    }

    // 更新用户学号
    @PutMapping("/profile/student-id")
    public Result updateStudentId(@RequestParam String studentId, HttpServletRequest request) {
        return userService.updateStudentId(studentId, request);
    }

    // 获取用户订单列表
    @GetMapping("/{userId}/orders")
    public Result getUserOrders(@PathVariable Integer userId,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        // TODO: 需要订单服务来实现
        return Result.success("获取用户订单列表功能待实现");
    }

    // 获取用户发布的话题
    @GetMapping("/{userId}/topics")
    public Result getUserTopics(@PathVariable Integer userId,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        // TODO: 需要话题服务来实现
        return Result.success("获取用户话题列表功能待实现");
    }

    // 获取用户认证状态
    @GetMapping("/{userId}/auth-status")
    public Result getAuthStatus(@PathVariable Integer userId) {
        // TODO: 实现认证状态查询
        return Result.success("获取认证状态功能待实现");
    }

    // 申请身份认证
    @PostMapping("/{userId}/verify-identity")
    public Result applyIdentityVerification(@PathVariable Integer userId,
                                            @RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        if ("cancel".equals(action)) {
            // 取消认证
            return Result.success("已取消身份认证");
        } else {
            // 申请认证
            return Result.success("身份认证申请功能开发中");
        }
    }

    // 申请实名认证
    @PostMapping("/{userId}/verify-realname")
    public Result applyRealNameVerification(@PathVariable Integer userId,
                                            @RequestBody Map<String, Object> request) {
        String action = (String) request.get("action");
        if ("cancel".equals(action)) {
            // 取消认证
            return Result.success("已取消实名认证");
        } else {
            // 申请认证
            return Result.success("实名认证申请功能开发中");
        }
    }
    // ... existing code ...

    // 手机号验证码登录
    @PostMapping("/login/phone")
    public Result loginByPhone(@RequestBody Map<String, String> request) {
        return userService.loginByPhone(request);
    }

    // 微信扫码登录相关接口
    @GetMapping("/wechat/qrcode")
    public Result getWechatQRCode() {
        return userService.getWechatQRCode();
    }

    @GetMapping("/wechat/scan-status/{qrCodeId}")
    public Result getScanStatus(@PathVariable String qrCodeId) {
        return userService.getWechatScanStatus(qrCodeId);
    }

    // 找回密码 - 发送验证码
    @PostMapping("/password/recovery/code")
    public Result sendRecoveryCode(@RequestParam String identifier,
                                   @RequestParam String type) {
        return userService.sendRecoveryCode(identifier, type);
    }

    // 找回密码 - 重置密码
    @PostMapping("/password/reset")
    public Result resetPassword(@RequestBody Map<String, String> request) {
        return userService.resetPassword(request);
    }

    /**
     * 实名认证申请接口
     * POST /user/verification/realname
     */
    @PostMapping("/verification/realname")
    public Result realNameVerification(@RequestBody Map<String, Object> request,
                                     HttpServletRequest httpRequest) {
        try {
            // 从Token中获取用户ID
            Integer userId = extractUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }

            String realName = (String) request.get("realName");
            String idCard = (String) request.get("idCard");

            if (realName == null || realName.trim().isEmpty()) {
                return Result.error("真实姓名不能为空");
            }
            if (idCard == null || idCard.trim().isEmpty()) {
                return Result.error("身份证号码不能为空");
            }

            return userService.applyRealNameVerification(userId, realName, idCard);
        } catch (Exception e) {
            return Result.error("实名认证申请失败: " + e.getMessage());
        }
    }
    
    /**
     * 从Token中提取用户ID的辅助方法
     */
    private Integer extractUserIdFromToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (token != null && !token.trim().isEmpty()) {
            return jwtUtil.getUserIdFromToken(token);
        }
        
        return null;
    }

// ... existing code ...

}
