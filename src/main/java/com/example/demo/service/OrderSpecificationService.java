package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.OrderSpecification;

import java.util.List;

/**
 * 订单规格关联服务接口
 */
public interface OrderSpecificationService extends IService<OrderSpecification> {
    
    /**
     * 根据订单 ID 获取所有规格
     */
    List<OrderSpecification> getByOrderId(Integer orderId);
    
    /**
     * 为订单批量保存规格
     */
    void batchSaveForOrder(Integer orderId, List<OrderSpecification> specifications);
}
