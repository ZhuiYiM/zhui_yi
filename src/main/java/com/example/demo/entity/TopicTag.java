package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题标签实体（二级标签）
 * 包含系统标签和用户自定义标签
 */
@Data
@TableName("topic_tag")
public class TopicTag {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 标签代码（系统标签）或标签名称（用户自定义）
     */
    private String name;
    
    /**
     * 标签代码（用于系统标签）
     */
    private String code;
    
    /**
     * 标签类型：system/custom
     */
    private String type;
    
    /**
     * 使用次数
     */
    private Long usageCount;
    
    /**
     * 趋势分数（用于热门排序）
     */
    private Double trendScore;
    
    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;
    
    /**
     * 创建者用户 ID（用户自定义标签）
     */
    private Long createdBy;
    
    /**
     * 状态：active/banned/pending（用户自定义标签需要审核）
     */
    private String status;
    
    /**
     * 图标 URL
     */
    private String icon;
    
    /**
     * 标签颜色
     */
    private String color;
    
    /**
     * 补充说明（用户提交自定义标签时使用）
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
