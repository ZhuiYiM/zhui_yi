package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.UserIdentity;
import com.example.demo.entity.UserVerification;
import com.example.demo.mapper.UserIdentityMapper;
import com.example.demo.mapper.UserVerificationMapper;
import com.example.demo.service.UserIdentityService;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 用户身份 Service 实现类
 */
@Service
public class UserIdentityServiceImpl extends ServiceImpl<UserIdentityMapper, UserIdentity> 
        implements UserIdentityService {
    
    @Autowired
    private UserIdentityMapper userIdentityMapper;
    
    @Autowired
    private UserVerificationMapper userVerificationMapper;
    
    @Autowired
    private ApplicationContext applicationContext;
    
    private UserService getUserService() {
        return applicationContext.getBean(UserService.class);
    }
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public List<UserIdentity> getUserIdentities(Long userId) {
        LambdaQueryWrapper<UserIdentity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserIdentity::getUserId, userId)
               .orderByDesc(UserIdentity::getVerified)
               .orderByDesc(UserIdentity::getCreatedAt);
        return this.list(wrapper);
    }
    
    @Override
    public UserIdentity getPrimaryIdentity(Long userId) {
        // 优先返回已认证的身份，按认证时间排序
        LambdaQueryWrapper<UserIdentity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserIdentity::getUserId, userId)
               .eq(UserIdentity::getVerified, 1)
               .orderByDesc(UserIdentity::getVerifiedAt)
               .last("LIMIT 1");
        
        UserIdentity identity = this.getOne(wrapper);
        
        // 如果没有已认证的，返回第一个
        if (identity == null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserIdentity::getUserId, userId)
                   .orderByAsc(UserIdentity::getCreatedAt)
                   .last("LIMIT 1");
            identity = this.getOne(wrapper);
        }
        
        return identity;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result applyIdentity(Long userId, String identityType, Map<String, Object> materials) {
        // 1. 验证身份类型
        List<String> validTypes = Arrays.asList("student", "staff", "merchant", "organization");
        if (!validTypes.contains(identityType)) {
            return Result.error("不支持的身份类型");
        }
        
        // 2. 检查用户是否已有身份记录
        LambdaQueryWrapper<UserIdentity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserIdentity::getUserId, userId);
        
        UserIdentity existingIdentity = this.getOne(wrapper);
        
        // 3. 处理身份记录
        UserIdentity identity;
        if (existingIdentity != null) {
            // 用户已有身份记录
            if (existingIdentity.getVerified() == 1) {
                // 已有已认证的身份，检查是否是同一种
                if (existingIdentity.getIdentityType().equals(identityType)) {
                    return Result.error("您已拥有该身份认证，无需重复申请");
                }
                // 用户申请切换身份：更新现有记录，但设置为未认证状态
                identity = existingIdentity;
                identity.setIdentityType(identityType);
                identity.setIdentityName(getIdentityTypeName(identityType));
                identity.setVerified(0); // 重新认证
                identity.setVerifiedAt(null);
            } else {
                // 已有未认证的记录，更新为新的身份类型申请
                identity = existingIdentity;
                identity.setIdentityType(identityType);
                identity.setIdentityName(getIdentityTypeName(identityType));
            }
        } else {
            // 用户没有任何身份记录，创建新的
            identity = new UserIdentity();
            identity.setUserId(userId);
            identity.setIdentityType(identityType);
            identity.setIdentityName(getIdentityTypeName(identityType));
            identity.setVerified(0);
            identity.setCreatedAt(LocalDateTime.now());
        }
        
        // 4. 设置额外信息
        try {
            String extraInfoJson = objectMapper.writeValueAsString(materials);
            identity.setExtraInfo(extraInfoJson);
        } catch (Exception e) {
            return Result.error("材料格式错误");
        }
        
        identity.setUpdatedAt(LocalDateTime.now());
        
        // 5. 保存或更新身份记录
        boolean success = existingIdentity != null ? 
            this.updateById(identity) : this.save(identity);
        
        if (!success) {
            return Result.error("申请保存失败，请稍后重试");
        }
        
        // 5. 创建认证申请记录
        UserVerification verification = new UserVerification();
        verification.setUserId(userId.intValue());
        verification.setVerificationType(mapIdentityTypeToVerificationType(identityType));
        verification.setStatus("pending");
        verification.setSubmittedAt(LocalDateTime.now());
        
        // 将材料信息存储到相应字段
        
        // ========== 旧字段兼容（用于快速索引，详细信息请查看 extra_info）==========
        if (materials.containsKey("idCard")) {
            verification.setIdCard((String) materials.get("idCard"));
        }
        if (materials.containsKey("realName")) {
            verification.setRealName((String) materials.get("realName"));
        }
        if (materials.containsKey("studentId")) {
            verification.setStudentId((String) materials.get("studentId"));
        }
        if (materials.containsKey("college")) {
            verification.setCollege((String) materials.get("college"));
        }
        
        // ========== 新字段支持（复用旧字段存储，完整数据在 extra_info）==========
        // staffId → student_id 字段（学号/工号复用）
        if (materials.containsKey("staffId")) {
            verification.setStudentId((String) materials.get("staffId"));
        }
        // department → college 字段（学院/部门复用）
        if (materials.containsKey("department")) {
            verification.setCollege((String) materials.get("department"));
        }
        // shopName → real_name 字段（姓名/店铺名复用）
        if (materials.containsKey("shopName")) {
            verification.setRealName((String) materials.get("shopName"));
        }
        // organizationName → real_name 字段（姓名/组织名复用）
        if (materials.containsKey("organizationName")) {
            verification.setRealName((String) materials.get("organizationName"));
        }
        // leaderName → student_id 字段（学号/负责人复用）
        if (materials.containsKey("leaderName")) {
            verification.setStudentId((String) materials.get("leaderName"));
        }
        
        // ========== URL 和补充信息（仅存储在 extra_info 中）==========
        // 完整的 URL 信息存储在 extra_info 的 JSON 中
        
        userVerificationMapper.insert(verification);
        
        // 同时将完整的材料信息存储到 extra_info 字段（JSON 格式）
        try {
            String extraInfoJson = objectMapper.writeValueAsString(materials);
            verification.setExtraInfo(extraInfoJson);
            userVerificationMapper.updateById(verification);
        } catch (Exception e) {
            // JSON 序列化失败不影响主流程
        }
        
        // 6. 根据身份类型更新用户表
        User user = getUserService().getById(userId);
        if (user != null) {
            if ("merchant".equals(identityType)) {
                user.setIsMerchant(0); // 待审核
            } else if ("organization".equals(identityType)) {
                user.setIsOrganization(0);
            } else if ("staff".equals(identityType)) {
                // 教职工身份处理
            }
            getUserService().updateById(user);
        }
        
        return Result.success("认证申请已提交，请等待审核");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result verifyIdentity(Long identityId, Boolean approved, String reason, Long reviewerId) {
        UserIdentity identity = this.getById(identityId);
        if (identity == null) {
            return Result.error("身份记录不存在");
        }
        
        if (approved) {
            // 通过认证
            identity.setVerified(1);
            identity.setVerifiedAt(LocalDateTime.now());
            
            // 更新用户表
            User user = getUserService().getById(identity.getUserId());
            if (user != null) {
                // 先清除所有旧的身份标记
                user.setIsStaff(null);
                user.setIsMerchant(null);
                user.setIsOrganization(null);
                // 根据新身份设置标记
                if ("merchant".equals(identity.getIdentityType())) {
                    user.setIsMerchant(1);
                } else if ("organization".equals(identity.getIdentityType())) {
                    user.setIsOrganization(1);
                } else if ("staff".equals(identity.getIdentityType())) {
                    user.setIsStaff(1);  // 修复：设置教职工身份
                } else if ("student".equals(identity.getIdentityType())) {
                    // 学生身份处理
                }
                getUserService().updateById(user);
            }
            
            // 将该用户的其他身份记录设置为未认证或删除
            LambdaQueryWrapper<UserIdentity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserIdentity::getUserId, identity.getUserId())
                   .ne(UserIdentity::getId, identityId); // 排除当前这条
            boolean deleted = this.remove(wrapper);
            System.out.println("✅ 已删除其他身份记录：" + deleted);
        } else {
            // 拒绝认证
            identity.setVerified(0);
            this.updateById(identity);
        }
        
        // 更新认证申请记录
        LambdaQueryWrapper<UserVerification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserVerification::getUserId, identity.getUserId())
               .eq(UserVerification::getVerificationType, 
                   mapIdentityTypeToVerificationType(identity.getIdentityType()))
               .orderByDesc(UserVerification::getSubmittedAt)
               .last("LIMIT 1");
        
        UserVerification verification = userVerificationMapper.selectOne(wrapper);
        if (verification != null) {
            verification.setStatus(approved ? "approved" : "rejected");
            verification.setReviewedAt(LocalDateTime.now());
            verification.setReviewerId(reviewerId.intValue());
            if (!approved) {
                verification.setRejectionReason(reason);
            }
            userVerificationMapper.updateById(verification);
        }
        
        // 如果审核通过，返回需要重新登录的提示
        if (approved) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("message", "认证已通过，请重新登录以更新身份信息");
            responseData.put("force_relogin", true);
            return Result.success(responseData);
        }
        
        return Result.success(approved ? "认证已通过" : "认证已拒绝");
    }
    
    @Override
    public boolean hasIdentity(Long userId, String identityType, boolean requireVerified) {
        LambdaQueryWrapper<UserIdentity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserIdentity::getUserId, userId)
               .eq(UserIdentity::getIdentityType, identityType);
        
        if (requireVerified) {
            wrapper.eq(UserIdentity::getVerified, 1);
        }
        
        return this.count(wrapper) > 0;
    }
    
    @Override
    public List<Map<String, Object>> getIdentityBadges(Long userId) {
        List<UserIdentity> identities = getUserIdentities(userId);
        List<Map<String, Object>> badges = new ArrayList<>();
        
        for (UserIdentity identity : identities) {
            Map<String, Object> badge = new HashMap<>();
            badge.put("type", identity.getIdentityType());
            badge.put("name", identity.getIdentityName());
            badge.put("verified", identity.getVerified() == 1);
            badge.put("icon", getIdentityIcon(identity.getIdentityType()));
            badge.put("color", getIdentityColor(identity.getIdentityType()));
            
            if (identity.getVerified() == 1 && identity.getVerifiedAt() != null) {
                // 将 LocalDateTime 转换为字符串，避免 JWT 序列化问题
                badge.put("verifiedAt", identity.getVerifiedAt().toString());
            }
            
            badges.add(badge);
        }
        
        return badges;
    }
    
    /**
     * 获取身份类型显示名称
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
                return "未知身份";
        }
    }
    
    /**
     * 获取身份图标
     */
    private String getIdentityIcon(String type) {
        switch (type) {
            case "student":
                return "User";
            case "staff":
                return "Suit";
            case "merchant":
                return "Shop";
            case "organization":
                return "OfficeBuilding";
            default:
                return "User";
        }
    }
    
    /**
     * 获取身份颜色
     */
    private String getIdentityColor(String type) {
        switch (type) {
            case "student":
                return "#409EFF"; // 蓝色
            case "staff":
                return "#67C23A"; // 绿色
            case "merchant":
                return "#E6A23C"; // 橙色
            case "organization":
                return "#909399"; // 灰色
            default:
                return "#909399";
        }
    }
    
    /**
     * 映射身份类型到认证类型
     */
    private String mapIdentityTypeToVerificationType(String identityType) {
        switch (identityType) {
            case "student":
                return "student";
            case "staff":
                return "staff";
            case "merchant":
                return "merchant";
            case "organization":
                return "organization";
            default:
                return "other";
        }
    }
}
