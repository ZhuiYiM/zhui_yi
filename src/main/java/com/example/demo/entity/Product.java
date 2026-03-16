// entity/Product.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "products", autoResultMap = true)
public class Product {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice; // 原价
    private Integer categoryId;
    
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images; // JSON 格式存储图片 URL 数组
    
    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tradeMethods; // JSON 格式存储交易方式
    
    private String contactInfo; // 联系方式
    private String location; // 交易地点
    private Integer status; // 0-下架，1-上架，2-已售出
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount; // 收藏人数
    private Integer isHot; // 是否热门：0-否，1-是
    private Integer isRecommend; // 是否推荐：0-否，1-是
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt; // 软删除时间
}
