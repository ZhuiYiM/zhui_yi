package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题评论表实体类
 * 对应需求文档中的topic_comments表结构
 */
@Data
@TableName("topic_comments")
public class TopicComments {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long topicId;
    
    private Long userId;
    
    private Long parentId; // 父评论ID，用于回复功能
    
    private String content;
    
    private Integer likesCount;
    
    private Integer status; // 状态 1-正常, 0-删除
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}