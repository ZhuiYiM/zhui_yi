// entity/Product.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("products")
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private Integer categoryId;
    private String images; // JSON格式存储图片URL数组
    private Integer status; // 0-下架，1-上架，2-已售出
    private Integer viewCount;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
