package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 身份标签实体类
 */
@Data
@TableName("identity_tags")
public class IdentityTags {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String code;
    
    private String name;
    
    private String icon;
    
    private String description;
    
    private Integer sortOrder;
    
    private Boolean isActive;
    
    private LocalDateTime createdAt;
}
