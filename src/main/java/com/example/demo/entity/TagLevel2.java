// entity/TagLevel2.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 二级标签实体类 - 话题分类
 */
@Data
@TableName("tag_level_2")
public class TagLevel2 {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 标签代码
     */
    private String code;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 父级 ID（用于扩展子分类）
     */
    private Integer parentId;
    
    /**
     * 图标 URL
     */
    private String icon;
    
    /**
     * 标签颜色
     */
    private String color;
    
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
