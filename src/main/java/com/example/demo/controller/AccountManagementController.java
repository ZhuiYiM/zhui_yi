// controller/AccountManagementController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.UserService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/account")
@CrossOrigin
public class AccountManagementController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 修改密码
    @PostMapping("/password/change")
    public Result changePassword(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return Result.error("密码不能为空");
        }

        return userService.changePassword(oldPassword, newPassword, httpRequest);
    }

    // 获取登录设备列表
    @GetMapping("/devices")
    public Result getLoginDevices(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.getLoginDevices(userId);
    }

    // 退出指定设备
    @DeleteMapping("/devices/{deviceId}")
    public Result logoutDevice(@PathVariable Integer deviceId, HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.logoutDevice(deviceId, userId);
    }

    // 退出当前设备
    @DeleteMapping("/devices/current")
    public Result logoutCurrentDevice(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.logoutCurrentDevice(userId);
    }

    // 下载用户数据
    @GetMapping("/data/download")
    public Result downloadUserData(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.downloadUserData(userId);
    }

    // 注销账号
    @DeleteMapping("/delete")
    public Result deleteAccount(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.deleteAccount(userId);
    }

    // 绑定手机号
    @PostMapping("/phone/bind")
    public Result bindPhone(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String phone = request.get("phone");
        if (phone == null) {
            return Result.error("手机号不能为空");
        }

        Integer userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        return userService.bindPhone(phone, userId);
    }

    // 绑定邮箱
    @PostMapping("/email/bind")
    public Result bindEmail(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        String email = request.get("email");
        if (email == null) {
            return Result.error("邮箱不能为空");
        }

        Integer userId = extractUserIdFromToken(httpRequest);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        return userService.bindEmail(email, userId);
    }

    // 解绑社交账号
    @DeleteMapping("/social/{platform}")
    public Result unbindSocial(@PathVariable String platform, HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.unbindSocial(platform, userId);
    }

    // 更新安全设置
    @PutMapping("/security/settings")
    public Result updateSecuritySettings(@RequestBody Map<String, Object> settings, HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.updateSecuritySettings(settings, userId);
    }

    // 获取安全设置
    @GetMapping("/security/settings")
    public Result getSecuritySettings(HttpServletRequest request) {
        Integer userId = extractUserIdFromToken(request);
        if (userId == null) {
            return Result.error("用户未登录");
        }
        return userService.getSecuritySettings(userId);
    }

    // 从Token中提取用户ID的辅助方法
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
