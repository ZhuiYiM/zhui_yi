// controller/ProductController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.ProductDTO;
import com.example.demo.service.ProductService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    // 发布商品
    @PostMapping
    public Result publishProduct(@RequestBody ProductDTO productDTO, HttpServletRequest request) {
        return productService.createProduct(productDTO, request);
    }

    // 获取商品列表
    @GetMapping
    public Result getProducts(@RequestParam Map<String, Object> params) {
        return productService.getProducts(params);
    }

    // 获取商品详情
    @GetMapping("/{id}")
    public Result getProductDetail(@PathVariable Integer id, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = null;

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);

            // 安全地提取用户ID
            if (!token.trim().isEmpty()) {
                userId = jwtUtil.getUserIdFromToken(token);
            }
        }

        return productService.getProductById(id, userId);
    }

    // 更新商品状态
    @PutMapping("/{id}/status")
    public Result updateProductStatus(@PathVariable Integer id,
                                      @RequestParam Integer status,
                                      HttpServletRequest request) {
        return productService.updateProductStatus(id, status, request);
    }

    // 从Token中提取用户ID的辅助方法
    private Integer extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 这里应该根据实际实现来获取用户ID
        // 暂时返回null表示未登录用户
        return null;
    }
}
