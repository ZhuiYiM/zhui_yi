package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.OrderCancelReason;
import com.example.demo.mapper.OrderCancelReasonMapper;
import com.example.demo.service.OrderCancelReasonService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单取消原因服务实现
 */
@Service
public class OrderCancelReasonServiceImpl extends ServiceImpl<OrderCancelReasonMapper, OrderCancelReason> implements OrderCancelReasonService {
    
    @Override
    public void recordCancelReason(Integer orderId, Integer buyerId, Integer sellerId, String reason, String note) {
        OrderCancelReason cancelReason = new OrderCancelReason();
        cancelReason.setOrderId(orderId);
        cancelReason.setBuyerId(buyerId);
        cancelReason.setSellerId(sellerId);
        cancelReason.setReason(reason);
        cancelReason.setNote(note);
        cancelReason.setCreatedAt(LocalDateTime.now());
        
        this.save(cancelReason);
    }
    
    @Override
    public List<Map<String, Object>> getCancelReasonStats(Integer userId) {
        QueryWrapper<OrderCancelReason> wrapper = new QueryWrapper<>();
        wrapper.eq("buyer_id", userId);
        wrapper.select("reason", "COUNT(*) as count");
        wrapper.groupBy("reason");
        
        List<Map<String, Object>> stats = new ArrayList<>();
        List<OrderCancelReason> reasons = this.list(wrapper);
        
        for (OrderCancelReason reason : reasons) {
            Map<String, Object> stat = new HashMap<>();
            stat.put("reason", reason.getReason());
            stat.put("count", 1); // 这里需要重新查询计数
            stats.add(stat);
        }
        
        // 重新统计每个原因的数量
        for (Map<String, Object> stat : stats) {
            QueryWrapper<OrderCancelReason> countWrapper = new QueryWrapper<>();
            countWrapper.eq("buyer_id", userId);
            countWrapper.eq("reason", stat.get("reason"));
            Long count = this.count(countWrapper);
            stat.put("count", count);
        }
        
        return stats;
    }
}
