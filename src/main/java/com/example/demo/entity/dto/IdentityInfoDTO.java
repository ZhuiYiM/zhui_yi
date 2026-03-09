package com.example.demo.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户身份信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityInfoDTO {
    /**
     * 用户 ID
     */
    private Integer userId;
    
    /**
     * 一级身份标签代码
     */
    private String level1Tag;
    
    /**
     * 一级身份标签名称
     */
    private String level1TagName;
    
    /**
     * 一级身份标签图标
     */
    private String level1TagIcon;
    
    /**
     * 认证状态详情
     */
    private CertificationStatus certifications;
    
    /**
     * 学号（学生身份）
     */
    private String studentId;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 认证状态内部类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CertificationStatus {
        /**
         * 实名认证状态
         */
        private Boolean realName;
        
        /**
         * 学生认证状态
         */
        private Boolean student;
        
        /**
         * 商户认证状态
         */
        private Boolean merchant;
        
        /**
         * 团体认证状态
         */
        private Boolean organization;
    }
}
