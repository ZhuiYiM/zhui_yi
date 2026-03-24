package com.example.demo.service.topic.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.Topics;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.TopicQueryDTO;
import com.example.demo.mapper.TopicsMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.topic.TopicQueryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 话题查询服务实现类
 */
@Service
public class TopicQueryServiceImpl extends ServiceImpl<TopicsMapper, Topics> implements TopicQueryService {

    @Autowired
    private TopicsMapper topicsMapper;

    @Autowired
    private UserMapper userMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ApiResult getTopics(TopicQueryDTO queryDTO) {
        try {
            Page<Topics> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
            QueryWrapper<Topics> wrapper = new QueryWrapper<>();

            // 只查询正常状态的话题
            wrapper.eq("status", 1);

            // 一级标签筛选
            if (queryDTO.getLevel1Tag() != null && !queryDTO.getLevel1Tag().trim().isEmpty()) {
                wrapper.eq("level1_tag_code", queryDTO.getLevel1Tag());
            }

            // 二级标签筛选
            if (queryDTO.getLevel2Tags() != null && queryDTO.getLevel2Tags().length > 0) {
                wrapper.and(w -> {
                    for (String level2Code : queryDTO.getLevel2Tags()) {
                        w.or(inner -> inner.like("level2_tag_codes", "\"" + level2Code.trim() + "\""));
                    }
                });
            }

            // 三级标签筛选
            if (queryDTO.getLevel3Tags() != null && queryDTO.getLevel3Tags().length > 0) {
                wrapper.and(w -> {
                    for (String level3Code : queryDTO.getLevel3Tags()) {
                        w.or(inner -> inner.like("level3_tag_codes", "\"" + level3Code.trim() + "\""));
                    }
                });
            }

            // 按排序方式处理
            switch (queryDTO.getSort()) {
                case "hot":
                    wrapper.orderByDesc("likes_count");
                    break;
                case "essence":
                    wrapper.eq("is_essence", 1);
                    wrapper.orderByDesc("created_at");
                    break;
                case "latest":
                default:
                    wrapper.orderByDesc("created_at");
                    break;
            }

            Page<Topics> result = this.page(page, wrapper);

            // 补充用户信息和处理数据
            List<Map<String, Object>> topicsWithUserInfo = new ArrayList<>();
            for (Topics topic : result.getRecords()) {
                Map<String, Object> topicInfo = convertTopicToMap(topic);
                User author = userMapper.selectById(topic.getUserId());
                if (author != null) {
                    Map<String, Object> authorInfo = buildAuthorInfo(author);
                    topicInfo.put("author", authorInfo);
                }
                topicsWithUserInfo.add(topicInfo);
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("topics", topicsWithUserInfo);
            responseData.put("total", result.getTotal());
            responseData.put("page", result.getCurrent());
            responseData.put("size", result.getSize());
            responseData.put("totalPages", result.getPages());

            return ApiResult.success(responseData);
        } catch (Exception e) {
            return ApiResult.error(500, "获取话题列表失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResult getTopicDetail(Integer topicId) {
        try {
            Topics topic = this.getById(topicId);
            if (topic == null || topic.getStatus() != 1) {
                return ApiResult.error(404, "话题不存在");
            }

            // 增加浏览量
            topic.setViewsCount(topic.getViewsCount() + 1);
            this.updateById(topic);

            // 获取作者信息
            User author = userMapper.selectById(topic.getUserId());

            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("id", topic.getId());
            result.put("content", topic.getContent());
            result.put("images", parseJsonToList(topic.getImages()));
            result.put("tags", parseJsonToList(topic.getTags()));
            result.put("level1TagCode", topic.getLevel1TagCode());
            result.put("topicTagCodes", parseJsonToList(topic.getTopicTagCodes()));
            result.put("productTagCodes", parseJsonToList(topic.getProductTagCodes()));
            result.put("locationTagCodes", parseJsonToList(topic.getLocationTagCodes()));
            result.put("likesCount", topic.getLikesCount());
            result.put("commentsCount", topic.getCommentsCount());
            result.put("viewsCount", topic.getViewsCount());
            result.put("collectionsCount", topic.getCollectionsCount());
            result.put("isEssence", topic.getIsEssence());
            result.put("isTop", topic.getIsTop() != null ? topic.getIsTop() : 0);
            result.put("isForwarded", topic.getIsForwarded() != null ? topic.getIsForwarded() : false);
            result.put("forwardedFromTopicId", topic.getForwardedFromTopicId());
            result.put("forwardedFromProductId", topic.getForwardedFromProductId());
            result.put("createdAt", topic.getCreatedAt());

            if (author != null) {
                Map<String, Object> authorInfo = new HashMap<>();
                authorInfo.put("id", author.getId());
                authorInfo.put("username", author.getUsername());
                authorInfo.put("realName", author.getRealName());
                authorInfo.put("avatarUrl", author.getAvatarUrl());
                authorInfo.put("studentId", author.getStudentId());
                String level1Tag = determineUserLevel1Tag(author);
                authorInfo.put("level1Tag", level1Tag);
                result.put("author", authorInfo);
            }

            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(500, "获取话题详情失败：" + e.getMessage());
        }
    }

    private Map<String, Object> convertTopicToMap(Topics topic) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", topic.getId());
        map.put("content", topic.getContent());
        map.put("images", parseJsonToList(topic.getImages()));
        map.put("tags", parseJsonToList(topic.getTags()));
        
        // 添加标签字段 - 使用扁平化命名
        map.put("topicTags", parseJsonToList(topic.getTopicTagCodes())); // 话题标签
        map.put("locationTags", parseJsonToList(topic.getLocationTagCodes())); // 地点标签
        
        map.put("isForwarded", topic.getIsForwarded() != null ? topic.getIsForwarded() : false);
        map.put("forwardedFromTopicId", topic.getForwardedFromTopicId());
        map.put("forwardedFromProductId", topic.getForwardedFromProductId());
        map.put("likesCount", topic.getLikesCount());
        map.put("commentsCount", topic.getCommentsCount());
        map.put("viewsCount", topic.getViewsCount());
        map.put("isEssence", topic.getIsEssence());
        map.put("isTop", topic.getIsTop() != null ? topic.getIsTop() : 0);
        map.put("createdAt", topic.getCreatedAt());
        return map;
    }

    private Map<String, Object> buildAuthorInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("realName", user.getRealName());
        info.put("avatarUrl", user.getAvatarUrl());
        info.put("studentId", user.getStudentId());
        info.put("level1Tag", determineUserLevel1Tag(user));
        return info;
    }

    private String determineUserLevel1Tag(User user) {
        if ("admin".equals(user.getRole()) || (user.getIsAdmin() != null && user.getIsAdmin() == 1)) {
            return "admin";
        }
        if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
            return "student";
        }
        if (user.getIsMerchant() != null && user.getIsMerchant() == 1) {
            return "merchant";
        }
        if (user.getIsOrganization() != null && user.getIsOrganization() == 1) {
            return "organization";
        }
        return "society";
    }

    private List<String> parseJsonToList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
