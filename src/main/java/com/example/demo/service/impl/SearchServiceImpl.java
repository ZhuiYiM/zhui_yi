package com.example.demo.service.impl;

import com.example.demo.common.Result;
import com.example.demo.entity.Location;
import com.example.demo.entity.Product;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.mapper.LocationMapper;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 */
@Service
public class SearchServiceImpl implements SearchService {
    
    @Autowired
    private LocationMapper locationMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public Result searchAll(String query, String type, String tags, String quickFilters) {
        Map<String, Object> results = new HashMap<>();
        int total = 0;
        
        // 根据类型搜索
        if ("all".equals(type) || "location".equals(type)) {
            List<Location> locations = searchLocationsInternal(query, tags);
            results.put("locations", locations);
            total += locations.size();
        }
        
        if ("all".equals(type) || "product".equals(type)) {
            List<Product> products = searchProductsInternal(query, tags);
            results.put("products", products);
            total += products.size();
        }
        
        if ("all".equals(type) || "topic".equals(type)) {
            List<Topic> topics = searchTopicsInternal(query, tags);
            results.put("topics", topics);
            total += topics.size();
        }
        
        if ("all".equals(type) || "topic_info".equals(type)) {
            // TODO: 话题信息搜索（如果有独立的话题信息表）
            results.put("topicInfos", new ArrayList<>());
        }
        
        if ("all".equals(type) || "user".equals(type)) {
            List<User> users = searchUsersInternal(query);
            results.put("users", users);
            total += users.size();
        }
        
        results.put("total", total);
        return Result.success(results);
    }
    
    @Override
    public Result searchLocations(String query, Integer campusId, String tags) {
        List<Location> locations = searchLocationsInternal(query, tags);
        
        // 如果指定了校区 ID，进一步筛选
        if (campusId != null) {
            locations = locations.stream()
                .filter(loc -> campusId.equals(loc.getCampusId()))
                .collect(Collectors.toList());
        }
        
        return Result.success(locations);
    }
    
    @Override
    public Result searchProducts(String query, String tags) {
        List<Product> products = searchProductsInternal(query, tags);
        return Result.success(products);
    }
    
    @Override
    public Result searchTopics(String query, String tags) {
        List<Topic> topics = searchTopicsInternal(query, tags);
        return Result.success(topics);
    }
    
    @Override
    public Result searchUsers(String query) {
        List<User> users = searchUsersInternal(query);
        return Result.success(users);
    }
    
    // ==================== 内部搜索方法 ====================
    
    /**
     * 搜索地点（内部方法）
     */
    private List<Location> searchLocationsInternal(String query, String tags) {
        // 如果没有关键词且没有标签，返回空列表
        if ((query == null || query.trim().isEmpty()) && (tags == null || tags.isEmpty())) {
            return new ArrayList<>();
        }
        
        List<Location> locations;
        
        // 如果有关键词，进行模糊搜索
        if (query != null && !query.trim().isEmpty()) {
            String keyword = "%" + query.trim() + "%";
            locations = locationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Location>()
                    .like("name", keyword)
                    .or()
                    .like("description", keyword)
            );
        } else {
            // 没有关键词时，查询所有
            locations = locationMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Location>()
            );
        }
        
        // 如果有标签筛选，进一步处理
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            locations = locations.stream()
                .filter(loc -> {
                    // 检查地点的 category 是否匹配标签
                    if (loc.getCategory() != null) {
                        for (String tag : tagArray) {
                            if (loc.getCategory().equals(tag)) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        return locations;
    }
    
    /**
     * 搜索商品（内部方法）
     */
    private List<Product> searchProductsInternal(String query, String tags) {
        // 如果没有关键词且没有标签，返回空列表
        if ((query == null || query.trim().isEmpty()) && (tags == null || tags.isEmpty())) {
            return new ArrayList<>();
        }
        
        List<Product> products;
        
        // 如果有关键词，进行模糊搜索
        if (query != null && !query.trim().isEmpty()) {
            String keyword = "%" + query.trim() + "%";
            products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product>()
                    .like("title", keyword)
                    .or()
                    .like("description", keyword)
            );
        } else {
            // 没有关键词时，查询所有
            products = productMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Product>()
            );
        }
        
        // 标签筛选（按分类 ID）
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            // 这里假设标签就是分类 ID，直接匹配
            products = products.stream()
                .filter(p -> {
                    if (p.getCategoryId() != null) {
                        String categoryIdStr = String.valueOf(p.getCategoryId());
                        for (String tag : tagArray) {
                            if (categoryIdStr.equals(tag)) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        return products;
    }
    
    /**
     * 搜索话题（内部方法）
     */
    private List<Topic> searchTopicsInternal(String query, String tags) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String keyword = "%" + query.trim() + "%";
        List<Topic> topics = topicMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Topic>()
                .like("title", keyword)
                .or()
                .like("content", keyword)
                .or()
                .like("tags", keyword)
        );
        
        // 标签筛选
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            topics = topics.stream()
                .filter(topic -> {
                    if (topic.getTags() != null) {
                        for (String tag : tagArray) {
                            if (topic.getTags().contains(tag)) {
                                return true;
                            }
                        }
                    }
                    return false;
                })
                .collect(Collectors.toList());
        }
        
        return topics;
    }
    
    /**
     * 搜索用户（内部方法）
     */
    private List<User> searchUsersInternal(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        String keyword = "%" + query.trim() + "%";
        return userMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .like("username", keyword)
                .or()
                .like("student_id", keyword)
                .or()
                .like("bio", keyword)
        );
    }
}
