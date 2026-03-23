package com.example.demo.service.topic;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.dto.TopicQueryDTO;

/**
 * 话题查询服务接口
 */
public interface TopicQueryService {
    
    /**
     * 分页查询话题列表
     * @param queryDTO 查询条件
     * @return API 结果
     */
    ApiResult getTopics(TopicQueryDTO queryDTO);
    
    /**
     * 获取话题详情
     * @param topicId 话题 ID
     * @return API 结果
     */
    ApiResult getTopicDetail(Integer topicId);
}
