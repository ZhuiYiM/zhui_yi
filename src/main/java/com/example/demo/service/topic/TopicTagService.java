package com.example.demo.service.topic;

import com.example.demo.common.ApiResult;
import java.util.List;

/**
 * 话题标签服务接口
 */
public interface TopicTagService {
    
    /**
     * 为话题绑定标签
     * @param topicId 话题 ID
     * @param tagCodes 标签代码列表
     * @return API 结果
     */
    ApiResult bindTags(Integer topicId, List<String> tagCodes);
    
    /**
     * 获取话题的标签列表
     * @param topicId 话题 ID
     * @return API 结果
     */
    ApiResult getTopicTags(Integer topicId);
}
