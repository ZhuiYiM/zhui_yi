package com.example.demo.service.topic;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.dto.CommentCreateDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 话题评论服务接口
 */
public interface TopicCommentService {
    
    /**
     * 添加评论
     * @param dto 评论创建 DTO
     * @param request HTTP 请求
     * @return API 结果
     */
    ApiResult addComment(CommentCreateDTO dto, HttpServletRequest request);
    
    /**
     * 分页获取评论列表
     * @param topicId 话题 ID
     * @param page 页码
     * @param size 每页数量
     * @return API 结果
     */
    ApiResult getComments(Integer topicId, Integer page, Integer size);
}
