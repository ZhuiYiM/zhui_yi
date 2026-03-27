// service/UserService.java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.dto.LoginDTO;
import com.example.demo.entity.dto.LoginResponseDTO;
import com.example.demo.entity.dto.RegisterDTO;
import com.example.demo.entity.dto.UserProfileDTO;
import com.example.demo.entity.User;
import com.example.demo.common.Result;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService extends IService<User> {
    Result register(RegisterDTO registerDTO);
    Result login(LoginDTO loginDTO);
    boolean isEmailExists(String email);
    boolean isUsernameExists(String username);
    LoginResponseDTO generateLoginResponse(User user);

    // 获取用户资料
    Result getUserProfile(HttpServletRequest request);
    // 更新用户资料
    Result updateUserProfile(UserProfileDTO profileDTO, HttpServletRequest request);
    // 根据部门查询用户列表
    Result getUsersByDepartment(String department, Integer page, Integer size);
    // 根据用户ID获取用户详情
    Result getUserDetail(Integer userId);
    // 学生身份验证
    Result verifyStudent(String studentId, String realName, Integer userId);
    // 获取隐私设置
    Result getPrivacySettings(Integer userId);
    // 更新隐私设置
    Result updatePrivacySettings(Integer userId, Map<String, Boolean> privacySettings, String defaultLevel);
    // 上传头像
    Result uploadAvatar(MultipartFile file, Integer userId);
    // 更新用户电话
    Result updatePhoneNumber(String phoneNumber, HttpServletRequest request);
    // 更新用户邮箱
    Result updateEmail(String email, HttpServletRequest request);
    // 更新用户学号
    Result updateStudentId(String studentId, HttpServletRequest request);
    // 账号管理相关方法
    Result changePassword(String oldPassword, String newPassword, HttpServletRequest request);
    Result getLoginDevices(Integer userId);
    Result logoutDevice(Integer deviceId, Integer userId);
    Result logoutCurrentDevice(Integer userId);
    Result downloadUserData(Integer userId);
    Result deleteAccount(Integer userId);
    Result bindPhone(String phone, Integer userId);
    Result bindEmail(String email, Integer userId);
    Result unbindSocial(String platform, Integer userId);
    Result updateSecuritySettings(Map<String, Object> settings, Integer userId);
    Result getSecuritySettings(Integer userId);
    
    // 新增的用户功能方法
    Result loginByPhone(Map<String, String> request);
    Result getWechatQRCode();
    Result getWechatScanStatus(String qrCodeId);
    Result sendRecoveryCode(String identifier, String type);
    Result resetPassword(Map<String, String> request);
    // ... existing code ...

    // 认证相关方法
    Result getAuthStatus(Integer userId);
    Result applyIdentityVerification(Integer userId, String studentId, String realName, String college);
    Result applyRealNameVerification(Integer userId, String realName, String idCard);
    Result getVerificationApplications(Integer userId, Integer page, Integer size);
    Result cancelVerificationApplication(Integer applicationId, Integer userId);

    // 用户公开信息相关方法
    Result getUserPublicInfo(Integer userId);

    // 用户话题相关方法
    Result getUserPublishedTopics(Integer userId, Integer page, Integer size);
    Result getUserParticipatedTopics(Integer userId, Integer page, Integer size);

    // 举报用户相关方法
    Result reportUser(Integer reportedUserId, Integer reporterId, String reason, String description);

    // 工具方法
    String encryptPassword(String password);

// ... existing code ...

}
