package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderReview;
import com.example.demo.entity.User;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.OrderReviewService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单评价控制器
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class OrderReviewController {
    
    @Autowired
    private OrderReviewService orderReviewService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private OrderMapper orderMapper;
    
    /**
     * 创建评价
     */
    @PostMapping
    public Result createReview(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            Integer orderId = (Integer) params.get("orderId");
            Integer rating = (Integer) params.get("rating");
            String content = (String) params.get("content");
            String images = (String) params.get("images");
            
            if (orderId == null || rating == null) {
                return Result.error("参数错误");
            }
            
            if (rating < 1 || rating > 5) {
                return Result.error("评分必须在 1-5 之间");
            }
            
            orderReviewService.createReview(orderId, userId, rating, content, images);
            return Result.success("评价成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取商品评价列表
     */
    @GetMapping("/product/{productId}")
    public Result getProductReviews(@PathVariable Integer productId,
                                   @RequestParam(required = false, defaultValue = "1") Integer page,
                                   @RequestParam(required = false, defaultValue = "10") Integer size,
                                   HttpServletRequest request) {
        try {
            List<Map<String, Object>> reviews = orderReviewService.getProductReviews(productId, page, size);
            return Result.success(reviews);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取评价统计
     */
    @GetMapping("/stats/product/{productId}")
    public Result getReviewStats(@PathVariable Integer productId, HttpServletRequest request) {
        try {
            Map<String, Object> stats = orderReviewService.getReviewStats(productId);
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 卖家回复评价
     */
    @PutMapping("/{reviewId}/reply")
    public Result replyReview(@PathVariable Integer reviewId,
                             @RequestBody Map<String, String> params,
                             HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            String reply = params.get("reply");
            orderReviewService.replyReview(reviewId, userId, reply);
            return Result.success("回复成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户 ID
     */
    private Integer getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return null;
        }
        
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return null;
        }
        
        return user.getId();
    }
}
