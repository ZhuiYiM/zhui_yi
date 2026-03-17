package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.OrderDTO;
import com.example.demo.service.OrderService;
import com.example.demo.service.OrderCancelReasonService;
import com.example.demo.mapper.UserMapper;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderCancelReasonService orderCancelReasonService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 创建订单
     */
    @PostMapping
    public Result createOrder(@RequestBody OrderDTO orderDTO, HttpServletRequest request) {
        try {
            Integer buyerId = getCurrentUserId(request);
            if (buyerId == null) {
                return Result.error("请先登录");
            }
            
            // 设置默认订单状态为待付款
            if (orderDTO.getOrderStatus() == null) {
                orderDTO.setOrderStatus(0);
            }
            
            Order order = orderService.createOrder(orderDTO, buyerId);
            Map<String, Object> result = new HashMap<>();
            result.put("id", order.getId());
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我的订单列表
     */
    @GetMapping("/my")
    public Result getMyOrders(HttpServletRequest request,
                             @RequestParam(required = false, defaultValue = "1") Integer page,
                             @RequestParam(required = false, defaultValue = "10") Integer size,
                             @RequestParam(required = false) Integer status) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("size", size);
            if (status != null) {
                params.put("status", status);
            }
            
            Map<String, Object> result = orderService.getMyOrders(userId, params);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取订单详情
     */
    @GetMapping("/{id}")
    public Result getOrderDetail(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            Map<String, Object> result = orderService.getOrderDetail(id, userId);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    public Result updateOrderStatus(@PathVariable Integer id,
                                   @RequestParam Integer status,
                                   HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            boolean result = orderService.updateOrderStatus(id, status);
            if (result) {
                return Result.success("订单状态更新成功");
            } else {
                return Result.error("订单状态更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消订单
     */
    @PutMapping("/{id}/cancel")
    public Result cancelOrder(@PathVariable Integer id, 
                             @RequestBody(required = false) Map<String, String> requestBody,
                             HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            String reason = requestBody != null ? requestBody.get("reason") : null;
            String note = requestBody != null ? requestBody.get("note") : null;
            
            boolean result = orderService.cancelOrder(id, userId, reason, note);
            if (result) {
                return Result.success("订单取消成功");
            } else {
                return Result.error("订单取消失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除订单
     */
    @DeleteMapping("/{id}")
    public Result deleteOrder(@PathVariable Integer id, HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            boolean result = orderService.removeById(id);
            if (result) {
                return Result.success("订单删除成功");
            } else {
                return Result.error("订单删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取取消原因统计
     */
    @GetMapping("/cancel-stats")
    public Result getCancelReasonStats(HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            List<Map<String, Object>> stats = orderCancelReasonService.getCancelReasonStats(userId);
            return Result.success(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 支付订单
     */
    @PutMapping("/{id}/pay")
    public Result payOrder(@PathVariable Integer id,
                          @RequestBody Map<String, String> params,
                          HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            String paymentMethod = params.get("paymentMethod");
            if (paymentMethod == null || paymentMethod.isEmpty()) {
                return Result.error("请选择支付方式");
            }
            
            boolean result = orderService.payOrder(id, userId, paymentMethod);
            if (result) {
                return Result.success("支付成功");
            } else {
                return Result.error("支付失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取卖家订单列表
     */
    @GetMapping("/seller/my")
    public Result getSellerOrders(HttpServletRequest request,
                                 @RequestParam(required = false, defaultValue = "1") Integer page,
                                 @RequestParam(required = false, defaultValue = "10") Integer size,
                                 @RequestParam(required = false) Integer status) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("size", size);
            if (status != null) {
                params.put("status", status);
            }
            
            Map<String, Object> result = orderService.getSellerOrders(userId, params);
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 卖家发货
     */
    @PutMapping("/{id}/ship")
    public Result shipOrder(@PathVariable Integer id,
                           @RequestBody(required = false) Map<String, String> params,
                           HttpServletRequest request) {
        try {
            Integer userId = getCurrentUserId(request);
            if (userId == null) {
                return Result.error("请先登录");
            }
            
            String trackingNumber = params != null ? params.get("trackingNumber") : null;
            String courier = params != null ? params.get("courier") : null;
            
            boolean result = orderService.shipOrder(id, userId, trackingNumber, courier);
            if (result) {
                return Result.success("发货成功");
            } else {
                return Result.error("发货失败");
            }
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
        
        // 从数据库中查询用户
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return null;
        }
        
        return user.getId();
    }
}
