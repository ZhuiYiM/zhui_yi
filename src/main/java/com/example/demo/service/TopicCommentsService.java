package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.TopicComments;
import com.example.demo.entity.dto.CommentCreateDTO;
import com.example.demo.common.ApiResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 话题评论服务接口
 */
public interface TopicCommentsService extends IService<TopicComments> {
    
    /**
     * 创建评论
     */
    ApiResult createComment(Long topicId, CommentCreateDTO commentDTO, HttpServletRequest request);
    
    /**
     * 获取话题评论列表
     */
    ApiResult getTopicComments(Long topicId, Integer page, Integer size, String sort);
    
    /**
     * 删除评论
     */
    ApiResult deleteComment(Long commentId, Long userId);
    
    /**
     * 点赞评论
     */
    ApiResult likeComment(Long commentId, String action, HttpServletRequest request);
}