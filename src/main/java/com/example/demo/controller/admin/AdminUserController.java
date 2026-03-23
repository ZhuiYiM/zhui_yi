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
