// controller/UserController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.LoginDTO;
import com.example.demo.entity.dto.RegisterDTO;
import com.example.demo.entity.dto.UserProfileDTO;
import com.example.demo.service.MailService;
import com.example.demo.service.UserService;
import com.example.demo.service.SmsService;
import com.example.demo.service.IdentityService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")  // 修改为单数形式，符合 RESTful 规范
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
    
    @Autowired
    private IdentityService identityService;
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
     * 从 Token 中提取用户 ID 的辅助方法
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

    // ==================== 以下接口根据前端需求补充 ====================

    /**
     * 获取认证状态详情
     * GET /api/users/verification/status
     */
    @GetMapping("/verification/status")
    public Result getVerificationStatus(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.getAuthStatus(userId);
    }

    /**
     * 申请学生身份认证
     * POST /api/users/verification/identity
     */
    @PostMapping("/verification/identity")
    public Result applyIdentityVerification(@RequestBody Map<String, Object> request,
                                            HttpServletRequest httpRequest) {
        try {
            Integer userId = extractUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error(401, "用户未登录");
            }

            String studentId = (String) request.get("studentId");
            String realName = (String) request.get("realName");
            String college = (String) request.get("college");

            if (studentId == null || realName == null) {
                return Result.error("学号和真实姓名不能为空");
            }

            return userService.applyIdentityVerification(userId, studentId, realName, college);
        } catch (Exception e) {
            return Result.error("认证申请失败：" + e.getMessage());
        }
    }

    /**
     * 获取认证申请历史
     * GET /api/users/verification/applications
     */
    @GetMapping("/verification/applications")
    public Result getVerificationApplications(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.getVerificationApplications(userId, page, size);
    }

    /**
     * 取消认证申请
     * DELETE /api/user/verification/applications/{applicationId}
     */
    @DeleteMapping("/verification/applications/{applicationId}")
    public Result cancelVerificationApplication(@PathVariable Integer applicationId,
                                                 HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.cancelVerificationApplication(applicationId, userId);
    }

    // ==================== 安全设置相关接口 ====================

    /**
     * 获取安全设置
     * GET /api/user/account/security
     */
    @GetMapping("/account/security")
    public Result getSecuritySettings(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.getSecuritySettings(userId);
    }

    /**
     * 更新安全设置
     * PUT /api/user/account/security
     */
    @PutMapping("/account/security")
    public Result updateSecuritySettings(@RequestBody Map<String, Object> settings,
                                         HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.updateSecuritySettings(settings, userId);
    }

    /**
     * 获取隐私设置
     * GET /api/user/account/privacy
     */
    @GetMapping("/account/privacy")
    public Result getPrivacySettingsDetail(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.getPrivacySettings(userId);
    }

    /**
     * 更新隐私设置
     * PUT /api/user/account/privacy
     */
    @PutMapping("/account/privacy")
    public Result updatePrivacySettingsDetail(@RequestBody Map<String, Object> requestBody,
                                              HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        @SuppressWarnings("unchecked")
        Map<String, Boolean> privacySettings = (Map<String, Boolean>) requestBody.get("settings");
        String defaultLevel = (String) requestBody.get("defaultLevel");
        
        return userService.updatePrivacySettings(userId, privacySettings, defaultLevel);
    }

    /**
     * 修改密码
     * PUT /api/user/account/password
     */
    @PutMapping("/account/password")
    public Result changePassword(@RequestBody Map<String, String> passwords,
                                 HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        String oldPassword = passwords.get("oldPassword");
        String newPassword = passwords.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return Result.error("密码不能为空");
        }
        
        return userService.changePassword(oldPassword, newPassword, request);
    }

    /**
     * 获取登录设备列表
     * GET /api/user/account/devices
     */
    @GetMapping("/account/devices")
    public Result getLoginDevices(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.getLoginDevices(userId);
    }

    /**
     * 退出指定设备
     * DELETE /api/user/account/devices/{deviceId}
     */
    @DeleteMapping("/account/devices/{deviceId}")
    public Result logoutDevice(@PathVariable Integer deviceId,
                               HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.logoutDevice(deviceId, userId);
    }

    /**
     * 退出当前设备
     * POST /api/user/account/logout-current
     */
    @PostMapping("/account/logout-current")
    public Result logoutCurrentDevice(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.logoutCurrentDevice(userId);
    }

    /**
     * 下载用户数据
     * GET /api/user/account/data/export
     */
    @GetMapping("/account/data/export")
    public Result downloadUserData(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.downloadUserData(userId);
    }

    /**
     * 注销账号
     * DELETE /api/user/account
     */
    @DeleteMapping("/account")
    public Result deleteAccount(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.deleteAccount(userId);
    }

    /**
     * 绑定手机号
     * POST /api/user/account/bind/phone
     */
    @PostMapping("/account/bind/phone")
    public Result bindPhone(@RequestBody Map<String, String> request,
                           HttpServletRequest httpRequest) {
        Integer userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        String phone = request.get("phone");
        String code = request.get("code");
        
        if (phone == null || code == null) {
            return Result.error("手机号和验证码不能为空");
        }
        
        // TODO: 先验证验证码
        return userService.bindPhone(phone, userId);
    }

    /**
     * 绑定邮箱
     * POST /api/user/account/bind/email
     */
    @PostMapping("/account/bind/email")
    public Result bindEmail(@RequestBody Map<String, String> request,
                           HttpServletRequest httpRequest) {
        Integer userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        String email = request.get("email");
        String code = request.get("code");
        
        if (email == null || code == null) {
            return Result.error("邮箱和验证码不能为空");
        }
        
        // TODO: 先验证验证码
        return userService.bindEmail(email, userId);
    }

    /**
     * 解绑社交账号
     * DELETE /api/user/account/unbind/social/{platform}
     */
    @DeleteMapping("/account/unbind/social/{platform}")
    public Result unbindSocial(@PathVariable String platform,
                               HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return userService.unbindSocial(platform, userId);
    }

    /**
     * 获取用户公开信息
     * GET /api/user/{userId}/public-info
     */
    @GetMapping("/{userId}/public-info")
    public Result getUserPublicInfo(@PathVariable Integer userId) {
        return userService.getUserPublicInfo(userId);
    }

    /**
     * 获取用户身份信息
     * GET /api/user/{userId}/identity
     */
    @GetMapping("/{userId}/identity")
    public Result getUserIdentity(@PathVariable Integer userId) {
        return identityService.getIdentityInfo(userId);
    }

    /**
     * 获取当前登录用户的身份信息
     * GET /api/user/identity
     */
    @GetMapping("/identity")
    public Result getCurrentUserIdentity(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        return identityService.getIdentityInfo(userId);
    }

    /**
     * 获取用户发布的话题
     * GET /api/user/{userId}/published-topics
     */
    @GetMapping("/{userId}/published-topics")
    public Result getUserPublishedTopics(@PathVariable Integer userId,
                                         @RequestParam(defaultValue = "1") Integer page,
                                         @RequestParam(defaultValue = "10") Integer size) {
        return userService.getUserPublishedTopics(userId, page, size);
    }

    /**
     * 获取用户参与的话题
     * GET /api/user/{userId}/participated-topics
     */
    @GetMapping("/{userId}/participated-topics")
    public Result getUserParticipatedTopics(@PathVariable Integer userId,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        return userService.getUserParticipatedTopics(userId, page, size);
    }

    /**
     * 举报用户
     * POST /api/user/{userId}/report
     */
    @PostMapping("/{userId}/report")
    public Result reportUser(@PathVariable Integer userId,
                             @RequestBody Map<String, String> requestBody,
                             HttpServletRequest request) {
        try {
            // 提取当前用户 ID
            Integer reporterId = extractUserIdFromToken(request);
            if (reporterId == null) {
                return Result.error(401, "用户未登录");
            }

            // 不能举报自己
            if (userId.equals(reporterId)) {
                return Result.error(400, "不能举报自己");
            }

            String reason = requestBody.get("reason");
            String description = requestBody.get("description");

            if (reason == null || reason.trim().isEmpty()) {
                return Result.error(400, "举报原因不能为空");
            }

            return userService.reportUser(userId, reporterId, reason, description);
        } catch (Exception e) {
            return Result.error(500, "举报失败：" + e.getMessage());
        }
    }

}
