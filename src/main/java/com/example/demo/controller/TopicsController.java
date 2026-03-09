package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.dto.CommentCreateDTO;
import com.example.demo.entity.dto.TopicCreateDTO;
import com.example.demo.entity.dto.TopicLikeDTO;
import com.example.demo.entity.dto.TopicQueryDTO;
import com.example.demo.service.TopicCommentsService;
import com.example.demo.service.TopicsService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 话题墙控制器
 * 提供话题相关的RESTful API接口
 */
@RestController
@RequestMapping("/api/topics")
@CrossOrigin
public class TopicsController {

    @Autowired
    private TopicsService topicsService;
    
    @Autowired
    private TopicCommentsService topicCommentsService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取话题列表
     */
    @GetMapping
    public ApiResult getTopics(/*@Valid*/ TopicQueryDTO queryDTO) {
        return topicsService.getTopics(queryDTO);
    }

    /**
     * 发布新话题
     */
    @PostMapping
    public ApiResult createTopic(@Valid @RequestBody TopicCreateDTO topicDTO, 
                             HttpServletRequest request) {
        return topicsService.createTopic(topicDTO, request);
    }

    /**
     * 获取话题详情
     */
    @GetMapping("/{id}")
    public ApiResult getTopicDetail(@PathVariable Long id,
                                @RequestParam(required = false) Long userId) {
        return topicsService.getTopicDetail(id, userId);
    }

    /**
     * 点赞话题
     */
    @PostMapping("/{id}/like")
    public ApiResult likeTopic(@PathVariable Long id,
                           @Valid @RequestBody TopicLikeDTO likeDTO,
                           HttpServletRequest request) {
        return topicsService.likeTopic(id, likeDTO, request);
    }

    /**
     * 更新话题
     */
    @PutMapping("/{id}")
    public ApiResult updateTopic(@PathVariable Long id,
                             @Valid @RequestBody TopicCreateDTO topicDTO,
                             HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        return topicsService.updateTopic(id, topicDTO, userId != null ? userId : 0L);
    }

    /**
     * 删除话题
     */
    @DeleteMapping("/{id}")
    public ApiResult deleteTopic(@PathVariable Long id,
                             HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        return topicsService.deleteTopic(id, userId != null ? userId : 0L);
    }

    /**
     * 获取话题评论列表
     */
    @GetMapping("/{id}/comments")
    public ApiResult getTopicComments(@PathVariable Long id,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "20") Integer size,
                                  @RequestParam(defaultValue = "latest") String sort) {
        return topicCommentsService.getTopicComments(id, page, size, sort);
    }

    /**
     * 发布评论
     */
    @PostMapping("/{id}/comments")
    public ApiResult createComment(@PathVariable Long id,
                               @Valid @RequestBody CommentCreateDTO commentDTO,
                               HttpServletRequest request) {
        return topicCommentsService.createComment(id, commentDTO, request);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{commentId}")
    public ApiResult deleteComment(@PathVariable Long commentId,
                               HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        return topicCommentsService.deleteComment(commentId, userId != null ? userId : 0L);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/comments/{commentId}/like")
    public ApiResult likeComment(@PathVariable Long commentId,
                             @RequestParam String action,
                             HttpServletRequest request) {
        return topicCommentsService.likeComment(commentId, action, request);
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/tags/popular")
    public ApiResult getPopularTags() {
        return topicsService.getPopularTags();
    }

    /**
     * 搜索标签
     */
    @GetMapping("/tags/search")
    public ApiResult searchTags(@RequestParam String keyword) {
        return topicsService.searchTags(keyword);
    }

    /**
     * 获取社区统计
     */
    @GetMapping("/stats")
    public ApiResult getCommunityStats() {
        return topicsService.getCommunityStats();
    }
        
    /**
     * 话题置顶操作
     */
    @PutMapping("/{id}/top")
    public ApiResult toggleTopicTop(@PathVariable Long id,
                                   @RequestParam Boolean isTop,
                                   HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        return topicsService.toggleTopicTop(id, isTop, userId != null ? userId : 0L);
    }
        
    /**
     * 话题精华操作
     */
    @PutMapping("/{id}/essence")
    public ApiResult toggleTopicEssence(@PathVariable Long id,
                                       @RequestParam Boolean isEssence,
                                       HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        return topicsService.toggleTopicEssence(id, isEssence, userId != null ? userId : 0L);
    }
        
    /**
     * 收藏/取消收藏话题
     */
    @PostMapping("/{id}/collect")
    public ApiResult toggleTopicCollect(@PathVariable Long id,
                                       HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        if (userId == null || userId == 0L) {
            return ApiResult.error(401, "用户未登录");
        }
        return topicsService.toggleTopicCollect(id, userId);
    }
        
    /**
     * 获取用户收藏列表
     */
    @GetMapping("/collections")
    public ApiResult getUserCollections(HttpServletRequest request,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer size) {
        Long userId = extractUserIdFromRequest(request);
        if (userId == null || userId == 0L) {
            return ApiResult.error(401, "用户未登录");
        }
        return topicsService.getUserCollections(userId, page, size);
    }

    /**
     * 搜索话题
     */
    @GetMapping("/search")
    public ApiResult searchTopics(@RequestParam String q,
                                  @RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer size) {
        TopicQueryDTO queryDTO = new TopicQueryDTO();
        queryDTO.setSearch(q);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setSort("latest");
        return topicsService.getTopics(queryDTO);
    }

    /**
     * 通用点赞接口 (兼容不带 ID 的路径)
     * POST /api/topics/like
     * 注意：此方法已被废弃，建议使用 /api/topics/{id}/like
     */
    @Deprecated
    @PostMapping("/like")
    public ApiResult likeTopicLegacy(
            @RequestParam Long topicId,
            @RequestParam String action,
            HttpServletRequest request) {
        com.example.demo.entity.dto.TopicLikeDTO likeDTO = new com.example.demo.entity.dto.TopicLikeDTO();
        likeDTO.setAction(action);
        return topicsService.likeTopic(topicId, likeDTO, request);
    }

    /**
     * 根据标签获取话题
     * GET /api/topics/tag/:tag?page=1&size=10
     */
    @GetMapping("/tag/{tag}")
    public ApiResult getTopicsByTag(@PathVariable String tag,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer size) {
        TopicQueryDTO queryDTO = new TopicQueryDTO();
        queryDTO.setTag(tag);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setSort("latest");
        return topicsService.getTopics(queryDTO);
    }

    /**
     * 获取用户发布的话题
     * GET /api/users/:userId/topics?page=1&size=10&sort=latest
     */
    @GetMapping("/users/{userId}")
    public ApiResult getUserTopics(@PathVariable Long userId,
                                   @RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer size,
                                   @RequestParam(defaultValue = "latest") String sort) {
        TopicQueryDTO queryDTO = new TopicQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setSort(sort);
        return topicsService.getTopics(queryDTO);
    }

    /**
     * 根据多级标签筛选话题
     * GET /api/topics/filter?level1=student&level2=study,life&level3=library,dorm&page=1&size=10
     * POST /api/topics/filter (JSON body)
     */
    @GetMapping("/filter")
    @PostMapping("/filter")
    public ApiResult filterTopics(
            @RequestParam(required = false) String level1,
            @RequestParam(required = false) String level2,
            @RequestParam(required = false) String level3,
            @RequestBody(required = false) TopicQueryDTO requestBody,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        TopicQueryDTO queryDTO = new TopicQueryDTO();
        
        // 如果是 POST 请求，从 body 获取参数
        if (requestBody != null) {
            queryDTO.setLevel1Tag(requestBody.getLevel1Tag());
            queryDTO.setLevel2Tags(requestBody.getLevel2Tags());
            queryDTO.setLevel3Tags(requestBody.getLevel3Tags());
            queryDTO.setPage(requestBody.getPage());
            queryDTO.setSize(requestBody.getSize());
            queryDTO.setSort(requestBody.getSort());
        } else {
            // GET 请求，从 URL 参数获取
            queryDTO.setLevel1Tag(level1);
            // 将逗号分隔的字符串转为数组
            if (level2 != null && !level2.trim().isEmpty()) {
                queryDTO.setLevel2Tags(level2.split(","));
            }
            if (level3 != null && !level3.trim().isEmpty()) {
                queryDTO.setLevel3Tags(level3.split(","));
            }
            queryDTO.setPage(page);
            queryDTO.setSize(size);
            queryDTO.setSort("latest");
        }
        
        return topicsService.filterTopics(queryDTO);
    }

    /**
     * 从请求中提取用户 ID 的辅助方法
     */
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        return jwtUtil.getUserIdFromToken(token) != null ? jwtUtil.getUserIdFromToken(token).longValue() : 0L;
    }
}