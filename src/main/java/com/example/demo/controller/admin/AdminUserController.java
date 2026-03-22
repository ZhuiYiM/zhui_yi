package com.example.demo.controller.admin;

import com.example.demo.common.Result;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.entity.dto.admin.AdminLoginDTO;
import com.example.demo.service.admin.AdminUserService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody AdminLoginDTO loginDTO) {
        return adminUserService.login(loginDTO.getUsername(), loginDTO.getPassword());
    }

    /**
     * 管理员退出
     */
    @PostMapping("/logout")
    public Result logout() {
        return adminUserService.logout();
    }

    /**
     * 获取管理员信息
     */
    @GetMapping("/info")
    public Result getUserInfo(@RequestHeader("Authorization") String token) {
        try {
            // 从 Token 中提取 adminId
            Integer adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            if (adminId == null) {
                return Result.error("无效的Token");
            }
            return adminUserService.getUserInfo(adminId);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody AdminUser adminUser,
                                 @RequestHeader("Authorization") String token) {
        try {
            Integer adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            if (adminId == null) {
                return Result.error("无效的Token");
            }
            return adminUserService.updateProfile(adminId, adminUser);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result changePassword(@RequestBody Map<String, String> params,
                                  @RequestHeader("Authorization") String token) {
        try {
            Integer adminId = jwtUtil.getUserIdFromToken(token.replace("Bearer ", ""));
            if (adminId == null) {
                return Result.error("无效的Token");
            }
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            return adminUserService.changePassword(adminId, oldPassword, newPassword);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("修改失败：" + e.getMessage());
        }
    }
    
    /**
     * 检查普通用户是否是管理员（用于自动识别）
     */
    @PostMapping("/check-user-admin")
    public Result checkUserAdmin(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        return adminUserService.checkUserAdmin(username, password);
    }
    
    /**
     * 临时接口：重置 admin 密码为 admin123
     */
    @PostMapping("/reset-password-temp")
    public Result resetPasswordTemp() {
        return adminUserService.resetAdminPassword("admin", "admin123");
    }
}
