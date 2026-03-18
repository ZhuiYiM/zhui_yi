package com.example.demo.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.common.Result;

public interface AdminUserService extends IService<AdminUser> {
    Result login(String username, String password);
    Result logout();
    Result getUserInfo(Integer adminId);
    Result updateProfile(Integer adminId, AdminUser adminUser);
    Result changePassword(Integer adminId, String oldPassword, String newPassword);
}
