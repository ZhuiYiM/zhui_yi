package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Order;
import com.example.demo.entity.Product;
import com.example.demo.entity.OrderSpecification;
import com.example.demo.entity.dto.OrderDTO;
import com.example.demo.mapper.OrderMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.service.OrderService;
import com.example.demo.service.OrderSpecificationService;
import com.example.demo.service.OrderCancelReasonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private OrderSpecificationService orderSpecificationService;
    
    @Autowired
    private OrderCancelReasonService orderCancelReasonService;
    
    /**
     * 构建订单响应数据（包含商品信息和规格）
     */
    private Map<String, Object> buildOrderWithProduct(Order order) {
        Map<String, Object> orderData = new HashMap<>();
        // 订单信息
        orderData.put("id", order.getId());
        orderData.put("buyerId", order.getBuyerId());
        orderData.put("sellerId", order.getSellerId());
        orderData.put("productId", order.getProductId());
        orderData.put("orderStatus", order.getOrderStatus());
        orderData.put("totalAmount", order.getTotalAmount());
        orderData.put("buyerContact", order.getBuyerContact());
        orderData.put("buyerMessage", order.getBuyerMessage());
        orderData.put("paymentMethod", order.getPaymentMethod());
        orderData.put("paymentStatus", order.getPaymentStatus());
        orderData.put("paymentTime", order.getPaymentTime());
        orderData.put("createdAt", order.getCreatedAt());
        orderData.put("updatedAt", order.getUpdatedAt());
        
        // 商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            orderData.put("productTitle", product.getTitle());
            // 处理商品图片 - getImages() 返回的是 List<String>
            String productImage = "https://placehold.co/100x100/e0e0e0/999999?text=商品";
            if (product.getImages() != null && !product.getImages().isEmpty()) {
                productImage = product.getImages().get(0);
            }
            orderData.put("productImage", productImage);
        }
        
        // 获取订单规格信息
        List<OrderSpecification> specifications = orderSpecificationService.getByOrderId(order.getId());
        if (specifications != null && !specifications.isEmpty()) {
            orderData.put("specifications", specifications);
        }
        
        return orderData;
    }
    
    /**
     * 构建分页响应数据
     */
    private Map<String, Object> buildPageResponse(Page<Order> result, List<Map<String, Object>> records) {
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("total", result.getTotal());
        response.put("pages", result.getPages());
        response.put("current", result.getCurrent());
        response.put("size", result.getSize());
        return response;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(OrderDTO orderDTO, Integer buyerId) {
        // 1. 验证商品是否存在
        Product product = productMapper.selectById(orderDTO.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        
        // 2. 验证商品状态
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架或已售出");
        }
        
        // 3. 计算总金额（考虑规格价格调整）
        BigDecimal totalAmount = orderDTO.getTotalAmount();
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            // 如果没有传入总金额，根据商品价格和数量计算
            totalAmount = product.getPrice().multiply(new BigDecimal(orderDTO.getQuantity()));
        }
        
        // 4. 创建订单
        Order order = new Order();
        order.setBuyerId(buyerId);
        order.setSellerId(product.getSellerId());
        order.setProductId(orderDTO.getProductId());
        order.setOrderStatus(orderDTO.getOrderStatus() != null ? orderDTO.getOrderStatus() : 0); // 默认待付款
        order.setTotalAmount(totalAmount);
        order.setBuyerContact(orderDTO.getBuyerContact());
        order.setBuyerMessage(orderDTO.getMessage());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPaymentStatus(0); // 未支付
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        boolean saved = this.save(order);
        if (!saved) {
            throw new RuntimeException("创建订单失败");
        }
        
        // 5. 保存订单规格信息
        if (orderDTO.getSelectedSpecifications() != null && !orderDTO.getSelectedSpecifications().isEmpty()) {
            List<OrderSpecification> orderSpecs = new java.util.ArrayList<>();
            for (OrderDTO.OrderSpecItemDTO specItem : orderDTO.getSelectedSpecifications()) {
                OrderSpecification orderSpec = new OrderSpecification();
                orderSpec.setSpecId(specItem.getSpecId());
                orderSpec.setSpecName(specItem.getSpecName());
                orderSpec.setSpecValue(specItem.getSpecValue());
                orderSpec.setSpecIcon(specItem.getSpecIcon());
                orderSpec.setQuantity(specItem.getQuantity());
                orderSpecs.add(orderSpec);
            }
            orderSpecificationService.batchSaveForOrder(order.getId(), orderSpecs);
        }
        
        return order;
    }
    
    @Override
    public Map<String, Object> getMyOrders(Integer userId, Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        
        Page<Order> orderPage = new Page<>(page, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        
        // 只查询用户作为买家的订单
        wrapper.eq("buyer_id", userId);
        
        // 状态筛选
        if (status != null) {
            wrapper.eq("order_status", status);
        }
        
        wrapper.orderByDesc("created_at");
        
        Page<Order> result = this.page(orderPage, wrapper);
        
        // 构建响应数据，包含商品信息
        List<Map<String, Object>> ordersWithProduct = new java.util.ArrayList<>();
        for (Order order : result.getRecords()) {
            ordersWithProduct.add(buildOrderWithProduct(order));
        }
        
        return buildPageResponse(result, ordersWithProduct);
    }
    
    @Override
    public Map<String, Object> getOrderDetail(Integer orderId, Integer userId) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证订单归属
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权查看此订单");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        
        // 获取商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            data.put("product", product);
        }
        
        // 获取订单规格信息
        List<OrderSpecification> specifications = orderSpecificationService.getByOrderId(orderId);
        if (specifications != null && !specifications.isEmpty()) {
            data.put("specifications", specifications);
        }
        
        return data;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateOrderStatus(Integer orderId, Integer status) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        order.setOrderStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        
        // 如果是付款完成，记录付款时间
        if (status == 1) { // 待发货表示已付款
            order.setPaymentStatus(1);
            order.setPaymentTime(LocalDateTime.now());
        }
        
        return this.updateById(order);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(Integer orderId, Integer userId, String reason, String note) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证订单归属
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权取消此订单");
        }
        
        // 只有待付款状态的订单可以取消
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("该订单状态无法取消");
        }
        
        order.setOrderStatus(4); // 已取消
        order.setUpdatedAt(LocalDateTime.now());
        
        boolean saved = this.updateById(order);
        
        // 记录取消原因
        if (reason != null && !reason.isEmpty()) {
            orderCancelReasonService.recordCancelReason(orderId, userId, order.getSellerId(), reason, note);
        }
        
        return saved;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean payOrder(Integer orderId, Integer userId, String paymentMethod) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证订单归属
        if (!order.getBuyerId().equals(userId)) {
            throw new RuntimeException("无权支付此订单");
        }
        
        // 只有待付款状态的订单可以支付
        if (order.getOrderStatus() != 0) {
            throw new RuntimeException("该订单状态无法支付");
        }
        
        order.setOrderStatus(1); // 待发货
        order.setPaymentStatus(1); // 已支付
        order.setPaymentMethod(paymentMethod);
        order.setPaymentTime(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        
        return this.updateById(order);
    }
    
    @Override
    public Map<String, Object> getSellerOrders(Integer userId, Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;
        
        Page<Order> orderPage = new Page<>(page, size);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        
        // 查询用户作为卖家的订单
        wrapper.eq("seller_id", userId);
        
        // 状态筛选
        if (status != null) {
            wrapper.eq("order_status", status);
        }
        
        wrapper.orderByDesc("created_at");
        
        Page<Order> result = this.page(orderPage, wrapper);
        
        // 构建响应数据，包含商品信息
        List<Map<String, Object>> ordersWithProduct = new java.util.ArrayList<>();
        for (Order order : result.getRecords()) {
            ordersWithProduct.add(buildOrderWithProduct(order));
        }
        
        return buildPageResponse(result, ordersWithProduct);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean shipOrder(Integer orderId, Integer userId, String trackingNumber, String courier) {
        Order order = this.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        
        // 验证订单归属（只有卖家可以发货）
        if (!order.getSellerId().equals(userId)) {
            throw new RuntimeException("无权发货此订单");
        }
        
        // 只有待发货状态的订单可以发货
        if (order.getOrderStatus() != 1) {
            throw new RuntimeException("该订单状态无法发货");
        }
        
        order.setOrderStatus(2); // 已发货
        order.setUpdatedAt(LocalDateTime.now());
        
        // TODO: 如果需要，可以创建 OrderShipping 表来存储物流信息
        // 目前暂时不存储物流信息
        
        return this.updateById(order);
    }
}
