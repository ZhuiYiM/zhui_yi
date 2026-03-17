package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品规格实体类
 */
@Data
@TableName("product_specifications")
public class ProductSpecification {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer productId;
    
    private String specName;
    
    private String specValue;
    
    private String specIcon;
    
    private Integer stockQuantity;
    
    private BigDecimal priceAdjustment;
    
    private Integer isDefault;
    
    private Integer sortOrder;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
