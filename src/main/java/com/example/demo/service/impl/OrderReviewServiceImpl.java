package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderReview;
import com.example.demo.entity.Product;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.OrderReviewMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.OrderReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单评价服务实现
 */
@Service
public class OrderReviewServiceImpl extends ServiceImpl<OrderReviewMapper, OrderReview> implements OrderReviewService {
    
    @Autowired
    private OrderReviewMapper orderReviewMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReview(Integer orderId, Integer userId, Integer rating, String content, String images) {
        // 验证订单是否存在且属于该用户
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权评价此订单");
        }
        
        // 验证订单状态（只有已完成的订单可以评价）
        if (order.getOrderStatus() != 3) {
            throw new RuntimeException("该订单状态无法评价");
        }
        
        // 检查是否已经评价过
        QueryWrapper<OrderReview> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        Long count = this.count(wrapper);
        if (count > 0) {
            throw new RuntimeException("该订单已评价");
        }
        
        // 创建评价
        OrderReview review = new OrderReview();
        review.setOrderId(orderId);
        review.setProductId(order.getProductId());
        review.setBuyerId(userId);
        review.setSellerId(order.getSellerId());
        review.setRating(rating);
        review.setContent(content);
        review.setImages(images);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        this.save(review);
    }
    
    @Override
    public List<Map<String, Object>> getProductReviews(Integer productId, Integer page, Integer size) {
        Page<OrderReview> reviewPage = new Page<>(page, size);
        QueryWrapper<OrderReview> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        wrapper.orderByDesc("created_at");
        
        Page<OrderReview> result = this.page(reviewPage, wrapper);
        
        List<Map<String, Object>> reviews = new ArrayList<>();
        for (OrderReview review : result.getRecords()) {
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("id", review.getId());
            reviewData.put("rating", review.getRating());
            reviewData.put("content", review.getContent());
            reviewData.put("images", review.getImages());
            reviewData.put("reply", review.getReply());
            reviewData.put("replyTime", review.getReplyTime());
            reviewData.put("createdAt", review.getCreatedAt());
            
            // 获取买家信息（可选）
            reviewData.put("buyerId", review.getBuyerId());
            
            reviews.add(reviewData);
        }
        
        return reviews;
    }
    
    @Override
    public Map<String, Object> getReviewStats(Integer productId) {
        QueryWrapper<OrderReview> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id", productId);
        
        List<OrderReview> allReviews = this.list(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", allReviews.size());
        
        if (allReviews.isEmpty()) {
            stats.put("averageRating", 0.0);
            stats.put("ratingDistribution", new HashMap<>());
        } else {
            // 计算平均分
            double totalRating = 0;
            Map<Integer, Integer> ratingDistribution = new HashMap<>();
            
            for (OrderReview review : allReviews) {
                totalRating += review.getRating();
                ratingDistribution.put(review.getRating(), 
                    ratingDistribution.getOrDefault(review.getRating(), 0) + 1);
            }
            
            stats.put("averageRating", String.format("%.1f", totalRating / allReviews.size()));
            stats.put("ratingDistribution", ratingDistribution);
        }
        
        return stats;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(Integer reviewId, Integer userId, String reply) {
        OrderReview review = this.getById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        
        // 验证是否是卖家回复
        if (!review.getSellerId().equals(userId)) {
            throw new RuntimeException("无权回复此评价");
        }
        
        review.setReply(reply);
        review.setReplyTime(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        
        this.updateById(review);
    }
    
    @Override
    public List<Map<String, Object>> getSellerReviews(Integer sellerId, Integer page, Integer size) {
        Page<OrderReview> reviewPage = new Page<>(page, size);
        QueryWrapper<OrderReview> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", sellerId);
        wrapper.orderByDesc("created_at");
        
        Page<OrderReview> result = this.page(reviewPage, wrapper);
        
        List<Map<String, Object>> reviews = new ArrayList<>();
        for (OrderReview review : result.getRecords()) {
            Map<String, Object> reviewData = new HashMap<>();
            reviewData.put("id", review.getId());
            reviewData.put("rating", review.getRating());
            reviewData.put("content", review.getContent());
            reviewData.put("images", review.getImages());
            reviewData.put("reply", review.getReply());
            reviewData.put("replyTime", review.getReplyTime());
            reviewData.put("createdAt", review.getCreatedAt());
            
            // 获取商品图片
            Product product = productMapper.selectById(review.getProductId());
            if (product != null) {
                Map<String, Object> productInfo = new HashMap<>();
                productInfo.put("id", product.getId());
                productInfo.put("title", product.getTitle());
                List<String> images = product.getImages();
                if (images != null && !images.isEmpty()) {
                    productInfo.put("image", images.get(0));
                } else {
                    productInfo.put("image", "https://placehold.co/100x100/e0e0e0/999999?text=商品图片");
                }
                reviewData.put("product", productInfo);
            }
            
            // 获取买家信息
            reviewData.put("buyerId", review.getBuyerId());
            
            reviews.add(reviewData);
        }
        
        return reviews;
    }
    
    @Override
    public List<Map<String, Object>> getReviewableOrders(Integer userId) {
        // 查询用户已完成的订单（order_status = 3）且未评价的订单
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("buyer_id", userId);
        orderWrapper.eq("order_status", 3); // 已完成
        
        List<Order> orders = orderMapper.selectList(orderWrapper);
        
        List<Map<String, Object>> reviewableOrders = new ArrayList<>();
        
        for (Order order : orders) {
            // 检查是否已经评价过
            QueryWrapper<OrderReview> reviewWrapper = new QueryWrapper<>();
            reviewWrapper.eq("order_id", order.getId());
            Long reviewCount = this.count(reviewWrapper);
            
            if (reviewCount == 0) {
                // 未评价，添加到可评价列表
                Map<String, Object> orderData = new HashMap<>();
                orderData.put("orderId", order.getId());
                orderData.put("productId", order.getProductId());
                orderData.put("totalAmount", order.getTotalAmount());
                orderData.put("createdAt", order.getCreatedAt());
                
                // 获取商品信息
                Product product = productMapper.selectById(order.getProductId());
                if (product != null) {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("id", product.getId());
                    productInfo.put("title", product.getTitle());
                    List<String> images = product.getImages();
                    if (images != null && !images.isEmpty()) {
                        productInfo.put("image", images.get(0));
                    } else {
                        productInfo.put("image", "https://placehold.co/100x100/e0e0e0/999999?text=商品图片");
                    }
                    orderData.put("product", productInfo);
                }
                
                reviewableOrders.add(orderData);
            }
        }
        
        return reviewableOrders;
    }
}
