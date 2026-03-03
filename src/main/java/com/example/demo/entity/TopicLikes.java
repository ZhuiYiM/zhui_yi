package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题点赞记录表实体类
 * 对应需求文档中的topic_likes表结构
 */
@Data
@TableName("topic_likes")
public class TopicLikes {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long topicId;
    
    private Long userId;
    
    private LocalDateTime createdAt;
}