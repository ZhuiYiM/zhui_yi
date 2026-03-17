package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.OrderReview;

import java.util.List;
import java.util.Map;

/**
 * 订单评价服务
 */
public interface OrderReviewService extends IService<OrderReview> {
    
    /**
     * 创建评价
     */
    void createReview(Integer orderId, Integer userId, Integer rating, String content, String images);
    
    /**
     * 获取商品评价列表
     */
    List<Map<String, Object>> getProductReviews(Integer productId, Integer page, Integer size);
    
    /**
     * 获取评价统计
     */
    Map<String, Object> getReviewStats(Integer productId);
    
    /**
     * 卖家回复评价
     */
    void replyReview(Integer reviewId, Integer userId, String reply);
}
