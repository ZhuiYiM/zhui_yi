package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签表实体类
 * 对应需求文档中的tags表结构
 */
@Data
@TableName("tags")
public class Tags {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer usageCount;
    
    private LocalDateTime createdAt;
}