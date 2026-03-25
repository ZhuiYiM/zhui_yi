package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.dto.*;
import com.example.demo.entity.User;
import com.example.demo.entity.PrivacySetting;
import com.example.demo.entity.DefaultPrivacyLevel;
import com.example.demo.entity.UserVerification;
import com.example.demo.entity.enums.IdentityLevel1;
import com.example.demo.entity.Topics;
import com.example.demo.entity.TopicComments;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UserVerificationMapper;
import com.example.demo.mapper.UserSecuritySettingMapper;
import com.example.demo.mapper.PrivacySettingMapper;
import com.example.demo.mapper.DefaultPrivacyLevelMapper;
import com.example.demo.mapper.TopicsMapper;
import com.example.demo.mapper.TopicCommentsMapper;
import com.example.demo.service.FileUploadService;
import com.example.demo.service.MailService;
import com.example.demo.service.UserService;
import com.example.demo.service.UserIdentityService;
import com.example.demo.common.Result;
import com.example.demo.utils.JwtUtil;
import com.example.demo.utils.IdCardValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceimpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private MailService mailService;
    @Autowired
    private FileUploadService fileUploadService;
    @Autowired
    private UserVerificationMapper userVerificationMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PrivacySettingMapper privacySettingMapper;
    @Autowired
    private DefaultPrivacyLevelMapper defaultPrivacyLevelMapper;
    
    @Autowired
    private TopicsMapper topicsMapper;
    
    @Autowired
    private TopicCommentsMapper topicCommentsMapper;
    
    @Autowired
    private UserIdentityService userIdentityService;

    @Override
    public Result register(RegisterDTO registerDTO) {
        // 验证邮箱验证码
        MailService.VerificationResult verificationResult = mailService.validateCode(
                registerDTO.getEmail(),
                registerDTO.getVerificationCode()
        );

        if (verificationResult != MailService.VerificationResult.CODE_VALID) {
            return Result.error(verificationResult.getMessage());
        }

        // 检查邮箱是否已存在
        if (isEmailExists(registerDTO.getEmail())) {
            return Result.error("邮箱已被注册");
        }

        // 检查用户名是否已存在
        if (isUsernameExists(registerDTO.getUsername())) {
            return Result.error("用户名已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(encryptPassword(registerDTO.getPassword()));
        user.setStatus(1); // 已激活
        user.setCreateTime(LocalDateTime.now());

        boolean result = this.save(user);
        if (result) {
            return Result.success("注册成功");
        } else {
            return Result.error("注册失败");
        }
    }

    @Override
    public Result login(LoginDTO loginDTO) {
        User user = this.getOne(new QueryWrapper<User>()
                .eq("username", loginDTO.getUsername())
                .or()
                .eq("email", loginDTO.getUsername()));
    
        if (user == null) {
            return Result.error("用户不存在");
        }
    
        // 密码验证：数据库存储的是明文，所以直接比较
        if (!user.getPassword().equals(loginDTO.getPassword())) {
            return Result.error("密码错误");
        }
    
        // 登录成功，生成 Token 并返回
        LoginResponseDTO response = generateLoginResponse(user);
        return Result.success(response);
    }

    @Override
    public LoginResponseDTO generateLoginResponse(User user) {
        // 获取用户身份信息
        List<Map<String, Object>> identities = userIdentityService.getIdentityBadges(user.getId().longValue());
        
        // 生成包含用户 ID、角色和身份信息的 Token
        String token = jwtUtil.generateToken(
            user.getUsername(), 
            user.getId(), 
            user.getRole(),
            identities
        );

        // 构建 UserDTO
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhoneNumber(user.getPhoneNumber());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setRealName(user.getRealName());
        userDTO.setStudentId(user.getStudentId());
        userDTO.setGender(user.getGender());
        userDTO.setBirthDate(user.getBirthDate());
        userDTO.setCollege(user.getCollege());
        userDTO.setMajor(user.getMajor());
        userDTO.setBio(user.getBio());
        userDTO.setHobbies(user.getHobbies());
        userDTO.setStatus(user.getStatus());
        userDTO.setIsVerified(user.getIsVerified());
        userDTO.setIsRealNameVerified(user.getIsRealNameVerified());
        userDTO.setRole(user.getRole());
        userDTO.setIsAdmin(user.getIsAdmin());
        userDTO.setIsMerchant(user.getIsMerchant());
        userDTO.setIsOrganization(user.getIsOrganization());

        // 返回包含 Token 和用户信息的响应
        return new LoginResponseDTO(token, userDTO);
    }

    @Override
    public boolean isEmailExists(String email) {
        return this.count(new QueryWrapper<User>().eq("email", email)) > 0;
    }

    @Override
    public boolean isUsernameExists(String username) {
        return this.count(new QueryWrapper<User>().eq("username", username)) > 0;
    }

    @Override
    public Result getUserProfile(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        // 获取用户ID，而不是用户名
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null || !jwtUtil.validateTokenUsingId(token, userId)) {
            return Result.error("Token无效");
        }

        // 使用Mapper直接查询数据库，避免缓存问题
        User user = userMapper.selectById(userId);
        System.out.println("查询到的完整用户信息: " + user); // 添加此行调试
        if (user == null) {
            return Result.error("用户不存在");
        }

        return Result.success(user); // 直接返回从数据库查询到的用户对象
    }


    @Override
    public Result updateUserProfile(UserProfileDTO profileDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null || !jwtUtil.validateTokenUsingId(token, userId)) {
            return Result.error("Token无效");
        }

        // 使用UpdateWrapper来明确指定要更新的字段
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User>().eq("id", userId);

        boolean hasUpdates = false;

        // 更新头像
        if (profileDTO.getAvatarUrl() != null) {
            updateWrapper.set("avatar_url", profileDTO.getAvatarUrl());
            hasUpdates = true;
        }

        // 更新真实姓名
        if (profileDTO.getRealName() != null) {
            updateWrapper.set("real_name", profileDTO.getRealName());
            hasUpdates = true;
        }

        // 更新性别
        if (profileDTO.getGender() != null) {
            updateWrapper.set("gender", profileDTO.getGender());
            hasUpdates = true;
        }

        // 更新生日
        if (profileDTO.getBirthDate() != null) {
            updateWrapper.set("birth_date", profileDTO.getBirthDate());
            hasUpdates = true;
        }

        // 更新学院
        if (profileDTO.getCollege() != null) {
            updateWrapper.set("college", profileDTO.getCollege());
            hasUpdates = true;
        }

        // 更新专业班级
        if (profileDTO.getMajor() != null) {
            updateWrapper.set("major", profileDTO.getMajor());
            hasUpdates = true;
        }

        // 更新个人简介
        if (profileDTO.getBio() != null) {
            updateWrapper.set("bio", profileDTO.getBio());
            hasUpdates = true;
        }

        // 更新兴趣爱好
        if (profileDTO.getHobbies() != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String hobbiesJson = objectMapper.writeValueAsString(profileDTO.getHobbies()); // 修正：通过实例调用 getHobbies()
                updateWrapper.set("hobbies", hobbiesJson);
                hasUpdates = true;
            } catch (Exception e) {
                return Result.error("兴趣爱好数据格式错误");
            }
        }


        // 更新时间戳
        updateWrapper.set("update_time", LocalDateTime.now());

        if (!hasUpdates) {
            return Result.error("没有提供任何需要更新的信息");
        }

        // 执行更新
        boolean result = userMapper.update(null, updateWrapper) > 0;
        if (result) {
            return Result.success("用户信息更新成功");
        } else {
            return Result.error("用户信息更新失败");
        }
    }


    @Override
    public Result getUserDetail(Integer userId) {
        User user = userMapper.selectById(userId);  // 使用Mapper直接查询，避免缓存问题
        if (user == null) {
            return Result.error("用户不存在");
        }

        return Result.success(user);  // 直接返回从数据库查询到的用户对象
    }

    @Override
    public Result verifyStudent(String studentId, String realName, Integer userId) {
        User user = userMapper.selectById(userId);  // 使用Mapper直接查询，避免缓存问题
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 这里可以调用学校的学生信息系统验证学生身份
        // 模拟验证过程
        if (studentId != null && realName != null) {
            user.setStudentId(studentId);
            user.setRealName(realName);
            user.setIsVerified(1); // 设置为已验证
            user.setIsRealNameVerified(1); // 设置为已实名验证
            user.setUpdateTime(LocalDateTime.now());

            boolean result = userMapper.updateById(user) > 0;  // 使用Mapper直接更新
            if (result) {
                return Result.success("学生身份验证成功");
            } else {
                return Result.error("验证失败");
            }
        } else {
            return Result.error("学生信息不完整");
        }
    }
    @Override
    public Result getUsersByDepartment(String department, Integer page, Integer size) {
        Page<User> userPage = new Page<>(page, size);
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("department", department);
        Page<User> result = userMapper.selectPage(userPage, wrapper);
        return Result.success(result);
    }
    
    @Override
    public String encryptPassword(String password) {
        // 实际项目中建议使用BCrypt加密
        return password; // 这里先简单返回，实际应使用加密算法
    }
    
    @Override
    public Result uploadAvatar(MultipartFile file, Integer userId) {
        // 调用文件上传服务
        return fileUploadService.uploadImage(file, userId);
    }

    @Override
    public Result updatePhoneNumber(String phoneNumber, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null || !jwtUtil.validateTokenUsingId(token, userId)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setPhoneNumber(phoneNumber);
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userMapper.updateById(user) > 0;
        if (result) {
            return Result.success("电话更新成功");
        } else {
            return Result.error("电话更新失败");
        }
    }

    @Override
    public Result updateEmail(String email, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null || !jwtUtil.validateTokenUsingId(token, userId)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setEmail(email);
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userMapper.updateById(user) > 0;
        if (result) {
            return Result.success("邮箱更新成功");
        } else {
            return Result.error("邮箱更新失败");
        }
    }

    @Override
    public Result updateStudentId(String studentId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null || !jwtUtil.validateTokenUsingId(token, userId)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setStudentId(studentId);
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userMapper.updateById(user) > 0;
        if (result) {
            return Result.success("学号更新成功");
        } else {
            return Result.error("学号更新失败");
        }
    }

    @Override
    public Result changePassword(String oldPassword, String newPassword, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null || !jwtUtil.validateTokenUsingId(token, userId)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 验证旧密码
        if (!user.getPassword().equals(encryptPassword(oldPassword))) {
            return Result.error("原密码错误");
        }

        // 更新密码
        user.setPassword(encryptPassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userMapper.updateById(user) > 0;
        if (result) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("密码修改失败");
        }
    }

    @Override
    public Result getLoginDevices(Integer userId) {
        // 模拟登录设备数据
        List<Map<String, Object>> devices = new ArrayList<>();

        Map<String, Object> device1 = new HashMap<>();
        device1.put("id", 1);
        device1.put("deviceType", "desktop");
        device1.put("browserInfo", "Windows Chrome 浏览器");
        device1.put("ipAddress", "192.168.1.100");
        device1.put("lastLoginTime", "2023-05-20 14:30");
        device1.put("isCurrent", true);
        devices.add(device1);

        Map<String, Object> device2 = new HashMap<>();
        device2.put("id", 2);
        device2.put("deviceType", "mobile");
        device2.put("browserInfo", "iPhone Safari 浏览器");
        device2.put("ipAddress", "192.168.1.101");
        device2.put("lastLoginTime", "2023-05-19 09:15");
        device2.put("isCurrent", false);
        devices.add(device2);

        return Result.success(devices);
    }


    @Override
    public Result logoutDevice(Integer deviceId, Integer userId) {
        // 模拟退出设备登录
        // 实际项目中需要清除该设备的session/token
        return Result.success("设备退出成功");
    }

    @Override
    public Result logoutCurrentDevice(Integer userId) {
        // 模拟退出当前设备
        return Result.success("当前设备退出成功");
    }

    @Override
    public Result downloadUserData(Integer userId) {
        // 模拟数据下载
        // 实际项目中应该生成用户数据文件并提供下载链接
        return Result.success("数据下载请求已提交，请稍后查看邮箱");
    }

    @Override
    public Result deleteAccount(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        // 逻辑删除用户
        user.setStatus(0); // 设置为禁用状态
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userMapper.updateById(user) > 0;
        if (result) {
            return Result.success("账号注销成功");
        } else {
            return Result.error("账号注销失败");
        }
    }

    @Override
    public Result bindPhone(String phone, Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setPhoneNumber(phone);
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userMapper.updateById(user) > 0;
        if (result) {
            return Result.success("手机号绑定成功");
        } else {
            return Result.error("手机号绑定失败");
        }
    }

    @Override
    public Result bindEmail(String email, Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        user.setEmail(email);
        user.setUpdateTime(LocalDateTime.now());

        boolean result = userMapper.updateById(user) > 0;
        if (result) {
            return Result.success("邮箱绑定成功");
        } else {
            return Result.error("邮箱绑定失败");
        }
    }

    @Override
    public Result unbindSocial(String platform, Integer userId) {
        // 模拟社交账号解绑
        return Result.success(platform + "账号解绑成功");
    }

    @Override
    public Result updateSecuritySettings(Map<String, Object> settings, Integer userId) {
        // 模拟安全设置更新
        return Result.success("安全设置更新成功");
    }


    @Override
    public Result getSecuritySettings(Integer userId) {
        // 模拟获取安全设置
        Map<String, Object> settings = new HashMap<>();
        settings.put("twoFactorAuth", false);
        return Result.success(settings);
    }

    @Override
    public Result updatePrivacySettings(Integer userId, Map<String, Boolean> privacySettings, String defaultLevel) {
        try {
            // 删除用户现有隐私设置
            privacySettingMapper.delete(new QueryWrapper<PrivacySetting>().eq("user_id", userId));

            // 插入新的隐私设置
            List<PrivacySetting> settingsToInsert = new ArrayList<>();

            for (Map.Entry<String, Boolean> entry : privacySettings.entrySet()) {
                PrivacySetting setting = new PrivacySetting();
                setting.setUserId(userId);
                setting.setFieldName(entry.getKey());
                setting.setVisibilityLevel(entry.getValue() ? "public" : "private");
                setting.setCreatedAt(LocalDateTime.now());
                setting.setUpdatedAt(LocalDateTime.now());
                settingsToInsert.add(setting);
            }

            if (!settingsToInsert.isEmpty()) {
                for (PrivacySetting setting : settingsToInsert) {
                    privacySettingMapper.insert(setting);
                }
            }

            // 更新默认隐私级别
            if (defaultLevel != null) {
                DefaultPrivacyLevel defaultPrivacy = defaultPrivacyLevelMapper
                        .selectOne(new QueryWrapper<DefaultPrivacyLevel>().eq("user_id", userId));

                if (defaultPrivacy == null) {
                    defaultPrivacy = new DefaultPrivacyLevel();
                    defaultPrivacy.setUserId(userId);
                    defaultPrivacy.setLevel(defaultLevel);
                    defaultPrivacyLevelMapper.insert(defaultPrivacy);
                } else {
                    defaultPrivacy.setLevel(defaultLevel);
                    defaultPrivacyLevelMapper.updateById(defaultPrivacy);
                }
            }

            return Result.success("隐私设置更新成功");
        } catch (Exception e) {
            return Result.error("隐私设置更新失败: " + e.getMessage());
        }
    }

    @Override
    public Result getPrivacySettings(Integer userId) {
        try {
            // 获取具体字段隐私设置
            List<PrivacySetting> fieldSettings = privacySettingMapper
                    .selectList(new QueryWrapper<PrivacySetting>().eq("user_id", userId));

            // 获取默认隐私级别
            DefaultPrivacyLevel defaultLevel = defaultPrivacyLevelMapper
                    .selectOne(new QueryWrapper<DefaultPrivacyLevel>().eq("user_id", userId));

            Map<String, Object> result = new HashMap<>();
            Map<String, String> fieldVisibility = new HashMap<>();

            for (PrivacySetting setting : fieldSettings) {
                fieldVisibility.put(setting.getFieldName(), setting.getVisibilityLevel());
            }

            result.put("fieldSettings", fieldVisibility);
            result.put("defaultLevel", defaultLevel != null ? defaultLevel.getLevel() : "private");

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取隐私设置失败: " + e.getMessage());
        }
    }

    // 新增的用户功能实现
    @Override
    public Result loginByPhone(Map<String, String> request) {
        // 这个方法的实现应该在Controller中调用相应的服务
        return Result.success("手机号登录功能待实现");
    }
    
    @Override
    public Result getWechatQRCode() {
        // 微信扫码登录功能待实现
        return Result.success("微信扫码登录功能待实现");
    }
    
    @Override
    public Result getWechatScanStatus(String qrCodeId) {
        // 微信扫码状态查询功能待实现
        return Result.success("微信扫码状态查询功能待实现");
    }
    
    @Override
    public Result sendRecoveryCode(String identifier, String type) {
        // 找回密码验证码发送功能待实现
        return Result.success("找回密码验证码发送功能待实现");
    }
    
    @Override
    public Result resetPassword(Map<String, String> request) {
        // 密码重置功能待实现
        return Result.success("密码重置功能待实现");
    }

    @Override
    public Result getAuthStatus(Integer userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 查询用户的认证申请记录
            QueryWrapper<UserVerification> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .orderByDesc("submitted_at");
            
            List<UserVerification> verifications = userVerificationMapper.selectList(queryWrapper);
            
            Map<String, Object> status = new HashMap<>();
            status.put("userId", userId);
            status.put("isVerified", user.getIsVerified() != null && user.getIsVerified() == 1);
            status.put("isIdVerified", user.getIsRealNameVerified() != null && user.getIsRealNameVerified() == 1);
            status.put("isRealNameVerified", user.getIsRealNameVerified() != null && user.getIsRealNameVerified() == 1);
            
            // 统计各类认证状态
            Map<String, Integer> verificationStats = new HashMap<>();
            verificationStats.put("pending", 0);
            verificationStats.put("approved", 0);
            verificationStats.put("rejected", 0);
            
            for (UserVerification verification : verifications) {
                String statusKey = verification.getStatus();
                verificationStats.put(statusKey, verificationStats.get(statusKey) + 1);
            }
            
            status.put("verificationStats", verificationStats);
            status.put("recentVerifications", verifications.size() > 0 ? verifications.subList(0, Math.min(3, verifications.size())) : new ArrayList<>());
            
            return Result.success(status);
        } catch (Exception e) {
            return Result.error("获取认证状态失败: " + e.getMessage());
        }
    }

    @Override
    public Result getVerificationApplications(Integer userId, Integer page, Integer size) {
        try {
            Page<UserVerification> verificationPage = new Page<>(page, size);
            QueryWrapper<UserVerification> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .orderByDesc("submitted_at");
            
            Page<UserVerification> result = userVerificationMapper.selectPage(verificationPage, queryWrapper);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取申请历史失败: " + e.getMessage());
        }
    }

    @Override
    public Result cancelVerificationApplication(Integer applicationId, Integer userId) {
        try {
            UserVerification verification = userVerificationMapper.selectById(applicationId);
            if (verification == null) {
                return Result.error("认证申请不存在");
            }
            
            if (!verification.getUserId().equals(userId)) {
                return Result.error("无权限操作此申请");
            }
            
            if (!"pending".equals(verification.getStatus())) {
                return Result.error("只能取消待审核的申请");
            }
            
            // 删除申请记录
            boolean result = userVerificationMapper.deleteById(applicationId) > 0;
            if (result) {
                return Result.success("申请已取消");
            } else {
                return Result.error("取消申请失败");
            }
        } catch (Exception e) {
            return Result.error("取消申请失败: " + e.getMessage());
        }
    }

    @Override
    public Result applyIdentityVerification(Integer userId, String studentId, String realName, String college) {
        try {
            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 检查是否已经有进行中的认证申请
            QueryWrapper<UserVerification> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId)
                       .eq("verification_type", "student_id")
                       .in("status", "pending", "approved");
            
            UserVerification existingVerification = userVerificationMapper.selectOne(queryWrapper);
            if (existingVerification != null && "approved".equals(existingVerification.getStatus())) {
                return Result.error("您已经通过学生身份认证");
            }
            if (existingVerification != null && "pending".equals(existingVerification.getStatus())) {
                return Result.error("您已有正在进行的学生身份认证申请，请等待审核");
            }

            // 创建新的认证申请
            UserVerification verification = new UserVerification();
            verification.setUserId(userId);
            verification.setVerificationType("student_id");
            verification.setStatus("pending");
            verification.setStudentId(studentId);
            verification.setRealName(realName);
            verification.setCollege(college);
            verification.setSubmittedAt(LocalDateTime.now());

            boolean result = userVerificationMapper.insert(verification) > 0;
            if (result) {
                return Result.success("学生身份认证申请已提交，请等待审核");
            } else {
                return Result.error("认证申请提交失败");
            }
        } catch (Exception e) {
            return Result.error("认证申请处理失败: " + e.getMessage());
        }
    }

    @Override
    public Result applyRealNameVerification(Integer userId, String realName, String idCard) {
        try {
            // 验证用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 验证身份证号码格式
            if (!IdCardValidator.isValid(idCard)) {
                return Result.error("身份证号码格式不正确");
            }

            // 检查身份证是否已被其他用户认证
            QueryWrapper<UserVerification> idCardQuery = new QueryWrapper<>();
            idCardQuery.eq("id_card", idCard)
                      .eq("verification_type", "id_card")
                      .eq("status", "approved");
            
            UserVerification existingIdCard = userVerificationMapper.selectOne(idCardQuery);
            if (existingIdCard != null && !existingIdCard.getUserId().equals(userId)) {
                return Result.error("该身份证已被其他用户认证");
            }

            // 检查是否已经有进行中的认证申请
            QueryWrapper<UserVerification> userQuery = new QueryWrapper<>();
            userQuery.eq("user_id", userId)
                    .eq("verification_type", "id_card")
                    .in("status", "pending", "approved");
            
            UserVerification existingVerification = userVerificationMapper.selectOne(userQuery);
            if (existingVerification != null && "approved".equals(existingVerification.getStatus())) {
                return Result.error("您已经通过实名认证");
            }
            if (existingVerification != null && "pending".equals(existingVerification.getStatus())) {
                return Result.error("您已有正在进行的实名认证申请，请等待审核");
            }

            // 验证姓名与身份证匹配（这里简化处理，实际应调用公安系统接口）
            String extractedName = getRealNameFromIdCard(idCard);
            if (extractedName != null && !extractedName.equals(realName)) {
                return Result.error("姓名与身份证信息不匹配");
            }

            // 创建新的认证申请
            UserVerification verification = new UserVerification();
            verification.setUserId(userId);
            verification.setVerificationType("id_card");
            verification.setStatus("pending");
            verification.setRealName(realName);
            verification.setIdCard(idCard);
            verification.setSubmittedAt(LocalDateTime.now());

            boolean result = userVerificationMapper.insert(verification) > 0;
            if (result) {
                // 同时更新用户的实名信息
                user.setRealName(realName);
                user.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(user);
                
                return Result.success("实名认证申请已提交，请等待审核");
            } else {
                return Result.error("认证申请提交失败");
            }
        } catch (Exception e) {
            return Result.error("认证申请处理失败: " + e.getMessage());
        }
    }
    
    /**
     * 从身份证号码中提取姓名（模拟方法，实际应调用公安系统接口）
     * @param idCard 身份证号码
     * @return 姓名
     */
    private String getRealNameFromIdCard(String idCard) {
        // 这里是模拟实现，在实际项目中应该调用公安系统的身份验证接口
        // 返回 null 表示跳过姓名验证，或者可以实现简单的规则验证
        return null; // 暂时不进行姓名验证
    }

    @Override
    public Result getUserPublicInfo(Integer userId) {
        try {
            // 查询用户基本信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();

            // 基本信息
            Map<String, Object> basicInfo = new HashMap<>();
            basicInfo.put("id", user.getId());
            basicInfo.put("username", user.getUsername());
            basicInfo.put("realName", user.getRealName());
            basicInfo.put("avatarUrl", user.getAvatarUrl());
            basicInfo.put("bio", user.getBio());
            basicInfo.put("college", user.getCollege());
            result.put("basicInfo", basicInfo);

            // 学术信息
            Map<String, Object> academicInfo = new HashMap<>();
            academicInfo.put("studentId", user.getStudentId());
            academicInfo.put("major", user.getMajor());
            academicInfo.put("college", user.getCollege());
            result.put("academicInfo", academicInfo);

            // 身份信息
            Map<String, Object> identity = new HashMap<>();
            identity.put("verified", user.getStatus() != null && user.getStatus() > 0);
            
            // 使用身份识别逻辑确定一级标签
            String level1Tag = determineLevel1Tag(user);
            identity.put("level1Tag", level1Tag);
            result.put("identity", identity);

            // 隐私设置
            Map<String, Object> privacySettings = new HashMap<>();
            privacySettings.put("showCollege", true);
            privacySettings.put("showStudentId", true);
            privacySettings.put("showMajor", true);
            result.put("privacySettings", privacySettings);

            // 统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("postCount", 0); // TODO: 需要查询话题表
            statistics.put("likesReceived", 0); // TODO: 需要查询点赞表
            statistics.put("followerCount", 0); // TODO: 需要查询关注表
            statistics.put("followingCount", 0); // TODO: 需要查询关注表
            result.put("statistics", statistics);

            // 是否可以发消息
            result.put("canMessage", true);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取用户公开信息失败：" + e.getMessage());
        }
    }

    /**
     * 确定用户的一级身份标签
     * @param user 用户对象
     * @return 一级身份标签代码
     */
    private String determineLevel1Tag(User user) {
        if (user == null) {
            return IdentityLevel1.SOCIETY.getCode();
        }
        
        // 1. 检查管理员身份（最高优先级）
        if ("admin".equals(user.getRole()) || 
            (user.getIsAdmin() != null && user.getIsAdmin() == 1)) {
            return IdentityLevel1.ADMIN.getCode();
        }
        
        // 2. 检查学生身份（第二优先级）
        if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
            return IdentityLevel1.STUDENT.getCode();
        }
        
        // 3. 检查商户身份（第三优先级）
        if (user.getIsMerchant() != null && user.getIsMerchant() == 1) {
            return IdentityLevel1.MERCHANT.getCode();
        }
        
        // 4. 检查团体身份（第四优先级）
        if (user.getIsOrganization() != null && user.getIsOrganization() == 1) {
            return IdentityLevel1.ORGANIZATION.getCode();
        }
        
        // 5. 检查是否实名认证（第五优先级）
        if (user.getIsRealNameVerified() != null && user.getIsRealNameVerified() == 1) {
            return IdentityLevel1.SOCIETY.getCode();
        }
        
        // 6. 默认为社会用户（没有任何认证）
        return IdentityLevel1.SOCIETY.getCode();
    }

    @Override
    public Result getUserPublishedTopics(Integer userId, Integer page, Integer size) {
        try {
            // 查询用户发布的话题
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<Topics> topicsPage = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
            QueryWrapper<Topics> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("status", 1)  // 只查询正常状态的话题
                   .orderByDesc("created_at");
            
            Page<Topics> result = topicsMapper.selectPage(topicsPage, wrapper);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("topics", result.getRecords());
            responseData.put("total", result.getTotal());
            responseData.put("page", result.getCurrent());
            responseData.put("size", result.getSize());
            responseData.put("totalPages", result.getPages());
            
            return Result.success(responseData);
        } catch (Exception e) {
            return Result.error("获取用户发布的话题失败：" + e.getMessage());
        }
    }

    @Override
    public Result getUserParticipatedTopics(Integer userId, Integer page, Integer size) {
        try {
            // 查询用户参与的话题 (通过评论)
            QueryWrapper<TopicComments> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("user_id", userId)
                         .eq("status", 1);  // 只查询有效的评论
            
            List<TopicComments> comments = topicCommentsMapper.selectList(commentWrapper);
            
            // 提取话题 ID 列表
            List<Long> topicIds = new ArrayList<>();
            for (TopicComments comment : comments) {
                topicIds.add(comment.getTopicId());
            }
            
            // 去重
            topicIds = topicIds.stream().distinct().collect(java.util.stream.Collectors.toList());
            
            // 分页处理
            int total = topicIds.size();
            int fromIndex = (page - 1) * size;
            int toIndex = Math.min(fromIndex + size, total);
            
            if (fromIndex >= total) {
                // 超出范围，返回空列表
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("topics", new ArrayList<>());
                responseData.put("total", 0);
                responseData.put("page", page);
                responseData.put("size", size);
                responseData.put("totalPages", 0);
                return Result.success(responseData);
            }
            
            List<Long> pagedTopicIds = topicIds.subList(fromIndex, toIndex);
            
            // 查询话题详情
            if (pagedTopicIds.isEmpty()) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("topics", new ArrayList<>());
                responseData.put("total", 0);
                responseData.put("page", page);
                responseData.put("size", size);
                responseData.put("totalPages", 0);
                return Result.success(responseData);
            }
            
            QueryWrapper<Topics> topicWrapper = new QueryWrapper<>();
            topicWrapper.in("id", pagedTopicIds)
                       .eq("status", 1);
            
            List<Topics> topics = topicsMapper.selectList(topicWrapper);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("topics", topics);
            responseData.put("total", total);
            responseData.put("page", page);
            responseData.put("size", size);
            responseData.put("totalPages", (total + size - 1) / size);
            
            return Result.success(responseData);
        } catch (Exception e) {
            return Result.error("获取用户参与的话题失败：" + e.getMessage());
        }
    }

    @Override
    public Result reportUser(Integer reportedUserId, Integer reporterId, String reason, String description) {
        try {
            // TODO: 需要创建 UserReport 实体类和 Mapper
            // 暂时返回成功，实际应该保存到数据库
            
            Map<String, Object> result = new HashMap<>();
            result.put("reportId", System.currentTimeMillis());
            result.put("status", "pending");
            result.put("message", "举报已提交，我们会尽快处理");
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("举报失败：" + e.getMessage());
        }
    }

}