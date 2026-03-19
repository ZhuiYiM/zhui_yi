package com.example.demo.service.admin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.common.Result;

import java.util.Map;

public interface AdminUserService extends IService<AdminUser> {
    Result login(String username, String password);
    Result logout();
    Result getUserInfo(Integer adminId);
    Result updateProfile(Integer adminId, AdminUser adminUser);
    Result changePassword(Integer adminId, String oldPassword, String newPassword);
    Result checkUserAdmin(String username, String password);
    Result resetAdminPassword(String username, String rawPassword);
    
    // 用户管理相关接口
    Result getUserList(Map<String, Object> params);
    Result getUserDetail(Integer userId);
    Result updateUserStatus(Integer userId, Integer status);
    Result deleteUser(Integer userId);
    Result verifyUserIdCard(Integer userId, boolean pass, String reason);
    Result verifyUserRealName(Integer userId, boolean pass, String reason);
    Result getVerificationApplications(Map<String, Object> params);
    Result getApplicationDetail(Integer applicationId);
    Result approveApplication(Integer applicationId, String adminRemark);
    Result rejectApplication(Integer applicationId, String reason);
}
