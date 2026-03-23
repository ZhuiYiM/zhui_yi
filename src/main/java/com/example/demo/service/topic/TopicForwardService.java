package com.example.demo.service.topic;

import com.example.demo.common.ApiResult;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 话题转发服务接口
 */
public interface TopicForwardService {
    
    /**
     * 转发话题
     * @param topicId 原话题 ID
     * @param content 转发内容
     * @param request HTTP 请求
     * @return API 结果
     */
    ApiResult forwardTopic(Integer topicId, String content, HttpServletRequest request);
    
    /**
     * 获取转发后的话题详情
     * @param topicId 话题 ID
     * @return API 结果
     */
    ApiResult getForwardedTopicDetail(Integer topicId);
}
