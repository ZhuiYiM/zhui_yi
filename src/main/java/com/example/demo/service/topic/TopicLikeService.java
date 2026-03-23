package com.example.demo.service.topic;

import com.example.demo.common.ApiResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 话题点赞服务接口
 */
public interface TopicLikeService {
    
    /**
     * 切换点赞状态
     * @param topicId 话题 ID
     * @param request HTTP 请求
     * @return API 结果
     */
    ApiResult toggleLike(Integer topicId, HttpServletRequest request);
    
    /**
     * 获取点赞数量
     * @param topicId 话题 ID
     * @return API 结果
     */
    ApiResult getLikesCount(Integer topicId);
}
