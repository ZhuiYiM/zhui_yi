// service/impl/ProductServiceImpl.java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.Category;
import com.example.demo.entity.ProductFavorite;
import com.example.demo.entity.dto.ProductDTO;
import com.example.demo.entity.ProductSpecification;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.CategoryMapper;
import com.example.demo.mapper.ProductFavoriteMapper;
import com.example.demo.service.ProductService;
import com.example.demo.service.ProductSpecificationService;
import com.example.demo.common.Result;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductFavoriteMapper productFavoriteMapper;

    @Autowired
    private ProductSpecificationService productSpecificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result createProduct(ProductDTO productDTO, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }
    
        // 使用更宽松的验证方式，只要 Token 有效即可
        if (!jwtUtil.validateTokenWithIdOrUsername(token)) {
            return Result.error("Token 无效");
        }
        
        // 从 Token 中获取用户 ID
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("用户未登录");
        }
    
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
    
        Product product = new Product();
        product.setSellerId(user.getId());
        product.setTitle(productDTO.getTitle());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setOriginalPrice(productDTO.getOriginalPrice());
        product.setCategoryId(productDTO.getCategoryId());
        product.setImages(productDTO.getImages());
        product.setTradeMethods(productDTO.getTradeMethods());
        product.setContactInfo(productDTO.getContactInfo());
        product.setLocation(productDTO.getLocation() != null ? productDTO.getLocation() : "校内");
        product.setStatus(1); // 上架状态
        product.setViewCount(0);
        product.setLikeCount(0);
        product.setFavoriteCount(0);
        product.setIsHot(0);
        product.setIsRecommend(0);
        // 判断是否有规格
        if (productDTO.getSpecifications() != null && !productDTO.getSpecifications().isEmpty()) {
            product.setHasSpecifications(1);
        } else {
            product.setHasSpecifications(0);
        }
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setDeletedAt(null);
    
        boolean result = this.save(product);
        if (result) {
            // 保存规格信息
            if (productDTO.getSpecifications() != null && !productDTO.getSpecifications().isEmpty()) {
                List<ProductSpecification> specifications = new java.util.ArrayList<>();
                for (ProductDTO.SpecificationDTO specDTO : productDTO.getSpecifications()) {
                    ProductSpecification spec = new ProductSpecification();
                    spec.setSpecName(specDTO.getSpecName());
                    spec.setSpecValue(specDTO.getSpecValue());
                    spec.setSpecIcon(specDTO.getSpecIcon());
                    spec.setStockQuantity(specDTO.getStockQuantity());
                    spec.setPriceAdjustment(specDTO.getPriceAdjustment());
                    spec.setIsDefault(specDTO.getIsDefault());
                    spec.setSortOrder(specDTO.getSortOrder());
                    specifications.add(spec);
                }
                productSpecificationService.batchSaveForProduct(product.getId(), specifications);
            }
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
        Integer categoryId = params.get("categoryId") != null ? Integer.parseInt(params.get("categoryId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : 1; // 默认只显示上架商品
        String level1Tag = params.get("level1Tag") != null ? params.get("level1Tag").toString() : null; // 身份标签筛选
        Integer sellerId = params.get("sellerId") != null ? Integer.parseInt(params.get("sellerId").toString()) : null; // 卖家 ID 筛选
        String sort = params.get("sort") != null ? params.get("sort").toString() : "newest";

        Page<Product> productPage = new Page<>(page, size);
        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("description", keyword));
        }

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        
        // 卖家筛选
        if (sellerId != null) {
            wrapper.eq("seller_id", sellerId);
        }

        // 状态筛选（不筛选已删除的）
        wrapper.eq("status", status);
        wrapper.isNull("deleted_at");

        // 排序
        switch (sort) {
            case "newest":
                wrapper.orderByDesc("created_at");
                break;
            case "hotest":
                wrapper.orderByDesc("view_count", "like_count");
                break;
            case "price-asc":
                wrapper.orderByAsc("price");
                break;
            case "price-desc":
                wrapper.orderByDesc("price");
                break;
            default:
                wrapper.orderByDesc("created_at");
        }

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

        // 构建返回数据
        Map<String, Object> data = new java.util.HashMap<>();
        
        // 商品基本信息
        data.put("product", product);
        
        // 添加卖家信息
        try {
            User seller = userMapper.selectById(product.getSellerId());
            if (seller != null) {
                Map<String, Object> sellerInfo = new java.util.HashMap<>();
                sellerInfo.put("id", seller.getId());
                sellerInfo.put("username", seller.getUsername());
                sellerInfo.put("realName", seller.getRealName());
                sellerInfo.put("avatarUrl", seller.getAvatarUrl());
                sellerInfo.put("bio", seller.getBio());
                sellerInfo.put("college", seller.getCollege());
                
                // 确定卖家身份标签
                String level1Tag = determineSellerLevel1Tag(seller);
                sellerInfo.put("level1Tag", level1Tag);
                
                // 获取卖家发布的商品数量
                int sellerProductCount = countProductsBySellerId(seller.getId());
                sellerInfo.put("productCount", sellerProductCount);
                
                data.put("seller", sellerInfo);
            }
        } catch (Exception e) {
            System.err.println("获取卖家信息失败：" + e.getMessage());
        }
        
        // 添加分类信息
        try {
            if (product.getCategoryId() != null) {
                Category category = categoryMapper.selectById(product.getCategoryId());
                if (category != null) {
                    data.put("categoryName", category.getName());
                }
            }
        } catch (Exception e) {
            System.err.println("获取分类信息失败：" + e.getMessage());
        }
        
        // 收藏状态
        if (userId != null) {
            QueryWrapper<ProductFavorite> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("product_id", productId);
            ProductFavorite favorite = productFavoriteMapper.selectOne(wrapper);
            data.put("isFavorite", favorite != null);
        } else {
            data.put("isFavorite", false);
        }
        
        // 添加规格信息
        try {
            List<ProductSpecification> specifications = productSpecificationService.getByProductId(productId);
            if (specifications != null && !specifications.isEmpty()) {
                // 按规格名称分组
                java.util.Map<String, java.util.List<ProductSpecification>> groupedSpecs = new java.util.HashMap<>();
                for (ProductSpecification spec : specifications) {
                    String specName = spec.getSpecName();
                    if (!groupedSpecs.containsKey(specName)) {
                        groupedSpecs.put(specName, new java.util.ArrayList<>());
                    }
                    groupedSpecs.get(specName).add(spec);
                }
                data.put("specifications", groupedSpecs);
            }
        } catch (Exception e) {
            System.err.println("获取规格信息失败：" + e.getMessage());
        }

        return Result.success(data);
    }
    
    /**
     * 确定卖家的身份标签
     */
    private String determineSellerLevel1Tag(User seller) {
        if (seller == null) {
            return "society";
        }
        
        // 1. 管理员优先级最高
        if ("admin".equals(seller.getRole()) || 
            (seller.getIsAdmin() != null && seller.getIsAdmin() == 1)) {
            return "admin";
        }
        
        // 2. 学生身份
        if (seller.getStudentId() != null && !seller.getStudentId().trim().isEmpty()) {
            return "student";
        }
        
        // 3. 商户身份
        if (seller.getIsMerchant() != null && seller.getIsMerchant() == 1) {
            return "merchant";
        }
        
        // 4. 团体身份
        if (seller.getIsOrganization() != null && seller.getIsOrganization() == 1) {
            return "organization";
        }
        
        // 5. 实名认证
        if (seller.getIsRealNameVerified() != null && seller.getIsRealNameVerified() == 1) {
            return "society";
        }
        
        return "society";
    }
    
    /**
     * 统计卖家的商品数量
     */
    private int countProductsBySellerId(Integer sellerId) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", sellerId)
               .eq("status", 1)  // 只统计上架商品
               .isNull("deleted_at");
        return Math.toIntExact(this.count(wrapper));
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
            return Result.error("Token 无效");
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
        product.setOriginalPrice(productDTO.getOriginalPrice());
        product.setCategoryId(productDTO.getCategoryId());
        product.setImages(productDTO.getImages());
        product.setTradeMethods(productDTO.getTradeMethods());
        product.setContactInfo(productDTO.getContactInfo());
        product.setLocation(productDTO.getLocation());
        product.setUpdatedAt(LocalDateTime.now());
    
        boolean result = this.updateById(product);
        if (result) {
            return Result.success("商品信息更新成功");
        } else {
            return Result.error("商品信息更新失败");
        }
    }

    @Override
    public Result deleteProduct(Integer productId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token 无效");
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
    public Result getCategories() {
        QueryWrapper<Category> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("parent_id", "name");
        java.util.List<Category> categories = categoryMapper.selectList(wrapper);
        return Result.success(categories);
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

    @Override
    public Result toggleFavorite(Integer productId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token 无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        Product product = this.getById(productId);
        if (product == null) {
            return Result.error("商品不存在");
        }

        // 检查是否已收藏
        QueryWrapper<ProductFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", user.getId()).eq("product_id", productId);
        ProductFavorite favorite = productFavoriteMapper.selectOne(wrapper);

        if (favorite != null) {
            // 取消收藏
            productFavoriteMapper.deleteById(favorite.getId());
            // 减少商品的收藏数
            product.setFavoriteCount(Math.max(0, product.getFavoriteCount() - 1));
            this.updateById(product);
            return Result.success("取消收藏成功");
        } else {
            // 添加收藏
            ProductFavorite newFavorite = new ProductFavorite();
            newFavorite.setUserId(user.getId());
            newFavorite.setProductId(productId);
            newFavorite.setCreatedAt(LocalDateTime.now());
            newFavorite.setUpdatedAt(LocalDateTime.now());
            productFavoriteMapper.insert(newFavorite);
            // 增加商品的收藏数
            product.setFavoriteCount(product.getFavoriteCount() + 1);
            this.updateById(product);
            return Result.success("收藏成功");
        }
    }

    @Override
    public Result getMyFavorites(HttpServletRequest request, Map<String, Object> params) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String username = jwtUtil.getUsernameFromToken(token);
        if (username == null || !jwtUtil.validateToken(token, username)) {
            return Result.error("Token 无效");
        }

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        if (user == null) {
            return Result.error("用户不存在");
        }

        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;

        // 查询用户的收藏记录
        QueryWrapper<ProductFavorite> favWrapper = new QueryWrapper<>();
        favWrapper.eq("user_id", user.getId()).orderByDesc("created_at");
        
        Page<ProductFavorite> favoritePage = new Page<>(page, size);
        Page<ProductFavorite> favoriteResult = productFavoriteMapper.selectPage(favoritePage, favWrapper);

        // 获取收藏的商品列表
        java.util.List<Integer> productIds = new java.util.ArrayList<>();
        for (ProductFavorite fav : favoriteResult.getRecords()) {
            productIds.add(fav.getProductId());
        }

        java.util.List<Product> products = new java.util.ArrayList<>();
        if (!productIds.isEmpty()) {
            products = this.listByIds(productIds);
        }

        return Result.success(products);
    }

    @Override
    public Result getMyPublishedProducts(HttpServletRequest request, Map<String, Object> params) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 从 Token 中获取用户 ID
        Integer userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return Result.error("用户未登录");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        int page = params.get("page") != null ? Integer.parseInt(params.get("page").toString()) : 1;
        int size = params.get("size") != null ? Integer.parseInt(params.get("size").toString()) : 10;
        Integer status = params.get("status") != null ? Integer.parseInt(params.get("status").toString()) : null;

        // 查询当前用户发布的商品
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.eq("seller_id", userId);
        wrapper.isNull("deleted_at"); // 只查询未删除的
        
        // 状态筛选（可选）
        if (status != null) {
            wrapper.eq("status", status);
        }
        
        // 排序：按创建时间倒序
        wrapper.orderByDesc("created_at");

        Page<Product> productPage = new Page<>(page, size);
        Page<Product> result = this.page(productPage, wrapper);
        
        return Result.success(result);
    }
}
