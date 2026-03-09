// entity/dto/UserPublicInfoDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 用户公开信息 DTO
 * 用于在话题详情页、评论页展示部分用户信息（脱敏处理）
 */
@Data
public class UserPublicInfoDTO {
    
    private Long id;
    private String username;
    private String avatarUrl;
    
    /**
     * 学院（脱敏）
     */
    private String college;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * 用户身份标签
     */
    private String identityType;
    private String identityName;
    
    /**
     * 统计信息
     */
    private Integer topicCount;
    private Integer followerCount;
    private Integer followingCount;
    
    /**
     * 是否已关注当前查看者
     */
    private Boolean isFollowed = false;
}
