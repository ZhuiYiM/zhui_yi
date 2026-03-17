package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.OrderSpecification;
import com.example.demo.mapper.OrderSpecificationMapper;
import com.example.demo.service.OrderSpecificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 订单规格关联服务实现
 */
@Service
public class OrderSpecificationServiceImpl extends ServiceImpl<OrderSpecificationMapper, OrderSpecification>
    implements OrderSpecificationService {
    
    @Override
    public List<OrderSpecification> getByOrderId(Integer orderId) {
        return baseMapper.selectByOrderId(orderId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveForOrder(Integer orderId, List<OrderSpecification> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return;
        }
        
        // 设置订单 ID
        for (OrderSpecification spec : specifications) {
            spec.setOrderId(orderId);
            if (spec.getQuantity() == null) {
                spec.setQuantity(1);
            }
        }
        
        // 批量插入
        baseMapper.batchInsert(specifications);
    }
}
