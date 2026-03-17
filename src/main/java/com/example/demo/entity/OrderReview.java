package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单评价
 */
@Data
@TableName("order_review")
public class OrderReview {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("order_id")
    private Integer orderId;
    
    @TableField("product_id")
    private Integer productId;
    
    @TableField("buyer_id")
    private Integer buyerId;
    
    @TableField("seller_id")
    private Integer sellerId;
    
    @TableField("rating")
    private Integer rating; // 评分 1-5 星
    
    @TableField("content")
    private String content; // 评价内容
    
    @TableField("images")
    private String images; // 评价图片，多张用逗号分隔
    
    @TableField("reply")
    private String reply; // 卖家回复
    
    @TableField("reply_time")
    private LocalDateTime replyTime; // 回复时间
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
