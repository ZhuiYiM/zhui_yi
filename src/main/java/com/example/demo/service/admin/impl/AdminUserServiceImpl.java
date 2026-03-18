package com.example.demo.service.admin.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.admin.AdminUser;
import com.example.demo.mapper.admin.AdminUserMapper;
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
    private JwtUtil jwtUtil;
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
        if (!passwordEncoder.matches(password, adminUser.getPassword())) {
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
}
