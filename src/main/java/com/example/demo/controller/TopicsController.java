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
                           /*@Valid*/ /*@RequestBody*/ TopicLikeDTO likeDTO,
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
                               /*@Valid*/ /*@RequestBody*/ CommentCreateDTO commentDTO,
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

    // 辅助方法：从请求中提取用户ID
    private Long extractUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Integer userIdInt = jwtUtil.getUserIdFromToken(token);
        if (userIdInt != null) {
            return userIdInt.longValue();
        } else {
            return 0L;
        }
    }
}