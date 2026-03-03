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
    
    private String content;
    
    private String images; // JSON格式存储图片URL数组
    
    private String tags; // JSON格式存储标签数组
    
    private Integer likesCount;
    
    private Integer commentsCount;
    
    private Integer viewsCount;
    
    private Integer isEssence; // 是否精华 0-否, 1-是
    
    private Integer isTop; // 是否置顶 0-否, 1-是
    
    private Integer status; // 状态 1-正常, 0-删除
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}