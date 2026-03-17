package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.OrderCancelReason;

import java.util.List;
import java.util.Map;

/**
 * 订单取消原因服务
 */
public interface OrderCancelReasonService extends IService<OrderCancelReason> {
    
    /**
     * 记录订单取消原因
     */
    void recordCancelReason(Integer orderId, Integer buyerId, Integer sellerId, String reason, String note);
    
    /**
     * 统计取消原因
     */
    List<Map<String, Object>> getCancelReasonStats(Integer userId);
}
