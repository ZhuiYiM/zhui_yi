package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Order;
import com.example.demo.entity.dto.OrderDTO;

import java.util.Map;

/**
 * 订单服务接口
 */
public interface OrderService extends IService<Order> {
    
    /**
     * 创建订单
     */
    Order createOrder(OrderDTO orderDTO, Integer buyerId);
    
    /**
     * 获取我的订单列表
     */
    Map<String, Object> getMyOrders(Integer userId, Map<String, Object> params);
    
    /**
     * 获取订单详情
     */
    Map<String, Object> getOrderDetail(Integer orderId, Integer userId);
    
    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Integer orderId, Integer status);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Integer orderId, Integer userId, String reason, String note);
    
    /**
     * 支付订单
     */
    boolean payOrder(Integer orderId, Integer userId, String paymentMethod);
    
    /**
     * 获取卖家订单列表
     */
    Map<String, Object> getSellerOrders(Integer userId, Map<String, Object> params);
    
    /**
     * 卖家发货
     */
    boolean shipOrder(Integer orderId, Integer userId, String trackingNumber, String courier);
}
