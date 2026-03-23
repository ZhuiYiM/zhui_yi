// entity/TagLevel5.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 五级标签实体类 - 商业标签
 */
@Data
@TableName("tag_level_5")
public class TagLevel5 {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标签代码：secondhand/service/parttime/...
     */
    private String code;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 所属分类（二手交易/服务/美食/...）
     */
    private String category;
    
    /**
     * 图标 URL
     */
    private String icon;
    
    /**
     * 标签颜色
     */
    private String color;
    
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
     * 状态：active/banned/pending
     */
    private String status;
    
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
