// entity/Like.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("likes")
public class Like {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String targetType; // topic, comment, product
    private Integer targetId;
    private LocalDateTime createdAt;
}
