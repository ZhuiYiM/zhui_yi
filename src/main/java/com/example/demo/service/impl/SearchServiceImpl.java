// service/impl/SearchServiceImpl.java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.entity.Product;
import com.example.demo.entity.Topic;
import com.example.demo.entity.User;
import com.example.demo.mapper.ProductMapper;
import com.example.demo.mapper.TopicMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.SearchService;
import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public Result searchAll(String keyword, Integer page, Integer size) {
        Map<String, Object> result = new HashMap<>();

        // 搜索商品
        result.put("products", searchProducts(keyword, page, size));

        // 搜索话题
        result.put("topics", searchTopics(keyword, page, size));

        // 搜索用户
        result.put("users", searchUsers(keyword, page, size));

        return Result.success(result);
    }

    @Override
    public Result searchProducts(String keyword, Integer page, Integer size) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        wrapper.like("title", keyword)
                .or()
                .like("description", keyword);

        List<Product> products = productMapper.selectList(wrapper);
        return Result.success(products);
    }

    @Override
    public Result searchTopics(String keyword, Integer page, Integer size) {
        QueryWrapper<Topic> wrapper = new QueryWrapper<>();
        wrapper.like("title", keyword)
                .or()
                .like("content", keyword);

        List<Topic> topics = topicMapper.selectList(wrapper);
        return Result.success(topics);
    }

    @Override
    public Result searchUsers(String keyword, Integer page, Integer size) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.like("username", keyword)
                .or()
                .like("real_name", keyword);

        List<User> users = userMapper.selectList(wrapper);
        return Result.success(users);
    }
}
