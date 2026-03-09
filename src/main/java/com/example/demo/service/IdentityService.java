package com.example.demo.service;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.IdentityInfoDTO;

/**
 * 身份识别服务接口
 */
public interface IdentityService {
    /**
     * 获取用户身份信息
     * @param userId 用户 ID
     * @return 用户身份信息
     */
    Result getIdentityInfo(Integer userId);
    
    /**
     * 确定用户的一级身份标签
     * @param userId 用户 ID
     * @return 一级身份标签代码
     */
    String determineUserLevel1Tag(Integer userId);
}
