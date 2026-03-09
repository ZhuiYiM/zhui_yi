// entity/TagLevel1.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 一级标签实体类 - 用户身份标签
 */
@Data
@TableName("tag_level_1")
public class TagLevel1 {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 标签代码：student/merchant/organization/individual/followed
     */
    private String code;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 图标 URL
     */
    private String icon;
    
    /**
     * 描述
     */
    private String description;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
    
    /**
     * 是否启用
     */
    private Boolean isActive;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
