// service/impl/ProductServiceImpl.java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.ProductDTO;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.ProductService;
import com.example.demo.common.Result;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;  // 添加这一行
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result createProduct(ProductDTO productDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        Product product = new Product();
        product.setSellerId(user.getId());
        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategoryId(productDTO.getCategoryId());
        product.setImages(productDTO.getImages() != null ? productDTO.getImages().toString() : null);
        product.setStatus(1); // 上架状态
        product.setViewCount(0);
        product.setLikeCount(0);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        boolean result = this.save(product);
        if (result) {
            return Result.success("商品发布成功");
        } else {
            return Result.error("商品发布失败");
        }
    }

    @Override
    public Result getProducts(Map<String, Object> params) {
        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        String keyword = params.get("keyword") != null ? params.get("keyword").toString() : "";
        Integer categoryId = params.get("category_id") != null ? Integer.parseInt(params.get("category_id").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : 1; // 默认只显示上架商品

        Page<Product> productPage = new Page<>(page, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        }

        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }

        wrapper.eq("status", status)
                .orderByDesc("created_at");

        Page<Product> result = this.page(productPage, wrapper);
        return Result.success(result);
    }

    @Override
    public Result getProductById(Integer productId, Integer userId) {
        Product product = this.getById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 增加浏览量
        product.setViewCount(product.getViewCount() + 1);
        this.updateById(product);

        // 如果用户已登录，可以添加其他个性化信息
        // 例如：检查用户是否收藏了此商品等

        return Result.success(product);
    }

    @Override
    public Result updateProductStatus(Integer productId, Integer status, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        Product product = this.getById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 验证是否是该商品的卖家
        if (!product.getSellerId().equals(user.getId())) {
            return Result.error("无权修改此商品状态");
        }

        product.setStatus(status);
        product.setUpdatedAt(LocalDateTime.now());

        boolean result = this.updateById(product);
        if (result) {
            return Result.success("商品状态更新成功");
        } else {
            return Result.error("商品状态更新失败");
        }
    }

    @Override
    public Result updateProduct(Integer productId, ProductDTO productDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        Product product = this.getById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 验证是否是该商品的卖家
        if (!product.getSellerId().equals(user.getId())) {
            return Result.error("无权修改此商品信息");
        }

        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setCategoryId(productDTO.getCategoryId());
        product.setImages(productDTO.getImages() != null ? productDTO.getImages().toString() : null);
        product.setUpdatedAt(LocalDateTime.now());

        boolean result = this.updateById(product);
        if (result) {
            return Result.success("商品信息更新成功");
        } else {
            return Result.error("商品信息更新失败");
        }
    }

    @Override
    public Result deleteProduct(Integer productId, Integer userId) {
        Product product = this.getById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 验证是否是该商品的卖家
        if (!product.getSellerId().equals(userId)) {
            return Result.error("无权删除此商品");
        }

        boolean result = this.removeById(productId);
        if (result) {
            return Result.success("商品删除成功");
        } else {
            return Result.error("商品删除失败");
        }
    }

    @Override
    public Result searchProducts(String keyword, Integer page, Integer size) {
        Page<Product> productPage = new Page<>(page, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        }

        wrapper.eq("status", 1) // 只搜索上架商品
                .orderByDesc("created_at");

        Page<Product> result = this.page(productPage, wrapper);
        return Result.success(result);
    }
}
