package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.Like;
import com.example.demo.entity.TopicComments;
import com.example.demo.entity.User;
import com.example.demo.entity.dto.CommentCreateDTO;
import com.example.demo.mapper.LikeMapper;
import com.example.demo.mapper.TopicCommentsMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.TopicCommentsService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 话题评论服务实现类
 */
@Service
public class TopicCommentsServiceImpl extends ServiceImpl<TopicCommentsMapper, TopicComments> implements TopicCommentsService {

    @Autowired
    private TopicCommentsMapper topicCommentsMapper;
    
    @Autowired
    private LikeMapper likeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public ApiResult createComment(Long topicId, CommentCreateDTO commentDTO, HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromRequest(request);
            if (userId == null) {
                return ApiResult.error(401, "用户未登录");
            }
            
            // 验证评论内容
            if (commentDTO.getContent() == null || commentDTO.getContent().trim().isEmpty()) {
                return ApiResult.error(400, "评论内容不能为空");
            }
            
            if (commentDTO.getContent().length() > 500) {
                return ApiResult.error(400, "评论内容不能超过500字符");
            }
            
            // 验证回复层数（最多3层）
            if (commentDTO.getParentId() != null) {
                int replyDepth = getReplyDepth(commentDTO.getParentId());
                if (replyDepth >= 3) {
                    return ApiResult.error(400, "回复层数不能超过3层");
                }
            }
            
            // 创建评论
            TopicComments comment = new TopicComments();
            comment.setTopicId(topicId);
            comment.setUserId(userId);
            comment.setParentId(commentDTO.getParentId());
            comment.setContent(commentDTO.getContent());
            comment.setLikesCount(0);
            comment.setStatus(1);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            
            boolean result = this.save(comment);
            if (result) {
                return ApiResult.success("评论发表成功");
            } else {
                return ApiResult.error(500, "评论发表失败");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "评论发表失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult getTopicComments(Long topicId, Integer page, Integer size, String sort) {
        try {
            Page<TopicComments> commentPage = new Page<>(page, size);
            QueryWrapper<TopicComments> wrapper = new QueryWrapper<>();
            wrapper.eq("topic_id", topicId).eq("status", 1);
            
            // 排序方式
            if ("hot".equals(sort)) {
                wrapper.orderByDesc("likes_count");
            } else {
                wrapper.orderByAsc("created_at");
            }
            
            Page<TopicComments> result = this.page(commentPage, wrapper);
            
            // 补充用户信息
            List<Map<String, Object>> commentsWithUserInfo = new ArrayList<>();
            for (TopicComments comment : result.getRecords()) {
                Map<String, Object> commentInfo = new HashMap<>();
                commentInfo.put("id", comment.getId());
                commentInfo.put("content", comment.getContent());
                commentInfo.put("likesCount", comment.getLikesCount());
                commentInfo.put("createdAt", comment.getCreatedAt());
                commentInfo.put("parentId", comment.getParentId());
                
                User author = userMapper.selectById(comment.getUserId());
                if (author != null) {
                    Map<String, Object> authorInfo = new HashMap<>();
                    authorInfo.put("id", author.getId());
                    authorInfo.put("username", author.getUsername());
                    authorInfo.put("realName", author.getRealName());
                    authorInfo.put("avatarUrl", author.getAvatarUrl());
                    commentInfo.put("author", authorInfo);
                }
                
                commentsWithUserInfo.add(commentInfo);
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("comments", commentsWithUserInfo);
            responseData.put("total", result.getTotal());
            responseData.put("page", result.getCurrent());
            responseData.put("size", result.getSize());
            
            return ApiResult.success(responseData);
        } catch (Exception e) {
            return ApiResult.error(500, "获取评论列表失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResult deleteComment(Long commentId, Long userId) {
        try {
            TopicComments comment = this.getById(commentId);
            if (comment == null) {
                return ApiResult.error(404, "评论不存在");
            }
            
            if (!comment.getUserId().equals(userId)) {
                return ApiResult.error(403, "无权删除此评论");
            }
            
            comment.setStatus(0);
            comment.setUpdatedAt(LocalDateTime.now());
            boolean result = this.updateById(comment);
            
            if (result) {
                return ApiResult.success("评论删除成功");
            } else {
                return ApiResult.error(500, "评论删除失败");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "评论删除失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResult likeComment(Long commentId, String action, HttpServletRequest request) {
        try {
            Long userId = extractUserIdFromRequest(request);
            if (userId == null) {
                return ApiResult.error(401, "用户未登录");
            }
            
            TopicComments comment = this.getById(commentId);
            if (comment == null || comment.getStatus() != 1) {
                return ApiResult.error(404, "评论不存在");
            }
            
            QueryWrapper<Like> wrapper = new QueryWrapper<>();
            wrapper.eq("user_id", userId)
                   .eq("target_type", "comment")
                   .eq("target_id", commentId);
            
            if ("like".equals(action)) {
                // 点赞操作
                Like existingLike = likeMapper.selectOne(wrapper);
                if (existingLike != null) {
                    return ApiResult.error(409, "已点赞");
                }
                
                Like like = new Like();
                like.setUserId(userId.intValue());
                like.setTargetType("comment");
                like.setTargetId(commentId.intValue());
                like.setCreatedAt(LocalDateTime.now());
                likeMapper.insert(like);
                
                // 更新评论点赞数
                comment.setLikesCount(comment.getLikesCount() + 1);
                this.updateById(comment);
                
                Map<String, Object> result = new HashMap<>();
                result.put("likesCount", comment.getLikesCount());
                return ApiResult.success(result);
                
            } else if ("unlike".equals(action)) {
                // 取消点赞操作
                Like existingLike = likeMapper.selectOne(wrapper);
                if (existingLike == null) {
                    return ApiResult.error(409, "未点赞");
                }
                
                likeMapper.delete(wrapper);
                
                // 更新评论点赞数
                comment.setLikesCount(Math.max(0, comment.getLikesCount() - 1));
                this.updateById(comment);
                
                Map<String, Object> result = new HashMap<>();
                result.put("likesCount", comment.getLikesCount());
                return ApiResult.success(result);
            } else {
                return ApiResult.error(400, "无效的操作类型");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "评论点赞操作失败: " + e.getMessage());
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
    
    private int getReplyDepth(Long parentId) {
        if (parentId == null) {
            return 0;
        }
        
        TopicComments parentComment = this.getById(parentId.longValue());
        if (parentComment == null || parentComment.getParentId() == null) {
            return 1;
        }
        
        return 1 + getReplyDepth(parentComment.getParentId());
    }
}