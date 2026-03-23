package com.example.demo.service.topic;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.dto.TopicCreateDTO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 话题创建服务接口
 */
public interface TopicCreateService {
    
    /**
     * 创建话题
     * @param topicDTO 话题创建 DTO
     * @param request HTTP 请求
     * @return API 结果
     */
    ApiResult createTopic(TopicCreateDTO topicDTO, HttpServletRequest request);
}
