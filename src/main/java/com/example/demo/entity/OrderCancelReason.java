package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单取消原因记录
 */
@Data
@TableName("order_cancel_reason")
public class OrderCancelReason {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    @TableField("order_id")
    private Integer orderId;
    
    @TableField("buyer_id")
    private Integer buyerId;
    
    @TableField("seller_id")
    private Integer sellerId;
    
    @TableField("reason")
    private String reason; // 取消原因：不想买了、信息填写错误、卖家同意退款、其他原因
    
    @TableField("note")
    private String note; // 补充说明
    
    @TableField("created_at")
    private LocalDateTime createdAt;
}
