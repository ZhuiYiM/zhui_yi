package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 身份认证控制器
 * 专门处理用户身份认证相关接口
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 申请学生身份认证
     */
    @PostMapping("/student-verification")
    public Result applyStudentVerification(@RequestBody Map<String, Object> request,
                                         HttpServletRequest httpRequest) {
        try {
            // 从Token中获取用户ID
            Integer userId = extractUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }

            String studentId = (String) request.get("studentId");
            String realName = (String) request.get("realName");
            String college = (String) request.get("college");

            if (studentId == null || studentId.trim().isEmpty()) {
                return Result.error("学号不能为空");
            }
            if (realName == null || realName.trim().isEmpty()) {
                return Result.error("真实姓名不能为空");
            }
            if (college == null || college.trim().isEmpty()) {
                return Result.error("学院信息不能为空");
            }

            return userService.applyIdentityVerification(userId, studentId, realName, college);
        } catch (Exception e) {
            return Result.error("申请处理失败: " + e.getMessage());
        }
    }

    /**
     * 申请身份证实名认证
     */
    @PostMapping("/idcard-verification")
    public Result applyIdCardVerification(@RequestBody Map<String, Object> request,
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
            return Result.error("申请处理失败: " + e.getMessage());
        }
    }

    /**
     * 获取认证状态
     */
    @GetMapping("/status")
    public Result getAuthStatus(HttpServletRequest httpRequest) {
        try {
            Integer userId = extractUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }

            return userService.getAuthStatus(userId);
        } catch (Exception e) {
            return Result.error("获取认证状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取认证申请历史
     */
    @GetMapping("/applications")
    public Result getVerificationApplications(HttpServletRequest httpRequest,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer size) {
        try {
            Integer userId = extractUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }

            return userService.getVerificationApplications(userId, page, size);
        } catch (Exception e) {
            return Result.error("获取申请历史失败: " + e.getMessage());
        }
    }

    /**
     * 取消认证申请
     */
    @DeleteMapping("/application/{applicationId}")
    public Result cancelVerificationApplication(@PathVariable Integer applicationId,
                                              HttpServletRequest httpRequest) {
        try {
            Integer userId = extractUserIdFromToken(httpRequest);
            if (userId == null) {
                return Result.error("用户未登录");
            }

            return userService.cancelVerificationApplication(applicationId, userId);
        } catch (Exception e) {
            return Result.error("取消申请失败: " + e.getMessage());
        }
    }

    /**
     * 验证身份证号码格式
     */
    @PostMapping("/validate-idcard")
    public Result validateIdCard(@RequestBody Map<String, String> request) {
        try {
            String idCard = request.get("idCard");
            if (idCard == null || idCard.trim().isEmpty()) {
                return Result.error("身份证号码不能为空");
            }

            // 调用身份证验证工具
            boolean isValid = com.example.demo.utils.IdCardValidator.isValid(idCard);
            if (isValid) {
                Map<String, Object> info = com.example.demo.utils.IdCardValidator.getDetailedInfo(idCard);
                return Result.success(info);
            } else {
                return Result.error("身份证号码格式不正确");
            }
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage());
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
}