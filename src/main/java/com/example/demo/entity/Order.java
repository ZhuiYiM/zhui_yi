package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
    private Integer buyer_id;
    private Integer seller_id;
    private Integer product_id;
    private Integer order_status;
    private BigDecimal total_amount;
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
}
