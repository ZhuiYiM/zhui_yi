package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.Result;
import com.example.demo.entity.UserIdentity;

import java.util.List;
import java.util.Map;

/**
 * 用户身份 Service 接口
 */
public interface UserIdentityService extends IService<UserIdentity> {
    
    /**
     * 获取用户的身份列表
     * @param userId 用户 ID
     * @return 身份列表
     */
    List<UserIdentity> getUserIdentities(Long userId);
    
    /**
     * 获取用户的主要身份
     * @param userId 用户 ID
     * @return 主要身份
     */
    UserIdentity getPrimaryIdentity(Long userId);
    
    /**
     * 申请身份认证
     * @param userId 用户 ID
     * @param identityType 身份类型
     * @param materials 认证材料
     * @return 结果
     */
    Result applyIdentity(Long userId, String identityType, Map<String, Object> materials);
    
    /**
     * 审核身份认证（管理员）
     * @param identityId 身份 ID
     * @param approved 是否通过
     * @param reason 审核意见
     * @param reviewerId 审核人 ID
     * @return 结果
     */
    Result verifyIdentity(Long identityId, Boolean approved, String reason, Long reviewerId);
    
    /**
     * 检查用户是否具有指定身份
     * @param userId 用户 ID
     * @param identityType 身份类型
     * @param requireVerified 是否要求已认证
     * @return true-具有该身份
     */
    boolean hasIdentity(Long userId, String identityType, boolean requireVerified);
    
    /**
     * 获取用户的身份标识信息（用于 Token）
     * @param userId 用户 ID
     * @return 身份标识列表
     */
    List<Map<String, Object>> getIdentityBadges(Long userId);
}
