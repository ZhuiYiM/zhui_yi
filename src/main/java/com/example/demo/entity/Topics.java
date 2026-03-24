package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题表实体类
 * 对应需求文档中的topics表结构
 */
@Data
@TableName("topics")
public class Topics {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String title; // 话题标题
    
    private String content;
    
    private String images; // JSON 格式存储图片 URL 数组
        
    private String tags; // JSON 格式存储标签数组
        
    // 分级标签字段
    private String level1TagCode;      // 一级标签代码（身份标签：student/merchant/organization/admin）
    private String topicTagCodes;      // JSON 格式存储话题标签代码数组
    private String productTagCodes;    // JSON 格式存储商品标签代码数组
    private String locationTagCodes;   // JSON 格式存储地点标签代码数组
        
    private Integer likesCount;
        
    private Integer commentsCount;
        
    private Integer viewsCount;
        
    private Integer collectionsCount; // 收藏数量
        
    private Integer isEssence; // 是否精华 0-否，1-是
    
    private Integer isTop; // 是否置顶 0-否，1-是
        
    private Integer status; // 状态 1-正常，0-删除
        
    // 转发相关字段
    private Boolean isForwarded; // 是否为转发话题
    private Long forwardedFromTopicId; // 被转发的话题 ID（如果是转发话题）
    private Long forwardedFromProductId; // 被转发的商品 ID（如果是分享商品）
        
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}