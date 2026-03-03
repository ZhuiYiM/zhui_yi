package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户关注表实体类
 * 对应需求文档中的user_follows表结构
 */
@Data
@TableName("user_follows")
public class UserFollows {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long followerId; // 关注者ID
    
    private Long followedId; // 被关注者ID
    
    private LocalDateTime createdAt;
}