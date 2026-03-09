package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.common.Result;
import com.example.demo.entity.User;
import com.example.demo.entity.UserVerification;
import com.example.demo.entity.dto.IdentityInfoDTO;
import com.example.demo.entity.enums.IdentityLevel1;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.UserVerificationMapper;
import com.example.demo.service.IdentityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 身份识别服务实现类
 */
@Service
public class IdentityServiceImpl implements IdentityService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserVerificationMapper userVerificationMapper;
    
    @Override
    public Result getIdentityInfo(Integer userId) {
        try {
            // 查询用户信息
            User user = userMapper.selectById(userId);
            if (user == null) {
                return Result.error("用户不存在");
            }
            
            // 确定一级身份标签
            String level1Tag = determineUserLevel1Tag(userId);
            IdentityLevel1 identity = IdentityLevel1.fromCode(level1Tag);
            
            // 构建认证状态
            IdentityInfoDTO.CertificationStatus certificationStatus = 
                new IdentityInfoDTO.CertificationStatus(
                    user.getIsRealNameVerified() != null && user.getIsRealNameVerified() == 1,
                    user.getStudentId() != null && !user.getStudentId().trim().isEmpty(),
                    user.getIsMerchant() != null && user.getIsMerchant() == 1,
                    user.getIsOrganization() != null && user.getIsOrganization() == 1
                );
            
            // 构建返回结果
            IdentityInfoDTO identityInfo = new IdentityInfoDTO(
                userId,
                level1Tag,
                identity.getName(),
                identity.getIcon(),
                certificationStatus,
                user.getStudentId(),
                user.getRealName()
            );
            
            return Result.success(identityInfo);
        } catch (Exception e) {
            return Result.error("获取身份信息失败：" + e.getMessage());
        }
    }
    
    @Override
    public String determineUserLevel1Tag(Integer userId) {
        // 查询用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            return IdentityLevel1.SOCIETY.getCode(); // 默认返回社会
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
}
