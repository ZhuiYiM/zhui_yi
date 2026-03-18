package com.example.demo.controller.admin;

import com.example.demo.common.Result;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.entity.dto.admin.AdminLoginDTO;
import com.example.demo.service.admin.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

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
        // 从 Token 中提取 adminId（这里简化处理，实际应该解析 JWT）
        Integer adminId = 1; // TODO: 从 JWT 中解析
        return adminUserService.getUserInfo(adminId);
    }

    /**
     * 更新管理员信息
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody AdminUser adminUser,
                                 @RequestHeader("Authorization") String token) {
        Integer adminId = 1; // TODO: 从 JWT 中解析
        return adminUserService.updateProfile(adminId, adminUser);
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result changePassword(@RequestBody Map<String, String> params,
                                  @RequestHeader("Authorization") String token) {
        Integer adminId = 1; // TODO: 从 JWT 中解析
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return adminUserService.changePassword(adminId, oldPassword, newPassword);
    }
}
