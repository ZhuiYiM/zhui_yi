package com.example.demo.service.admin.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.UserVerification;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.mapper.admin.AdminUserMapper;
import com.example.demo.mapper.UserVerificationMapper;
import com.example.demo.service.admin.AdminUserService;
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
    private JwtUtil jwtUtil;
    
    // 使用静态实例，避免 Spring 代理问题
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public Result login(String username, String password) {
        System.out.println("🔐 管理员登录请求 - 用户名：" + username);
        System.out.println("🔐 输入密码：" + password);
        
        // 查询管理员
        AdminUser adminUser = this.lambdaQuery()
                .eq(AdminUser::getUsername, username)
                .one();
        
        if (adminUser == null) {
            System.out.println("❌ 管理员不存在");
            return Result.error("用户名或密码错误");
        }
        
        System.out.println("✅ 找到管理员 - ID: " + adminUser.getId());
        System.out.println("✅ 数据库密码哈希：" + adminUser.getPassword());
        System.out.println("✅ 密码哈希长度：" + adminUser.getPassword().length());
        
        // 检查状态
        if (adminUser.getStatus() != 1) {
            System.out.println("❌ 账号已被禁用");
            return Result.error("账号已被禁用");
        }
        
        // 验证密码 - 添加调试日志
        boolean matches = false;
        try {
            matches = passwordEncoder.matches(password, adminUser.getPassword());
            System.out.println("🔍 密码验证结果：" + matches);
            
            // 如果验证失败，尝试多种方法
            if (!matches) {
                System.out.println("⚠️ BCrypt 验证失败，尝试备用方案...");
                System.out.println("输入密码：'" + password + "'");
                System.out.println("数据库密码：'" + adminUser.getPassword() + "'");
                
                // 尝试重新生成哈希并比较
                String testHash = passwordEncoder.encode(password);
                System.out.println("新生成的哈希：" + testHash);
                System.out.println("新哈希验证：" + passwordEncoder.matches(password, testHash));
            }
        } catch (Exception e) {
            System.out.println("❌ 密码验证异常：" + e.getMessage());
            e.printStackTrace();
        }
        
        if (!matches) {
            System.out.println("❌ 密码不匹配");
            return Result.error("用户名或密码错误");
        }
        
        System.out.println("✅ 密码验证成功");
        
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
        
        System.out.println("✅ 登录成功，生成 Token: " + token);
        
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
        
        // 验证密码 - 添加调试日志
        boolean matches = passwordEncoder.matches(password, adminUser.getPassword());
        System.out.println("🔍 密码验证 - 用户名：" + username);
        System.out.println("🔍 输入密码：" + password);
        System.out.println("🔍 数据库密码哈希：" + adminUser.getPassword());
        System.out.println("🔍 验证结果：" + matches);
        
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
        
        System.out.println("✅ 密码已重置 - 用户名：" + username);
        System.out.println("✅ 新密码哈希：" + encodedPassword);
        
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
            
            // 获取当前管理员 ID（从上下文或参数中获取，这里暂时硬编码）
            Integer adminId = 1; // TODO: 从登录上下文中获取
            
            if (pass) {
                // 通过认证
                verification.setStatus("approved");
                verification.setReviewerId(adminId);
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
                verification.setReviewerId(adminId);
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
            Integer adminId = 1; // TODO: 从登录上下文中获取
            
            // 通过认证
            verification.setStatus("approved");
            verification.setReviewerId(adminId);
            verification.setReviewedAt(LocalDateTime.now());
            if (adminRemark != null && !adminRemark.trim().isEmpty()) {
                // 如果有备注，可以存储在某个字段中
                // 可以考虑添加 admin_remark 字段到 UserVerification 实体
            }
            
            userVerificationMapper.updateById(verification);
            return Result.success("认证申请已通过");
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
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
            Integer adminId = 1; // TODO: 从登录上下文中获取
            
            // 拒绝认证
            verification.setStatus("rejected");
            verification.setRejectionReason(reason);
            verification.setReviewerId(adminId);
            verification.setReviewedAt(LocalDateTime.now());
            
            userVerificationMapper.updateById(verification);
            return Result.success("认证申请已拒绝");
        } catch (Exception e) {
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}
