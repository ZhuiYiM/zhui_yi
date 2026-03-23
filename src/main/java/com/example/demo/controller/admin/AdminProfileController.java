package com.example.demo.controller.admin;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.service.admin.AdminUserService;
import com.example.demo.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminProfileController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取当前管理员信息
     */
    @GetMapping("/info")
    public ApiResult getAdminInfo(HttpServletRequest request) {
        try {
            // 从请求头获取 token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ApiResult.error(401, "未登录");
            }
            
            token = token.substring(7);
            
            // 解析 token 获取管理员 ID
            Integer adminId = jwtUtil.getUserIdFromToken(token);
            if (adminId == null) {
                return ApiResult.error(401, "登录已过期");
            }
            
            // 获取管理员信息
            AdminUser admin = adminUserService.getById(adminId);
            if (admin == null) {
                return ApiResult.error(404, "管理员不存在");
            }
            
            // 返回管理员信息（不包含密码）
            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", admin.getId());
            adminInfo.put("username", admin.getUsername());
            adminInfo.put("realName", admin.getRealName());
            adminInfo.put("nickname", admin.getRealName()); // 使用 realName 作为 nickname
            adminInfo.put("avatar", admin.getAvatar());
            adminInfo.put("roleId", admin.getRoleId());
            adminInfo.put("createdAt", admin.getCreatedAt());
            adminInfo.put("lastLoginTime", admin.getLastLoginTime());
            
            return ApiResult.success(adminInfo);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error(500, "获取管理员信息失败：" + e.getMessage());
        }
    }

    /**
     * 更新管理员个人信息
     */
    @PutMapping("/profile")
    public ApiResult updateProfile(HttpServletRequest request, @RequestBody Map<String, String> params) {
        try {
            // 从请求头获取 token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ApiResult.error(401, "未登录");
            }
            
            token = token.substring(7);
            
            // 解析 token 获取管理员 ID
            Integer adminId = jwtUtil.getUserIdFromToken(token);
            if (adminId == null) {
                return ApiResult.error(401, "登录已过期");
            }
            
            // 获取管理员信息
            AdminUser admin = adminUserService.getById(adminId);
            if (admin == null) {
                return ApiResult.error(404, "管理员不存在");
            }
            
            // 更新信息
            String realName = params.get("realName");
            String nickname = params.get("nickname");
            String avatar = params.get("avatar");
            
            if (realName != null) {
                admin.setRealName(realName);
            }
            if (nickname != null) {
                admin.setRealName(nickname); // AdminUser 没有 nickname 字段，使用 realName
            }
            if (avatar != null) {
                admin.setAvatar(avatar);
            }
            
            boolean success = adminUserService.updateById(admin);
            if (success) {
                return ApiResult.success("更新成功", null);
            } else {
                return ApiResult.error(500, "更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error(500, "更新失败：" + e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public ApiResult changePassword(HttpServletRequest request, @RequestBody Map<String, String> params) {
        try {
            // 从请求头获取 token
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ApiResult.error(401, "未登录");
            }
            
            token = token.substring(7);
            
            // 解析 token 获取管理员 ID
            Integer adminId = jwtUtil.getUserIdFromToken(token);
            if (adminId == null) {
                return ApiResult.error(401, "登录已过期");
            }
            
            // 获取管理员信息
            AdminUser admin = adminUserService.getById(adminId);
            if (admin == null) {
                return ApiResult.error(404, "管理员不存在");
            }
            
            // 验证旧密码
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            
            if (oldPassword == null || newPassword == null) {
                return ApiResult.error(400, "密码不能为空");
            }
            
            // 使用 service 层的方法修改密码（会自动 BCrypt 加密）
            adminUserService.changePassword(adminId, oldPassword, newPassword);
            
            return ApiResult.success("密码修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResult.error(500, "修改失败：" + e.getMessage());
        }
    }
}
