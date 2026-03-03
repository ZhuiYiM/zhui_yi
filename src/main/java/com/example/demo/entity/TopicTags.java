package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题标签关联表实体类
 * 对应需求文档中的topic_tags表结构
 */
@Data
@TableName("topic_tags")
public class TopicTags {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long topicId;
    
    private Long tagId;
    
    private LocalDateTime createdAt;
}