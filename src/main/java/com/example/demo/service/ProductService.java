// service/ProductService.java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Product;
import com.example.demo.entity.dto.ProductDTO;
import com.example.demo.common.Result;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public interface ProductService extends IService<Product> {
    Result createProduct(ProductDTO productDTO, HttpServletRequest request);
    Result getProducts(Map<String, Object> params);
    Result getProductById(Integer productId, Integer userId);
    Result updateProductStatus(Integer productId, Integer status, HttpServletRequest request);
    Result updateProduct(Integer productId, ProductDTO productDTO, HttpServletRequest request);
    Result deleteProduct(Integer productId, HttpServletRequest request);
    Result searchProducts(String keyword, Integer page, Integer size);
    Result getCategories();
    
    // 收藏相关功能
    Result toggleFavorite(Integer productId, HttpServletRequest request);
    Result getMyFavorites(HttpServletRequest request, Map<String, Object> params);
}
