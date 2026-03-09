// entity/TagLevel3.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 三级标签实体类 - 地点实体
 */
@Data
@TableName("tag_level_3")
public class TagLevel3 {
    
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
     * 地点类型：campus/building/facility/area/other
     */
    private String locationType;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 详细地址
     */
    private String address;
    
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
