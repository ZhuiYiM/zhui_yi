// entity/TagLevel4.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 四级标签实体类 - 自定义标签
 */
@Data
@TableName("tag_level_4")
public class TagLevel4 {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 所属分类（可选）
     */
    private String category;
    
    /**
     * 使用次数
     */
    private Long usageCount;
    
    /**
     * 趋势分数（用于热门排序）
     */
    private BigDecimal trendScore;
    
    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;
    
    /**
     * 创建者用户ID
     */
    private Long createdBy;
    
    /**
     * 状态：active/banned/pending
     */
    private String status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
