package com.example.demo.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单 DTO
 */
@Data
public class OrderDTO {
    
    private Integer productId;
    private Integer sellerId;
    private BigDecimal totalAmount;
    private Integer orderStatus; // 0-待付款，1-待发货，2-已发货，3-已完成，4-已取消
    private String buyerContact; // 买家联系方式
    private String message; // 购买留言
    private Integer quantity; // 购买数量
    private String paymentMethod; // wechat-微信，alipay-支付宝，station-站内支付
    
    /**
     * 选中的规格列表
     */
    private List<OrderSpecItemDTO> selectedSpecifications;
    
    /**
     * 订单项 DTO
     */
    @Data
    public static class OrderSpecItemDTO {
        private Integer specId; // 规格 ID
        private String specName; // 规格名称
        private String specValue; // 规格值
        private String specIcon; // 规格图标
        private Integer quantity; // 该规格的数量
    }
}
