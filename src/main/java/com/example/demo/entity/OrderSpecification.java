package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单规格关联实体类
 */
@Data
@TableName("order_specifications")
public class OrderSpecification {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private Integer orderId;
    
    private Integer specId;
    
    private String specName;
    
    private String specValue;
    
    private String specIcon;
    
    private Integer quantity;
    
    private LocalDateTime createdAt;
}
