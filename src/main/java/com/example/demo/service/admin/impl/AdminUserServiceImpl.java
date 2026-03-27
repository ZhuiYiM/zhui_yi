package com.example.demo.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.User;
import com.example.demo.entity.UserIdentity;
import com.example.demo.entity.UserVerification;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.entity.admin.OperationLog;
import com.example.demo.mapper.admin.AdminUserMapper;
import com.example.demo.mapper.admin.OperationLogMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UserIdentityMapper;
import com.example.demo.mapper.UserVerificationMapper;
import com.example.demo.service.admin.AdminUserService;
import com.example.demo.service.admin.OperationLogService;
import com.example.demo.common.Result;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private UserVerificationMapper userVerificationMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserIdentityMapper userIdentityMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    // 使用静态实例，避免 Spring 代理问题
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result login(String username, String password) {
        // 查询管理员
        AdminUser adminUser = this.lambdaQuery()
                .eq(AdminUser::getUsername, username)
                .one();
        
        if (adminUser == null) {
            return Result.error("用户名或密码错误");
        }
        
        // 检查状态
        if (adminUser.getStatus() != 1) {
            return Result.error("账号已被禁用");
        }
        
        // 验证密码
        boolean matches = passwordEncoder.matches(password, adminUser.getPassword());
        
        if (!matches) {
            return Result.error("用户名或密码错误");
        }
        
        // 生成 Token（使用 adminId 作为 userId 存储）
        String token = jwtUtil.generateToken(adminUser.getUsername(), adminUser.getId());
        
        // 更新登录信息
        adminUser.setLastLoginTime(LocalDateTime.now());
        adminUserMapper.updateById(adminUser);
        
        // 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("adminId", adminUser.getId());
        result.put("username", adminUser.getUsername());
        result.put("realName", adminUser.getRealName());
        result.put("avatar", adminUser.getAvatar());
        result.put("roleId", adminUser.getRoleId());
        
        return Result.success("登录成功", result);
    }

    @Override
    public Result logout() {
        // 可以在这里添加 Token 黑名单逻辑
        return Result.success("退出成功");
    }

    @Override
    public Result getUserInfo(Integer adminId) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser == null) {
            return Result.error("管理员不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("id", adminUser.getId());
        result.put("username", adminUser.getUsername());
        result.put("realName", adminUser.getRealName());
        result.put("avatar", adminUser.getAvatar());
        result.put("roleId", adminUser.getRoleId());
        
        return Result.success(result);
    }

    @Override
    public Result updateProfile(Integer adminId, AdminUser adminUser) {
        AdminUser existingUser = adminUserMapper.selectById(adminId);
        if (existingUser == null) {
            return Result.error("管理员不存在");
        }
        
        existingUser.setRealName(adminUser.getRealName());
        if (adminUser.getAvatar() != null) {
            existingUser.setAvatar(adminUser.getAvatar());
        }
        
        adminUserMapper.updateById(existingUser);
        return Result.success("更新成功");
    }

    @Override
    public Result changePassword(Integer adminId, String oldPassword, String newPassword) {
        AdminUser adminUser = adminUserMapper.selectById(adminId);
        if (adminUser == null) {
            return Result.error("管理员不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, adminUser.getPassword())) {
            return Result.error("原密码错误");
        }
        
        // 更新密码
        adminUser.setPassword(passwordEncoder.encode(newPassword));
        adminUserMapper.updateById(adminUser);
        
        return Result.success("密码修改成功");
    }
    
    @Override
    public Result checkUserAdmin(String username, String password) {
        // 查询管理员
        AdminUser adminUser = this.lambdaQuery()
                .eq(AdminUser::getUsername, username)
                .one();
        
        if (adminUser == null) {
            // 不是管理员，返回成功但不包含管理员信息
            Map<String, Object> result = new HashMap<>();
            result.put("isAdmin", false);
            return Result.success(result);
        }
        
        // 检查状态
        if (adminUser.getStatus() != 1) {
            return Result.error("账号已被禁用");
        }
        
        // 验证密码
        boolean matches = passwordEncoder.matches(password, adminUser.getPassword());
        
        if (!matches) {
            return Result.error("用户名或密码错误");
        }
        
        // 是管理员，返回管理员信息
        Map<String, Object> result = new HashMap<>();
        result.put("isAdmin", true);
        result.put("adminId", adminUser.getId());
        result.put("username", adminUser.getUsername());
        result.put("realName", adminUser.getRealName());
        result.put("roleId", adminUser.getRoleId());
        
        return Result.success(result);
    }
    
    @Override
    public Result resetAdminPassword(String username, String rawPassword) {
        // 查询管理员
        AdminUser adminUser = this.lambdaQuery()
                .eq(AdminUser::getUsername, username)
                .one();
        
        if (adminUser == null) {
            return Result.error("管理员不存在");
        }
        
        // 使用 BCrypt 加密新密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        adminUser.setPassword(encodedPassword);
        adminUserMapper.updateById(adminUser);
        
        return Result.success("密码重置成功，新密码：" + rawPassword);
    }
    
    @Override
    public Result getUserList(Map<String, Object> params) {
        // TODO: 实现用户列表获取
        return Result.success("用户列表获取功能待实现");
    }
    
    @Override
    public Result getUserDetail(Integer userId) {
        // TODO: 实现用户详情获取
        return Result.success("用户详情获取功能待实现");
    }
    
    @Override
    public Result updateUserStatus(Integer userId, Integer status) {
        // TODO: 实现用户状态更新
        return Result.success("用户状态更新功能待实现");
    }
    
    @Override
    public Result deleteUser(Integer userId) {
        // TODO: 实现用户删除
        return Result.success("用户删除功能待实现");
    }
    
    @Override
    public Result verifyUserIdCard(Integer userId, boolean pass, String reason) {
        // TODO: 实现身份证认证审核
        return Result.success("身份证认证审核功能待实现");
    }
    
    @Override
    public Result verifyUserRealName(Integer userId, boolean pass, String reason) {
        try {
            // 查询待审核的认证申请
            UserVerification verification = userVerificationMapper.selectById(userId);
            if (verification == null) {
                return Result.error("认证申请不存在");
            }
            
            if (!"pending".equals(verification.getStatus())) {
                return Result.error("该申请已审核过");
            }
            
            // 获取当前管理员 ID（从登录上下文中获取）
            Long adminId = jwtUtil.getCurrentAdminId();
            if (adminId == null) {
                adminId = 1L; // 降级方案：如果无法获取，使用默认值
            }
            
            if (pass) {
                // 通过认证
                verification.setStatus("approved");
                verification.setReviewerId(adminId.intValue()); // 转换为 Integer
                verification.setReviewedAt(LocalDateTime.now());
                
                // 根据认证类型更新用户表
                if ("id_card".equals(verification.getVerificationType())) {
                    // 更新用户实名信息
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("is_real_name_verified", 1);
                    userUpdate.put("real_name", verification.getRealName());
                    userUpdate.put("id_card", verification.getIdCard());
                    // TODO: 调用 UserMapper 更新用户信息
                } else if ("student_id".equals(verification.getVerificationType())) {
                    // 更新学生身份信息
                    Map<String, Object> userUpdate = new HashMap<>();
                    userUpdate.put("is_verified", 1);
                    userUpdate.put("student_id", verification.getStudentId());
                    userUpdate.put("college", verification.getCollege());
                    // TODO: 调用 UserMapper 更新用户信息
                }
                
                userVerificationMapper.updateById(verification);
                return Result.success("认证已通过");
            } else {
                // 拒绝认证
                verification.setStatus("rejected");
                verification.setRejectionReason(reason);
                verification.setReviewerId(adminId.intValue()); // 转换为 Integer
                verification.setReviewedAt(LocalDateTime.now());
                
                userVerificationMapper.updateById(verification);
                return Result.success("认证已拒绝");
            }
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result getVerificationApplications(Map<String, Object> params) {
        // TODO: 实现认证申请列表获取
        return Result.success("认证申请列表获取功能待实现");
    }
    
    @Override
    public Result getApplicationDetail(Integer applicationId) {
        // TODO: 实现认证申请详情获取
        return Result.success("认证申请详情获取功能待实现");
    }
    
    @Override
    public Result approveApplication(Integer applicationId, String adminRemark) {
        System.out.println(" 开始审核认证申请，ID: " + applicationId);
        try {
            // 查询待审核的认证申请
            UserVerification verification = userVerificationMapper.selectById(applicationId);
            System.out.println("📋 查询到的认证申请：" + (verification != null ? "存在" : "不存在"));
            
            if (verification == null) {
                System.out.println("❌ 认证申请不存在");
                return Result.error("认证申请不存在");
            }
            
            System.out.println("📝 当前状态：" + verification.getStatus());
            if (!"pending".equals(verification.getStatus())) {
                System.out.println("❌ 该申请已审核过");
                return Result.error("该申请已审核过");
            }
            
            // 获取当前管理员 ID
            Long adminId = jwtUtil.getCurrentAdminId();
            if (adminId == null) {
                adminId = 1L; // 降级方案：如果无法获取，使用默认值
            }
            System.out.println("👤 管理员 ID: " + adminId);
            
            // 通过认证
            verification.setStatus("approved");
            verification.setReviewerId(adminId.intValue()); // 转换为 Integer
            verification.setReviewedAt(LocalDateTime.now());
            
            System.out.println("💾 更新认证申请状态...");
            int updateCount = userVerificationMapper.updateById(verification);
            System.out.println("✅ 认证申请更新成功，影响行数：" + updateCount);
            
            // 更新用户表中的身份信息
            System.out.println("🔄 开始更新用户身份信息...");
            updateUserIdentity(verification);
            System.out.println("✅ 用户身份信息更新完成");
            
            // 更新 user_identity 表，设置 verified = 1
            System.out.println("🔄 开始更新 user_identity 表...");
            LambdaQueryWrapper<UserIdentity> identityWrapper = new LambdaQueryWrapper<>();
            identityWrapper.eq(UserIdentity::getUserId, verification.getUserId())
                          .eq(UserIdentity::getIdentityType, verification.getVerificationType());
            
            UserIdentity userIdentity = userIdentityMapper.selectOne(identityWrapper);
            if (userIdentity != null) {
                userIdentity.setVerified(1);
                userIdentity.setVerifiedAt(LocalDateTime.now());
                userIdentityMapper.updateById(userIdentity);
                System.out.println("✅ user_identity 表更新成功");
            } else {
                // 如果 user_identity 表中没有对应的记录，创建新的记录
                System.out.println("⚠️ 未找到对应的 user_identity 记录，创建新记录...");
                userIdentity = new UserIdentity();
                userIdentity.setUserId(verification.getUserId().longValue());
                userIdentity.setIdentityType(verification.getVerificationType());
                userIdentity.setIdentityName(getIdentityTypeName(verification.getVerificationType()));
                userIdentity.setVerified(1);
                userIdentity.setVerifiedAt(LocalDateTime.now());
                userIdentity.setExtraInfo(verification.getExtraInfo());
                userIdentityMapper.insert(userIdentity);
                System.out.println("✅ 创建新的 user_identity 记录成功");
            }
            
            // 记录操作日志
            try {
                OperationLog log = new OperationLog();
                log.setAdminId(adminId);
                log.setAdminName(jwtUtil.getCurrentAdminUsername());
                log.setOperation("APPROVE_IDENTITY");
                log.setModule("身份认证");
                log.setTargetId(verification.getId().longValue());
                log.setDetail(String.format("通过用户 ID=%d 的 %s 认证申请", 
                    verification.getUserId(), 
                    getIdentityTypeName(verification.getVerificationType())));
                log.setCreatedAt(LocalDateTime.now());
                operationLogMapper.insert(log);
                System.out.println("📝 操作日志记录成功");
            } catch (Exception e) {
                System.err.println("⚠️ 记录操作日志失败：" + e.getMessage());
            }
            
            // 删除该用户的其他身份记录，确保身份唯一性
            try {
                LambdaQueryWrapper<UserIdentity> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(UserIdentity::getUserId, verification.getUserId())
                            .ne(UserIdentity::getIdentityType, verification.getVerificationType());
                int deletedCount = userIdentityMapper.delete(deleteWrapper);
                System.out.println("✅ 已删除 " + deletedCount + " 个其他身份记录");
            } catch (Exception e) {
                System.err.println("⚠️ 删除其他身份记录失败：" + e.getMessage());
            }
            
            return Result.success("认证申请已通过");
        } catch (Exception e) {
            System.err.println("❌ 审核失败：" + e.getMessage());
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据认证类型更新用户身份信息
     */
    private void updateUserIdentity(UserVerification verification) {
        System.out.println("🔧 updateUserIdentity 开始执行");
        System.out.println("📌 用户 ID: " + verification.getUserId());
        System.out.println("📌 认证类型：" + verification.getVerificationType());
        
        User user = userMapper.selectById(verification.getUserId());
        System.out.println("👤 查询到的用户：" + (user != null ? user.getUsername() : "不存在"));
        
        if (user == null) {
            System.out.println("❌ 用户不存在，跳过更新");
            return;
        }
        
        // 解析 extraInfo 获取详细信息
        String extraInfo = verification.getExtraInfo();
        System.out.println("📄 extraInfo: " + extraInfo);
        
        if (extraInfo != null && !extraInfo.trim().isEmpty()) {
            try {
                com.fasterxml.jackson.databind.JsonNode jsonNode = 
                    new com.fasterxml.jackson.databind.ObjectMapper().readTree(extraInfo);
                
                // 根据认证类型更新用户信息
                String verificationType = verification.getVerificationType();
                System.out.println("🏷️ 认证类型：" + verificationType);
                
                // 先清除所有旧的身份标记
                user.setIsStaff(null);
                user.setIsMerchant(null);
                user.setIsOrganization(null);
                System.out.println("️ 已清除所有旧身份标记");
                
                if ("student".equals(verificationType)) {
                    System.out.println("🎓 处理学生认证...");
                    String studentId = jsonNode.has("studentId") ? jsonNode.get("studentId").asText() : null;
                    if (studentId != null && !studentId.trim().isEmpty()) {
                        user.setStudentId(studentId);
                        System.out.println("✅ 设置学号：" + studentId);
                    }
                    user.setIsVerified(1);
                    System.out.println("✅ 设置 is_verified = 1");
                    
                } else if ("staff".equals(verificationType)) {
                    System.out.println("👨‍🏫 处理教职工认证...");
                    String staffId = jsonNode.has("staffId") ? jsonNode.get("staffId").asText() : null;
                    String department = jsonNode.has("department") ? jsonNode.get("department").asText() : null;
                    System.out.println("工号：" + staffId + ", 部门：" + department);
                    if (staffId != null && !staffId.trim().isEmpty()) {
                        user.setStudentId(staffId); // 复用 student_id 字段存储工号
                        System.out.println("✅ 设置工号：" + staffId);
                    }
                    if (department != null && !department.trim().isEmpty()) {
                        user.setDepartment(department);
                        System.out.println("✅ 设置部门：" + department);
                    }
                    user.setIsStaff(1);
                    user.setIsVerified(1);
                    System.out.println("✅ 设置 is_staff = 1, is_verified = 1");
                    // 清除学生身份标记
                    user.setStudentId(null); // 如果是教职工，清除学号
                    System.out.println("✅ 清除学生身份标记");
                    
                } else if ("merchant".equals(verificationType)) {
                    System.out.println("🏪 处理商户认证...");
                    String shopName = jsonNode.has("shopName") ? jsonNode.get("shopName").asText() : null;
                    if (shopName != null && !shopName.trim().isEmpty()) {
                        user.setRealName(shopName); // 使用 real_name 存储店铺名
                        System.out.println("✅ 设置店铺名：" + shopName);
                    }
                    user.setIsMerchant(1);
                    user.setIsVerified(1);
                    System.out.println("✅ 设置 is_merchant = 1, is_verified = 1");
                    // 清除学生身份标记
                    user.setStudentId(null); // 如果是商户，清除学号
                    System.out.println("✅ 清除学生身份标记");
                    
                } else if ("organization".equals(verificationType)) {
                    System.out.println("🏛️ 处理组织认证...");
                    String organizationName = jsonNode.has("organizationName") ? jsonNode.get("organizationName").asText() : null;
                    String leaderName = jsonNode.has("leaderName") ? jsonNode.get("leaderName").asText() : null;
                    if (organizationName != null && !organizationName.trim().isEmpty()) {
                        user.setRealName(organizationName);
                        System.out.println("✅ 设置组织名：" + organizationName);
                    }
                    if (leaderName != null && !leaderName.trim().isEmpty()) {
                        user.setDepartment(leaderName);
                        System.out.println("✅ 设置负责人：" + leaderName);
                    }
                    user.setIsOrganization(1);
                    user.setIsVerified(1);
                    System.out.println("✅ 设置 is_organization = 1, is_verified = 1");
                    // 清除学生身份标记
                    user.setStudentId(null); // 如果是团体，清除学号
                    System.out.println("✅ 清除学生身份标记");
                }
                
                System.out.println("💾 准备更新用户表...");
                
                // 使用 UpdateWrapper 确保 null 值也能被更新
                com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<User> updateWrapper = new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
                updateWrapper.eq("id", user.getId())
                    .set("is_staff", user.getIsStaff())
                    .set("is_merchant", user.getIsMerchant())
                    .set("is_organization", user.getIsOrganization())
                    .set("is_verified", user.getIsVerified());
                
                // 如果有店铺名，也更新
                if (user.getRealName() != null) {
                    updateWrapper.set("real_name", user.getRealName());
                }
                
                int updateCount = userMapper.update(null, updateWrapper);
                System.out.println("✅ 用户表更新成功，影响行数：" + updateCount);
                
            } catch (Exception e) {
                // JSON 解析失败，记录日志但不影响主流程
                System.err.println("❌ 解析 extraInfo 失败：" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ extraInfo 为空，跳过更新");
        }
        
        System.out.println("✅ updateUserIdentity 执行完成");
    }
    
    @Override
    public Result rejectApplication(Integer applicationId, String reason) {
        try {
            // 查询待审核的认证申请
            UserVerification verification = userVerificationMapper.selectById(applicationId);
            if (verification == null) {
                return Result.error("认证申请不存在");
            }
            
            if (!"pending".equals(verification.getStatus())) {
                return Result.error("该申请已审核过");
            }
            
            // 获取当前管理员 ID
            Long adminId = jwtUtil.getCurrentAdminId();
            if (adminId == null) {
                adminId = 1L; // 降级方案：如果无法获取，使用默认值
            }
            
            // 拒绝认证
            verification.setStatus("rejected");
            verification.setRejectionReason(reason);
            verification.setReviewerId(adminId.intValue()); // 转换为 Integer
            verification.setReviewedAt(LocalDateTime.now());
            
            userVerificationMapper.updateById(verification);
            
            // 记录操作日志
            try {
                OperationLog log = new OperationLog();
                log.setAdminId(adminId);
                log.setAdminName(jwtUtil.getCurrentAdminUsername());
                log.setOperation("REJECT_IDENTITY");
                log.setModule("身份认证");
                log.setTargetId(verification.getId().longValue());
                log.setDetail(String.format("拒绝用户 ID=%d 的 %s 认证申请，理由：%s", 
                    verification.getUserId(), 
                    getIdentityTypeName(verification.getVerificationType()),
                    reason != null ? reason : "未提供"));
                log.setCreatedAt(LocalDateTime.now());
                operationLogMapper.insert(log);
                System.out.println("📝 操作日志记录成功");
            } catch (Exception e) {
                System.err.println("⚠️ 记录操作日志失败：" + e.getMessage());
            }
            
            return Result.success("认证申请已拒绝");
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取身份类型中文名称
     */
    private String getIdentityTypeName(String type) {
        switch (type) {
            case "student":
                return "学生";
            case "staff":
                return "教职工";
            case "merchant":
                return "商户";
            case "organization":
                return "团体/部门";
            default:
                return type;
        }
    }
}
