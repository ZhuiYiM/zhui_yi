package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Topics;
import com.example.demo.entity.dto.TopicCreateDTO;
import com.example.demo.entity.dto.TopicLikeDTO;
import com.example.demo.entity.dto.TopicQueryDTO;
import com.example.demo.common.ApiResult;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 话题服务接口
 */
public interface TopicsService extends IService<Topics> {
    
    /**
     * 创建话题
     */
    ApiResult createTopic(TopicCreateDTO topicDTO, HttpServletRequest request);
    
    /**
     * 获取话题列表
     */
    ApiResult getTopics(TopicQueryDTO queryDTO);
    
    /**
     * 获取话题详情
     */
    ApiResult getTopicDetail(Long topicId, Long userId);
    
    /**
     * 点赞话题
     */
    ApiResult likeTopic(Long topicId, TopicLikeDTO likeDTO, HttpServletRequest request);
    
    /**
     * 删除话题
     */
    ApiResult deleteTopic(Long topicId, Long userId);
    
    /**
     * 更新话题
     */
    ApiResult updateTopic(Long topicId, TopicCreateDTO topicDTO, Long userId);
    
    /**
     * 获取热门标签
     */
    ApiResult getPopularTags();
    
    /**
     * 搜索标签
     */
    ApiResult searchTags(String keyword);
    
    /**
     * 获取社区统计
     */
    ApiResult getCommunityStats();
    
    /**
     * 置顶/取消置顶话题
     */
    ApiResult toggleTopicTop(Long topicId, Boolean isTop, Long userId);
    
    /**
     * 设置/取消精华话题
     */
    ApiResult toggleTopicEssence(Long topicId, Boolean isEssence, Long userId);
    
    /**
     * 收藏/取消收藏话题
     */
    ApiResult toggleTopicCollect(Long topicId, Long userId);
    
    /**
     * 获取用户收藏的话题列表
     */
    ApiResult getUserCollections(Long userId, Integer page, Integer size);
    
    /**
     * 根据多级标签筛选话题
     */
    ApiResult filterTopics(TopicQueryDTO queryDTO);
}