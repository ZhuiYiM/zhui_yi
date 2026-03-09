package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.dto.CommentCreateDTO;
import com.example.demo.service.TopicCommentsService;
import com.example.demo.service.TopicsService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 话题详情控制器（兼容单数路径 /api/topic/{id}）
 */
@RestController
@RequestMapping("/api/topic")
@CrossOrigin
public class TopicController {

    @Autowired
    private TopicsService topicsService;
    
    @Autowired
    private TopicCommentsService topicCommentsService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 获取话题详情
     * GET /api/topic/{id}
     */
    @GetMapping("/{id}")
    public ApiResult getTopicDetail(@PathVariable Long id,
                                @RequestParam(required = false) Long userId) {
        return topicsService.getTopicDetail(id, userId);
    }

    /**
     * 点赞话题
     * POST /api/topic/{id}/like
     */
    @PostMapping("/{id}/like")
    public ApiResult likeTopic(@PathVariable Long id,
                           @RequestParam String action,
                           HttpServletRequest request) {
        // 创建 TopicLikeDTO 并设置 action
        com.example.demo.entity.dto.TopicLikeDTO likeDTO = new com.example.demo.entity.dto.TopicLikeDTO();
        likeDTO.setAction(action);
        return topicsService.likeTopic(id, likeDTO, request);
    }

    /**
     * 收藏话题
     * POST /api/topic/{id}/collect
     */
    @PostMapping("/{id}/collect")
    public ApiResult collectTopic(@PathVariable Long id,
                                  HttpServletRequest request) {
        Long userId = extractUserIdFromRequest(request);
        if (userId == null || userId == 0L) {
            return ApiResult.error(401, "用户未登录");
        }
        return topicsService.toggleTopicCollect(id, userId);
    }

    /**
     * 获取话题评论列表
     * GET /api/topic/{id}/comments
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
     * POST /api/topic/{id}/comment
     */
    @PostMapping("/{id}/comment")
    public ApiResult createComment(@PathVariable Long id,
                               @RequestBody CommentCreateDTO commentDTO,
                               HttpServletRequest request) {
        return topicCommentsService.createComment(id, commentDTO, request);
    }

    /**
     * 点赞评论
     * POST /api/topic/comments/{commentId}/like
     */
    @PostMapping("/comments/{commentId}/like")
    public ApiResult likeComment(@PathVariable Long commentId,
                             @RequestParam String action,
                             HttpServletRequest request) {
        return topicCommentsService.likeComment(commentId, action, request);
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
