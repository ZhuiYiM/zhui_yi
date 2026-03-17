package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("buyer_id")
    private Integer buyerId;
    
    @TableField("seller_id")
    private Integer sellerId;
    
    @TableField("product_id")
    private Integer productId;
    
    @TableField("order_status")
    private Integer orderStatus; // 0-待付款，1-待发货，2-已发货，3-已完成，4-已取消
    
    @TableField("total_amount")
    private BigDecimal totalAmount;
    
    @TableField("buyer_contact")
    private String buyerContact; // 买家联系方式
    
    @TableField("buyer_message")
    private String buyerMessage; // 买家留言
    
    @TableField("payment_method")
    private String paymentMethod; // wechat-微信，alipay-支付宝，station-站内支付
    
    @TableField("payment_status")
    private Integer paymentStatus; // 0-未支付，1-已支付
    
    @TableField("payment_time")
    private LocalDateTime paymentTime; // 支付时间
    
    @TableField("created_at")
    private LocalDateTime createdAt;
    
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
