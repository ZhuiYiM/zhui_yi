package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.*;
import com.example.demo.entity.dto.TopicCreateDTO;
import com.example.demo.entity.dto.TopicLikeDTO;
import com.example.demo.entity.dto.TopicQueryDTO;
import com.example.demo.mapper.*;
import com.example.demo.service.TopicsService;
import com.example.demo.utils.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 话题服务实现类
 */
@Service
public class TopicsServiceImpl extends ServiceImpl<TopicsMapper, Topics> implements TopicsService {

    @Autowired
    private TopicsMapper topicsMapper;
    
    @Autowired
    private TopicLikesMapper topicLikesMapper;
    
    @Autowired
    private TopicCommentsMapper topicCommentsMapper;
    
    @Autowired
    private TagsMapper tagsMapper;
    
    @Autowired
    private TopicTagsMapper topicTagsMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TopicCollectionMapper topicCollectionMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ApiResult createTopic(TopicCreateDTO topicDTO, HttpServletRequest request) {
        try {
            // 验证用户权限
            Long userId = extractUserIdFromRequest(request);
            if (userId == null) {
                return ApiResult.error(401, "用户未登录");
            }
            
            // 验证内容长度
            if (topicDTO.getContent() == null || topicDTO.getContent().trim().isEmpty()) {
                return ApiResult.error(400, "话题内容不能为空");
            }
            
            if (topicDTO.getContent().length() > 1000) {
                return ApiResult.error(400, "话题内容不能超过1000字符");
            }
            
            // 验证图片数量
            if (topicDTO.getImages() != null && topicDTO.getImages().size() > 9) {
                return ApiResult.error(400, "图片数量不能超过9张");
            }
            
            // 验证标签数量
            if (topicDTO.getTags() != null && topicDTO.getTags().size() > 5) {
                return ApiResult.error(400, "标签数量不能超过5个");
            }
            
            // 创建话题
            Topics topic = new Topics();
            topic.setUserId(userId);
            topic.setContent(topicDTO.getContent());
            topic.setImages(convertListToJson(topicDTO.getImages()));
            topic.setTags(convertListToJson(topicDTO.getTags()));
            topic.setLikesCount(0);
            topic.setCommentsCount(0);
            topic.setViewsCount(0);
            topic.setIsEssence(0);
            topic.setStatus(1);
            topic.setCreatedAt(LocalDateTime.now());
            topic.setUpdatedAt(LocalDateTime.now());
            
            boolean saveResult = this.save(topic);
            if (!saveResult) {
                return ApiResult.error(500, "话题发布失败");
            }
            
            // 处理标签
            if (topicDTO.getTags() != null && !topicDTO.getTags().isEmpty()) {
                handleTopicTags(topic.getId(), topicDTO.getTags());
            }
            
            return ApiResult.success("话题发布成功", topic);
        } catch (Exception e) {
            return ApiResult.error(500, "话题发布失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult getTopics(TopicQueryDTO queryDTO) {
        try {
            Page<Topics> page = new Page<>(queryDTO.getPage(), queryDTO.getSize());
            QueryWrapper<Topics> wrapper = new QueryWrapper<>();
            
            // 只查询正常状态的话题
            wrapper.eq("status", 1);
            
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
                    Map<String, Object> authorInfo = new HashMap<>();
                    authorInfo.put("id", author.getId());
                    authorInfo.put("username", author.getUsername());
                    authorInfo.put("realName", author.getRealName());
                    authorInfo.put("avatarUrl", author.getAvatarUrl());
                    authorInfo.put("studentId", author.getStudentId());
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
            return ApiResult.error(500, "获取话题列表失败: " + e.getMessage());
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
            if (userId != null) {
                QueryWrapper<TopicLikes> likeWrapper = new QueryWrapper<>();
                likeWrapper.eq("topic_id", topicId.longValue()).eq("user_id", userId.longValue());
                isLiked = topicLikesMapper.selectCount(likeWrapper) > 0;
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
            result.put("likesCount", topic.getLikesCount());
            result.put("commentsCount", topic.getCommentsCount());
            result.put("viewsCount", topic.getViewsCount());
            result.put("isEssence", topic.getIsEssence());
            result.put("createdAt", topic.getCreatedAt());
            result.put("isLiked", isLiked);
            
            if (author != null) {
                Map<String, Object> authorInfo = new HashMap<>();
                authorInfo.put("id", author.getId());
                authorInfo.put("username", author.getUsername());
                authorInfo.put("realName", author.getRealName());
                authorInfo.put("avatarUrl", author.getAvatarUrl());
                authorInfo.put("studentId", author.getStudentId());
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

    @Override
    public ApiResult getPopularTags() {
        try {
            QueryWrapper<Tags> wrapper = new QueryWrapper<>();
            wrapper.orderByDesc("usage_count").last("LIMIT 10");
            List<Tags> tags = tagsMapper.selectList(wrapper);
            
            List<String> tagNames = tags.stream()
                    .map(Tags::getName)
                    .collect(Collectors.toList());
                    
            return ApiResult.success(tagNames);
        } catch (Exception e) {
            return ApiResult.error(500, "获取热门标签失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult searchTags(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ApiResult.error(400, "搜索关键词不能为空");
            }
            
            QueryWrapper<Tags> wrapper = new QueryWrapper<>();
            wrapper.like("name", keyword).orderByDesc("usage_count").last("LIMIT 20");
            List<Tags> tags = tagsMapper.selectList(wrapper);
            
            List<String> tagNames = tags.stream()
                    .map(Tags::getName)
                    .collect(Collectors.toList());
                    
            return ApiResult.success(tagNames);
        } catch (Exception e) {
            return ApiResult.error(500, "搜索标签失败: " + e.getMessage());
        }
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
    
    private List<String> parseJsonToList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
    
    private void handleTopicTags(Long topicId, List<String> tagNames) {
        for (String tagName : tagNames) {
            // 查找或创建标签
            QueryWrapper<Tags> tagWrapper = new QueryWrapper<>();
            tagWrapper.eq("name", tagName);
            Tags tag = tagsMapper.selectOne(tagWrapper);
            
            if (tag == null) {
                // 创建新标签
                tag = new Tags();
                tag.setName(tagName);
                tag.setUsageCount(1);
                tag.setCreatedAt(LocalDateTime.now());
                tagsMapper.insert(tag);
            } else {
                // 更新使用次数
                tag.setUsageCount(tag.getUsageCount() + 1);
                tagsMapper.updateById(tag);
            }
            
            // 创建话题标签关联
            TopicTags topicTag = new TopicTags();
            topicTag.setTopicId(topicId);
            topicTag.setTagId(tag.getId());
            topicTag.setCreatedAt(LocalDateTime.now());
            topicTagsMapper.insert(topicTag);
        }
    }
    
    private Map<String, Object> convertTopicToMap(Topics topic) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", topic.getId());
        map.put("content", topic.getContent());
        map.put("images", parseJsonToList(topic.getImages()));
        map.put("tags", parseJsonToList(topic.getTags()));
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
            
            QueryWrapper<TopicCollection> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId).eq("topic_id", topicId);
            
            TopicCollection existingCollection = topicCollectionMapper.selectOne(wrapper);
            
            if (existingCollection != null) {
                // 取消收藏
                topicCollectionMapper.delete(wrapper);
                return ApiResult.success("已取消收藏", Map.of("collected", false));
            } else {
                // 添加收藏
                TopicCollection collection = new TopicCollection();
                collection.setUserId(userId);
                collection.setTopicId(topicId);
                collection.setCreatedAt(LocalDateTime.now());
                collection.setUpdatedAt(LocalDateTime.now());
                topicCollectionMapper.insert(collection);
                return ApiResult.success("收藏成功", Map.of("collected", true));
            }
        } catch (Exception e) {
            return ApiResult.error(500, "收藏操作失败: " + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getUserCollections(Long userId, Integer page, Integer size) {
        try {
            Page<TopicCollection> collectionPage = new Page<>(page, size);
            QueryWrapper<TopicCollection> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .orderByDesc("created_at");
            
            Page<TopicCollection> result = topicCollectionMapper.selectPage(collectionPage, wrapper);
            
            // 获取收藏的话题详情
            List<Map<String, Object>> collections = new ArrayList<>();
            for (TopicCollection collection : result.getRecords()) {
                Topics topic = this.getById(collection.getTopicId());
                if (topic != null && topic.getStatus() == 1) {
                    Map<String, Object> collectionInfo = new HashMap<>();
                    collectionInfo.put("id", collection.getId());
                    collectionInfo.put("collectedAt", collection.getCreatedAt());
                    
                    // 获取话题基本信息
                    Map<String, Object> topicInfo = new HashMap<>();
                    topicInfo.put("id", topic.getId());
                    topicInfo.put("content", topic.getContent().substring(0, Math.min(100, topic.getContent().length())) + "...");
                    
                    // 获取作者信息
                    User author = userMapper.selectById(topic.getUserId());
                    if (author != null) {
                        Map<String, Object> authorInfo = new HashMap<>();
                        authorInfo.put("id", author.getId());
                        authorInfo.put("username", author.getUsername());
                        authorInfo.put("realName", author.getRealName());
                        topicInfo.put("author", authorInfo);
                    }
                    
                    collectionInfo.put("topic", topicInfo);
                    collections.add(collectionInfo);
                }
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("collections", collections);
            responseData.put("total", result.getTotal());
            responseData.put("page", result.getCurrent());
            responseData.put("size", result.getSize());
            responseData.put("totalPages", result.getPages());
            
            return ApiResult.success(responseData);
        } catch (Exception e) {
            return ApiResult.error(500, "获取收藏列表失败: " + e.getMessage());
        }
    }
}