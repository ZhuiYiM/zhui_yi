package com.example.demo.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.*;
import com.example.demo.entity.dto.TopicCreateDTO;
import com.example.demo.entity.dto.TopicLikeDTO;
import com.example.demo.entity.dto.TopicQueryDTO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.*;
import com.example.demo.service.TopicsService;
import com.example.demo.service.topic.TopicCreateService;
import com.example.demo.service.topic.TopicQueryService;
import com.example.demo.service.topic.TopicLikeService;
import com.example.demo.service.topic.TopicCommentService;
import com.example.demo.service.tags.TopicTagService;
import com.example.demo.service.topic.TopicForwardService;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 话题服务实现类（协调器模式）
 * 将原有功能拆分为 6 个子服务，本类作为协调器统一调度
 */
@Service
public class TopicsServiceImpl extends ServiceImpl<TopicsMapper, Topics> implements TopicsService {

    @Autowired
    private TopicCreateService createService;
    
    @Autowired
    private TopicQueryService queryService;
    
    @Autowired
    private TopicLikeService likeService;
    
    @Autowired
    private TopicCommentService commentService;
    
    @Autowired
    private TopicTagService tagService;
    
    @Autowired
    private TopicForwardService forwardService;
    
    // 保留原有的 Mapper 引用，用于向后兼容
    @Autowired
    private TopicsMapper topicsMapper;
    
    @Autowired
    private TopicLikesMapper topicLikesMapper;
    
    @Autowired
    private TopicCommentsMapper topicCommentsMapper;
    
    @Autowired
    private TopicTagsMapper topicTagsMapper;
    
    @Autowired
    private TopicTagMapper topicTagMapper;
    
    @Autowired
    private LocationTagMapper locationTagMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TopicCollectionMapper topicCollectionMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ApiResult createTopic(TopicCreateDTO topicDTO, HttpServletRequest request) {
        // 委托给 TopicCreateService 处理
        return createService.createTopic(topicDTO, request);
    }

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
            
            // 二级标签筛选 (JSON 数组包含匹配，使用 or 条件组)
            if (queryDTO.getLevel2Tags() != null && queryDTO.getLevel2Tags().length > 0) {
                wrapper.and(w -> {
                    for (String level2Code : queryDTO.getLevel2Tags()) {
                        w.or(inner -> inner.like("level2_tag_codes", "\"" + level2Code.trim() + "\""));
                    }
                });
            }
            
            // 三级标签筛选 (JSON 数组包含匹配，使用 or 条件组)
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
            
            // 标签筛选
            if (queryDTO.getTag() != null && !queryDTO.getTag().trim().isEmpty()) {
                wrapper.like("tags", "\"" + queryDTO.getTag() + "\"");
            }
            
            // 搜索关键词
            if (queryDTO.getSearch() != null && !queryDTO.getSearch().trim().isEmpty()) {
                wrapper.and(w -> w.like("content", queryDTO.getSearch()));
            }
            
            // 用户筛选
            if (queryDTO.getUserId() != null) {
                wrapper.eq("user_id", queryDTO.getUserId());
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
    public ApiResult filterTopics(TopicQueryDTO queryDTO) {
        try {
            Page<Topics> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
            QueryWrapper<Topics> wrapper = new QueryWrapper<>();
                
            // 只查询正常状态的话题
            wrapper.eq("status", 1);
                
            // 一级标签筛选
            if (queryDTO.getLevel1Tag() != null && !queryDTO.getLevel1Tag().trim().isEmpty()) {
                wrapper.eq("level1_tag_code", queryDTO.getLevel1Tag());
            }
                
            // 二级标签筛选 (JSON 数组包含匹配，使用 or 条件组)
            if (queryDTO.getLevel2Tags() != null && queryDTO.getLevel2Tags().length > 0) {
                wrapper.and(w -> {
                    for (String level2Code : queryDTO.getLevel2Tags()) {
                        w.or(inner -> inner.like("level2_tag_codes", "\"" + level2Code.trim() + "\""));
                    }
                });
            }
                
            // 三级标签筛选 (JSON 数组包含匹配，使用 or 条件组)
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
            return ApiResult.error(500, "筛选话题失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResult getTopicDetail(Long topicId, Long userId) {
        try {
            Topics topic = this.getById(topicId);
            if (topic == null || topic.getStatus() != 1) {
                return ApiResult.error(404, "话题不存在");
            }
            
            // 增加浏览量
            topic.setViewsCount(topic.getViewsCount() + 1);
            this.updateById(topic);
            
            // 检查用户是否点赞
            boolean isLiked = false;
            if (userId != null && userId > 0L) {
                QueryWrapper<TopicLikes> likeWrapper = new QueryWrapper<>();
                likeWrapper.eq("topic_id", topicId.longValue()).eq("user_id", userId.longValue());
                isLiked = topicLikesMapper.selectCount(likeWrapper) > 0;
            }
                        
            // 检查用户是否收藏
            boolean isCollected = false;
            if (userId != null && userId > 0L) {
                QueryWrapper<TopicCollection> collectionWrapper= new QueryWrapper<>();
               collectionWrapper.eq("topic_id", topicId.longValue()).eq("user_id", userId.longValue());
                isCollected = topicCollectionMapper.selectCount(collectionWrapper) > 0;
            }
            
            // 获取作者信息
            User author = userMapper.selectById(topic.getUserId());
            
            // 获取评论
            QueryWrapper<TopicComments> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("topic_id", topicId).eq("status", 1).orderByAsc("created_at");
            List<TopicComments> comments = topicCommentsMapper.selectList(commentWrapper);
            
            // 构造返回数据
            Map<String, Object> result = new HashMap<>();
            result.put("id", topic.getId());
            result.put("content", topic.getContent());
            result.put("images", parseJsonToList(topic.getImages()));
            result.put("tags", parseJsonToList(topic.getTags()));
            
            // 添加分级标签信息 - 直接返回字符串数组
            result.put("level1TagCode", topic.getLevel1TagCode());
            result.put("topicTagCodes", parseJsonToList(topic.getTopicTagCodes()));
            result.put("productTagCodes", parseJsonToList(topic.getProductTagCodes()));
            result.put("locationTagCodes", parseJsonToList(topic.getLocationTagCodes()));
            
            // 移除 tagsObject，避免返回 Map 对象导致前端类型转换错误
            // 前端可以直接使用 topicTagCodes、productTagCodes、locationTagCodes 字符串数组
            
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
            result.put("isLiked", isLiked);
            result.put("isCollected", isCollected);
            
            if (author != null) {
                Map<String, Object> authorInfo = new HashMap<>();
                authorInfo.put("id", author.getId());
                authorInfo.put("username", author.getUsername());
                authorInfo.put("realName", author.getRealName());
                authorInfo.put("avatarUrl", author.getAvatarUrl());
                authorInfo.put("studentId", author.getStudentId());
                // 添加 level1Tag 字段
                String level1Tag = determineUserLevel1Tag(author);
                authorInfo.put("level1Tag", level1Tag);
                result.put("author", authorInfo);
            }
            
            // 处理评论数据
            List<Map<String, Object>> commentList = new ArrayList<>();
            for (TopicComments comment : comments) {
                Map<String, Object> commentInfo = new HashMap<>();
                commentInfo.put("id", comment.getId());
                commentInfo.put("content", comment.getContent());
                commentInfo.put("likesCount", comment.getLikesCount());
                commentInfo.put("createdAt", comment.getCreatedAt());
                commentInfo.put("parentId", comment.getParentId());
                
                User commenter = userMapper.selectById(comment.getUserId());
                if (commenter != null) {
                    Map<String, Object> commenterInfo = new HashMap<>();
                    commenterInfo.put("id", commenter.getId());
                    commenterInfo.put("username", commenter.getUsername());
                    commenterInfo.put("realName", commenter.getRealName());
                    commenterInfo.put("avatarUrl", commenter.getAvatarUrl());
                    commentInfo.put("author", commenterInfo);
                }
                commentList.add(commentInfo);
            }
            result.put("comments", commentList);
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(500, "获取话题详情失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResult likeTopic(Long topicId, TopicLikeDTO likeDTO, HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromRequest(request);
            if (userId == null) {
                return ApiResult.error(401, "用户未登录");
            }
            
            Topics topic = this.getById(topicId);
            if (topic == null || topic.getStatus() != 1) {
                return ApiResult.error(404, "话题不存在");
            }
            
            QueryWrapper<TopicLikes> wrapper = new QueryWrapper<>();
            wrapper.eq("topic_id", topicId).eq("user_id", userId);
            
            if ("like".equals(likeDTO.getAction())) {
                // 点赞操作
                TopicLikes existingLike = topicLikesMapper.selectOne(wrapper);
                if (existingLike != null) {
                    return ApiResult.error(409, "已点赞");
                }
                
                TopicLikes topicLike = new TopicLikes();
                topicLike.setTopicId(topicId);
                topicLike.setUserId(userId);
                topicLike.setCreatedAt(LocalDateTime.now());
                topicLikesMapper.insert(topicLike);
                
                // 更新话题点赞数
                topic.setLikesCount(topic.getLikesCount() + 1);
                this.updateById(topic);
                
                Map<String, Object> result = new HashMap<>();
                result.put("likesCount", topic.getLikesCount());
                result.put("isLiked", true);
                return ApiResult.success(result);
                
            } else if ("unlike".equals(likeDTO.getAction())) {
                // 取消点赞操作
                TopicLikes existingLike = topicLikesMapper.selectOne(wrapper);
                if (existingLike == null) {
                    return ApiResult.error(409, "未点赞");
                }
                
                topicLikesMapper.delete(wrapper);
                
                // 更新话题点赞数
                topic.setLikesCount(Math.max(0, topic.getLikesCount() - 1));
                this.updateById(topic);
                
                Map<String, Object> result = new HashMap<>();
                result.put("likesCount", topic.getLikesCount());
                result.put("isLiked", false);
                return ApiResult.success(result);
            } else {
                return ApiResult.error(400, "无效的操作类型");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "点赞操作失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult deleteTopic(Long topicId, Long userId) {
        try {
            Topics topic = this.getById(topicId);
            if (topic == null) {
                return ApiResult.error(404, "话题不存在");
            }
            
            if (!topic.getUserId().equals(userId)) {
                return ApiResult.error(403, "无权删除此话题");
            }
            
            // 逻辑删除
            topic.setStatus(0);
            topic.setUpdatedAt(LocalDateTime.now());
            boolean result = this.updateById(topic);
            
            if (result) {
                return ApiResult.success("话题删除成功");
            } else {
                return ApiResult.error(500, "话题删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "话题删除失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult updateTopic(Long topicId, TopicCreateDTO topicDTO, Long userId) {
        try {
            Topics topic = this.getById(topicId);
            if (topic == null) {
                return ApiResult.error(404, "话题不存在");
            }
            
            if (!topic.getUserId().equals(userId)) {
                return ApiResult.error(403, "无权修改此话题");
            }
            
            // 验证内容
            if (topicDTO.getContent() == null || topicDTO.getContent().trim().isEmpty()) {
                return ApiResult.error(400, "话题内容不能为空");
            }
            
            if (topicDTO.getContent().length() > 1000) {
                return ApiResult.error(400, "话题内容不能超过1000字符");
            }
            
            // 更新话题
            topic.setContent(topicDTO.getContent());
            topic.setImages(convertListToJson(topicDTO.getImages()));
            topic.setTags(convertListToJson(topicDTO.getTags()));
            topic.setUpdatedAt(LocalDateTime.now());
            
            boolean result = this.updateById(topic);
            if (result) {
                return ApiResult.success("话题更新成功");
            } else {
                return ApiResult.error(500, "话题更新失败");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "话题更新失败: " + e.getMessage());
        }
    }

    // @Override
    // public ApiResult getPopularTags() {
    //     try {
    //         QueryWrapper<Tags> wrapper = new QueryWrapper<>();
    //         wrapper.orderByDesc("usage_count").last("LIMIT 10");
    //         List<Tags> tags = tagsMapper.selectList(wrapper);
    //         
    //         List<String> tagNames = tags.stream()
    //                 .map(Tags::getName)
    //                 .collect(Collectors.toList());
    //                 
    //         return ApiResult.success(tagNames);
    //     } catch (Exception e) {
    //         return ApiResult.error(500, "获取热门标签失败：" + e.getMessage());
    //     }
    // }
    
    @Override
    public ApiResult getPopularTags() {
        // 已废弃，使用四级标签系统
        return ApiResult.success();
    }
    
    // @Override
    // public ApiResult searchTags(String keyword) {
    //     try {
    //         if (keyword == null || keyword.trim().isEmpty()) {
    //             return ApiResult.error(400, "搜索关键词不能为空");
    //         }
    //         
    //         QueryWrapper<Tags> wrapper = new QueryWrapper<>();
    //         wrapper.like("name", keyword).orderByDesc("usage_count").last("LIMIT 20");
    //         List<Tags> tags = tagsMapper.selectList(wrapper);
    //         
    //         List<String> tagNames = tags.stream()
    //                 .map(Tags::getName)
    //                 .collect(Collectors.toList());
    //                 
    //         return ApiResult.success(tagNames);
    //     } catch (Exception e) {
    //         return ApiResult.error(500, "搜索标签失败：" + e.getMessage());
    //     }
    // }
    
    @Override
    public ApiResult searchTags(String keyword) {
        // 已废弃，使用四级标签系统
        return ApiResult.success();
    }

    @Override
    public ApiResult getCommunityStats() {
        try {
            // 统计总话题数
            QueryWrapper<Topics> topicWrapper = new QueryWrapper<>();
            topicWrapper.eq("status", 1);
            long totalPosts = topicsMapper.selectCount(topicWrapper);
            
            // 统计总评论数
            QueryWrapper<TopicComments> commentWrapper = new QueryWrapper<>();
            commentWrapper.eq("status", 1);
            long totalComments = topicCommentsMapper.selectCount(commentWrapper);
            
            // 统计总点赞数
            long totalLikes = topicLikesMapper.selectCount(null);
            
            // 今日话题数
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            QueryWrapper<Topics> todayWrapper = new QueryWrapper<>();
            todayWrapper.eq("status", 1).ge("created_at", todayStart);
            long todayPosts = topicsMapper.selectCount(todayWrapper);
            
            // 活跃用户数（近30天有活动的用户）
            LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
            QueryWrapper<Topics> activeUserWrapper = new QueryWrapper<>();
            activeUserWrapper.eq("status", 1).ge("created_at", monthAgo);
            List<Topics> recentTopics = topicsMapper.selectList(activeUserWrapper);
            Set<Long> activeUsers = recentTopics.stream()
                    .map(Topics::getUserId)
                    .collect(Collectors.toSet());
            long activeUserCount = activeUsers.size();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalPosts", totalPosts);
            stats.put("activeUsers", activeUserCount);
            stats.put("todayPosts", todayPosts);
            stats.put("totalComments", totalComments);
            stats.put("totalLikes", totalLikes);
            
            return ApiResult.success(stats);
        } catch (Exception e) {
            return ApiResult.error(500, "获取社区统计失败: " + e.getMessage());
        }
    }

    // 私有辅助方法
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token) != null ? jwtUtil.getUserIdFromToken(token).longValue() : 0L;
    }
    
    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    /**
     * 解析 JSON 字符串到列表
     * 支持两种格式：
     * 1. 字符串数组：["string1", "string2"]
     * 2. 对象数组：[{"name": "value1"}, {"name": "value2"}] - 提取 name 字段
     */
    private List<String> parseJsonToList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // 先尝试解析为字符串数组
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            // 如果失败，尝试解析为对象数组并提取 name 字段
            try {
                List<Map<String, Object>> objectList = objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
                List<String> result = new ArrayList<>();
                for (Map<String, Object> obj : objectList) {
                    // 优先提取 name 字段，如果没有则提取 code 字段
                    Object value = obj.get("name");
                    if (value == null) {
                        value = obj.get("code");
                    }
                    if (value != null) {
                        result.add(value.toString());
                    }
                }
                return result;
            } catch (JsonProcessingException e2) {
                System.err.println("⚠️ 解析 JSON 失败（两种格式都尝试）: " + json);
                return new ArrayList<>();
            }
        }
    }
    
    private Map<String, Object> convertTopicToMap(Topics topic) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", topic.getId());
        map.put("content", topic.getContent());
        
        // 解析图片
        List<String> images = parseJsonToList(topic.getImages());
        // System.out.println("🔍 从数据库读取话题 " + topic.getId() + " 的图片:");
        // System.out.println("   DB JSON: " + topic.getImages());
        // System.out.println("   Parsed List: " + (images != null ? images.size() + " 张" : "null"));
        
        // 修复图片路径：添加后端 base URL
        String baseUrl = "http://localhost:8080";
        List<String> processedImages = new ArrayList<>();
        if (images != null) {
            for (String url : images) {
                if (url != null && !url.trim().isEmpty()) {
                    // 如果 URL 不以 http 开头，添加 base URL
                    if (!url.startsWith("http://") && !url.startsWith("https://")) {
                        url = baseUrl + url;
                    }
                    processedImages.add(url);
                    // System.out.println("     - " + url);
                }
            }
        }
        map.put("images", processedImages);
        
        // 解析标签代码
        List<String> topicTagCodes = parseJsonToList(topic.getTopicTagCodes());
        List<String> locationTagCodes = parseJsonToList(topic.getLocationTagCodes());
        String identityTagCode = topic.getLevel1TagCode(); // 话题本身的身份标签
        
        // System.out.println("🔍 话题 " + topic.getId() + " 的标签:");
        // System.out.println("   level1TagCode DB: " + topic.getLevel1TagCode());
        // System.out.println("   Parsed identityTagCode: " + identityTagCode);
        // System.out.println("   topicTagCodes DB: " + topic.getTopicTagCodes());
        // System.out.println("   Parsed topicTagCodes: " + topicTagCodes);
        // System.out.println("   locationTagCodes DB: " + topic.getLocationTagCodes());
        // System.out.println("   Parsed locationTagCodes: " + locationTagCodes);
        
        // 获取完整的标签对象信息
        map.put("tags", buildTopicTagsInfo(topicTagCodes)); // 话题标签完整信息
        map.put("topicTags", buildTopicTagsInfo(topicTagCodes)); // 保持兼容
        map.put("locationTags", buildLocationTagsInfo(locationTagCodes)); // 地点标签完整信息
        map.put("identityTag", identityTagCode); // 保持兼容（返回代码）
        
        // 添加作者信息
        User author = userMapper.selectById(topic.getUserId());
        if (author != null) {
            Map<String, Object> authorInfo = buildAuthorInfo(author);
            // System.out.println("   作者身份标签：" + authorInfo.get("identityTag"));
            map.put("author", authorInfo);
        }
        
        // 添加转发信息（始终返回，即使为 false）
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
    
    @Override
    @Transactional
    public ApiResult toggleTopicTop(Long topicId, Boolean isTop, Long userId) {
        try {
            Topics topic = this.getById(topicId);
            if (topic == null || topic.getStatus() != 1) {
                return ApiResult.error(404, "话题不存在");
            }
            
            // 验证用户权限（只有管理员或话题作者可以置顶）
            if (!topic.getUserId().equals(userId)) {
                // 这里可以添加管理员权限检查逻辑
                return ApiResult.error(403, "无权限操作此话题");
            }
            
            topic.setIsTop(isTop ? 1 : 0);
            topic.setUpdatedAt(LocalDateTime.now());
            
            boolean result = this.updateById(topic);
            if (result) {
                String message = isTop ? "话题置顶成功" : "话题取消置顶成功";
                Map<String, Object> data = new HashMap<>();
                data.put("isTop", isTop);
                return ApiResult.success(message, data);
            } else {
                return ApiResult.error(500, "操作失败");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "置顶操作失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult toggleTopicEssence(Long topicId, Boolean isEssence, Long userId) {
        try {
            Topics topic = this.getById(topicId);
            if (topic == null || topic.getStatus() != 1) {
                return ApiResult.error(404, "话题不存在");
            }
            
            // 验证用户权限（只有管理员可以设置精华）
            // 这里可以添加管理员权限检查逻辑
            
            topic.setIsEssence(isEssence ? 1 : 0);
            topic.setUpdatedAt(LocalDateTime.now());
            
            boolean result = this.updateById(topic);
            if (result) {
                String message = isEssence ? "话题设为精华成功" : "话题取消精华成功";
                Map<String, Object> data = new HashMap<>();
                data.put("isEssence", isEssence);
                return ApiResult.success(message, data);
            } else {
                return ApiResult.error(500, "操作失败");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "精华操作失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult toggleTopicCollect(Long topicId, Long userId) {
        try {
           Topics topic = this.getById(topicId);
          if (topic == null || topic.getStatus() != 1) {
           return ApiResult.error(404, "话题不存在");
          }
          
         QueryWrapper<TopicCollection> wrapper= new QueryWrapper<>();
         wrapper.eq("user_id", userId).eq("topic_id", topicId);
         
         TopicCollection existingCollection = topicCollectionMapper.selectOne(wrapper);
         
         if (existingCollection != null) {
             // 取消收藏
          topicCollectionMapper.delete(wrapper);
             // 更新话题收藏数
          topic.setCollectionsCount(Math.max(0, topic.getCollectionsCount() - 1));
            this.updateById(topic);
            Map<String, Object> result = new HashMap<>();
          result.put("collected", false);
          result.put("collectionsCount", topic.getCollectionsCount());
         return ApiResult.success("已取消收藏", result);
         } else {
             // 添加收藏
            TopicCollection collection = new TopicCollection();
          collection.setUserId(userId);
          collection.setTopicId(topicId);
          collection.setCreatedAt(LocalDateTime.now());
          collection.setUpdatedAt(LocalDateTime.now());
          topicCollectionMapper.insert(collection);
             // 更新话题收藏数
          topic.setCollectionsCount(topic.getCollectionsCount() + 1);
            this.updateById(topic);
            Map<String, Object> result = new HashMap<>();
          result.put("collected", true);
          result.put("collectionsCount", topic.getCollectionsCount());
         return ApiResult.success("收藏成功", result);
         }
     } catch (Exception e) {
      return ApiResult.error(500, "收藏操作失败：" + e.getMessage());
     }
    }
    
    @Override
   public ApiResult getUserLikedTopics(Long userId, Integer page, Integer size) {
        try {
            // 分页查询用户点赞记录
            Page<TopicLikes> likesPage = new Page<>(page, size);
            QueryWrapper<TopicLikes> wrapper= new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .orderByDesc("created_at");
            
            Page<TopicLikes> result = topicLikesMapper.selectPage(likesPage, wrapper);
            
            // 获取对应的话题列表
            List<Map<String, Object>> topicsWithLikeInfo = new ArrayList<>();
            for (TopicLikes like : result.getRecords()) {
                Topics topic = this.getById(like.getTopicId());
                if (topic != null && topic.getStatus() == 1) {
                    Map<String, Object> topicInfo = convertTopicToMap(topic);
                    User author = userMapper.selectById(topic.getUserId());
                    if (author != null) {
                        Map<String, Object> authorInfo = buildAuthorInfo(author);
                        topicInfo.put("author", authorInfo);
                    }
                    // 添加点赞时间
                    topicInfo.put("likedAt", like.getCreatedAt());
                    topicsWithLikeInfo.add(topicInfo);
                }
            }
            
            Map<String, Object> responseData = new HashMap<>();
           responseData.put("topics", topicsWithLikeInfo);
           responseData.put("total", result.getTotal());
           responseData.put("page", result.getCurrent());
           responseData.put("size", result.getSize());
           responseData.put("totalPages", result.getPages());
            
           return ApiResult.success(responseData);
        } catch (Exception e) {
           return ApiResult.error(500, "获取用户点赞话题失败：" + e.getMessage());
        }
    }
    
    @Override
   public ApiResult getUserCollections(Long userId, Integer page, Integer size) {
        try {
            // 分页查询用户收藏记录
            Page<TopicCollection> collectionPage = new Page<>(page, size);
            QueryWrapper<TopicCollection> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .orderByDesc("created_at");
            
            Page<TopicCollection> result = topicCollectionMapper.selectPage(collectionPage, wrapper);
            
            // 获取对应的话题列表
            List<Map<String, Object>> topicsWithCollectionInfo = new ArrayList<>();
            for (TopicCollection collection : result.getRecords()) {
                Topics topic = this.getById(collection.getTopicId());
                if (topic != null && topic.getStatus() == 1) {
                    Map<String, Object> topicInfo = convertTopicToMap(topic);
                    User author = userMapper.selectById(topic.getUserId());
                    if (author != null) {
                        Map<String, Object> authorInfo = buildAuthorInfo(author);
                        topicInfo.put("author", authorInfo);
                    }
                    // 添加收藏时间
                    topicInfo.put("collectedAt", collection.getCreatedAt());
                    topicsWithCollectionInfo.add(topicInfo);
                }
            }
            
            Map<String, Object> responseData = new HashMap<>();
           responseData.put("topics", topicsWithCollectionInfo);
           responseData.put("total", result.getTotal());
           responseData.put("page", result.getCurrent());
           responseData.put("size", result.getSize());
           responseData.put("totalPages", result.getPages());
            
           return ApiResult.success(responseData);
        } catch (Exception e) {
           return ApiResult.error(500, "获取用户收藏话题失败：" + e.getMessage());
        }
    }
    
    /**
     * 构建作者信息（包含身份信息）
     */
    private Map<String, Object> buildAuthorInfo(User author) {
        Map<String, Object> authorInfo = new HashMap<>();
        authorInfo.put("id", author.getId());
        authorInfo.put("username", author.getUsername());
        authorInfo.put("realName", author.getRealName());
        authorInfo.put("avatarUrl", author.getAvatarUrl());
        authorInfo.put("studentId", author.getStudentId());
        
        // 添加身份信息
        Map<String, Object> identityInfo = buildIdentityInfo(author);
        authorInfo.put("identity", identityInfo);
        
        // 直接添加 identityTag 字段，方便前端读取
        String identityTag = determineUserLevel1Tag(author);
        authorInfo.put("identityTag", identityTag);
        
        return authorInfo;
    }
    
    /**
     * 构建用户身份信息
     */
    private Map<String, Object> buildIdentityInfo(User user) {
        Map<String, Object> identity = new HashMap<>();
        
        // 确定一级身份标签
        String level1Tag = determineUserLevel1Tag(user);
        identity.put("level1Tag", level1Tag);
        identity.put("level1TagName", getLevel1TagName(level1Tag));
        
        // 认证状态
        Map<String, Boolean> certifications = new HashMap<>();
        certifications.put("realName", user.getIsRealNameVerified() != null && user.getIsRealNameVerified() == 1);
        certifications.put("student", user.getStudentId() != null && !user.getStudentId().trim().isEmpty());
        certifications.put("merchant", user.getIsMerchant() != null && user.getIsMerchant() == 1);
        certifications.put("organization", user.getIsOrganization() != null && user.getIsOrganization() == 1);
        identity.put("certifications", certifications);
        
        return identity;
    }
    
    /**
     * 确定用户的一级身份标签
     */
    private String determineUserLevel1Tag(User user) {
        if (user == null) {
            return "society";
        }
        
        // 1. 检查管理员身份（最高优先级）
        if ("admin".equals(user.getRole()) || 
            (user.getIsAdmin() != null && user.getIsAdmin() == 1)) {
            return "admin";
        }
        
        // 2. 检查学生身份
        if (user.getStudentId() != null && !user.getStudentId().trim().isEmpty()) {
            return "student";
        }
        
        // 3. 检查商户身份
        if (user.getIsMerchant() != null && user.getIsMerchant() == 1) {
            return "merchant";
        }
        
        // 4. 检查团体身份
        if (user.getIsOrganization() != null && user.getIsOrganization() == 1) {
            return "organization";
        }
        
        // 5. 默认为社会
        return "society";
    }
    
    /**
     * 获取一级标签名称
     */
    private String getLevel1TagName(String code) {
        switch (code) {
            case "admin":
                return "管理员";
            case "student":
                return "学生";
            case "merchant":
                return "校外商户";
            case "organization":
                return "团体";
            default:
                return "社会";
        }
    }
    
    /**
     * 构建一级标签信息
     */
    private Map<String, String> buildLevel1TagInfo(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        Map<String, String> tagInfo = new HashMap<>();
        tagInfo.put("code", code);
        tagInfo.put("name", getLevel1TagName(code));
        
        // 添加图标
        switch (code) {
            case "admin":
                tagInfo.put("icon", "🛡️");
                break;
            case "student":
                tagInfo.put("icon", "👨‍🎓");
                break;
            case "merchant":
                tagInfo.put("icon", "🏪");
                break;
            case "organization":
                tagInfo.put("icon", "👥");
                break;
            default:
                tagInfo.put("icon", "🌍");
        }
        
        return tagInfo;
    }
    
    /**
     * 构建二级标签信息列表
     */
    private List<Map<String, String>> buildLevel2TagInfos(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, String>> result = new ArrayList<>();
        for (String code : codes) {
            Map<String, String> tagInfo = new HashMap<>();
            tagInfo.put("code", code);
            tagInfo.put("name", code); // TODO: 从数据库查询名称
            tagInfo.put("color", "#50C878"); // TODO: 从数据库查询颜色
            result.add(tagInfo);
        }
        return result;
    }
    
    /**
     * 构建三级标签信息列表
     */
    private List<Map<String, String>> buildLevel3TagInfos(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, String>> result = new ArrayList<>();
        for (String code : codes) {
            Map<String, String> tagInfo = new HashMap<>();
            tagInfo.put("code", code);
            tagInfo.put("name", code); // TODO: 从数据库查询名称
            tagInfo.put("type", "location"); // TODO: 从数据库查询类型
            result.add(tagInfo);
        }
        return result;
    }
    
    /**
     * 构建话题标签信息列表（从 topic_tag 表查询）
     */
    private List<Map<String, Object>> buildTopicTagsInfo(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (String code : codes) {
            try {
                // System.out.println("🔍 查询话题标签：" + code);
                // 查询话题标签（优先使用 name 字段，如果没有则尝试 code 字段）
                QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
                wrapper.and(w -> w.eq("name", code).or().eq("code", code));
                wrapper.last("LIMIT 1");
                TopicTag tag = topicTagMapper.selectOne(wrapper);
                
                if (tag != null) {
                    // System.out.println("✅ 找到标签：" + tag.getName() + " (id=" + tag.getId() + ")");
                    Map<String, Object> tagInfo = new HashMap<>();
                    tagInfo.put("id", tag.getId());
                    tagInfo.put("code", tag.getName() != null ? tag.getName() : tag.getCode());
                    tagInfo.put("name", tag.getName());
                    tagInfo.put("icon", tag.getIcon());
                    tagInfo.put("color", tag.getColor());
                    tagInfo.put("type", tag.getType());
                    result.add(tagInfo);
                } else {
                    // System.out.println("❌ 未找到标签：" + code);
                }
            } catch (Exception e) {
                System.err.println("查询话题标签失败：" + code + ", error: " + e.getMessage());
            }
        }
        // System.out.println("🏷️ 返回话题标签数量：" + result.size());
        return result;
    }
    
    /**
     * 构建地点标签信息列表（从 location_tag 表查询）
     */
    private List<Map<String, Object>> buildLocationTagsInfo(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (String code : codes) {
            try {
                // System.out.println("📍 查询地点标签：" + code);
                // 查询地点标签（使用 name 字段匹配）
                QueryWrapper<LocationTag> wrapper = new QueryWrapper<>();
                wrapper.eq("name", code).last("LIMIT 1");
                LocationTag tag = locationTagMapper.selectOne(wrapper);
                
                if (tag != null) {
                    // System.out.println("✅ 找到地点标签：" + tag.getName() + " (id=" + tag.getId() + ")");
                    Map<String, Object> tagInfo = new HashMap<>();
                    tagInfo.put("id", tag.getId());
                    tagInfo.put("code", tag.getName()); // LocationTag 只有 name 字段
                    tagInfo.put("name", tag.getName());
                    tagInfo.put("type", tag.getType());
                    tagInfo.put("color", tag.getColor());
                    tagInfo.put("icon", tag.getIcon());
                    result.add(tagInfo);
                } else {
                    // System.out.println("❌ 未找到地点标签：" + code);
                }
            } catch (Exception e) {
                System.err.println("查询地点标签失败：" + code + ", error: " + e.getMessage());
            }
        }
        // System.out.println("🏷️ 返回地点标签数量：" + result.size());
        return result;
    }
    
    /**
     * 构建四级标签信息列表
     */
    private List<Map<String, Object>> buildLevel4TagInfos(List<String> codes, String content) {
        if (codes == null || codes.isEmpty()) {
           return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (String code : codes) {
            Map<String, Object> tagInfo = new HashMap<>();
           tagInfo.put("code", code);
           tagInfo.put("name", code); // TODO: 从数据库查询名称
           tagInfo.put("category", "tech"); // TODO: 从数据库查询分类
           result.add(tagInfo);
        }
       return result;
    }
    
    // 重载方法，保持向后兼容
    private List<Map<String, Object>> buildLevel4TagInfos(List<String> codes) {
        return buildLevel4TagInfos(codes, null);
    }
}
