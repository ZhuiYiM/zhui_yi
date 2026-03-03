// entity/Topic.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("topics")
public class Topic {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private String tags; // JSON格式存储标签数组
    private Integer likeCount;
    private Integer commentCount;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
