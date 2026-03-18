package com.example.demo.service;

import com.example.demo.common.Result;

/**
 * 搜索服务接口
 * 提供全站搜索功能
 */
public interface SearchService {
    
    /**
     * 综合搜索所有类型
     * @param query 搜索关键词
     * @param type 搜索类型
     * @param tags 标签（逗号分隔）
     * @param quickFilters 快速筛选
     * @return 搜索结果
     */
    Result searchAll(String query, String type, String tags, String quickFilters);
    
    /**
     * 搜索地点
     * @param query 搜索关键词
     * @param campusId 校区 ID
     * @param tags 标签
     * @return 搜索结果
     */
    Result searchLocations(String query, Integer campusId, String tags);
    
    /**
     * 搜索商品
     * @param query 搜索关键词
     * @param tags 标签
     * @return 搜索结果
     */
    Result searchProducts(String query, String tags);
    
    /**
     * 搜索话题
     * @param query 搜索关键词
     * @param tags 标签
     * @return 搜索结果
     */
    Result searchTopics(String query, String tags);
    
    /**
     * 搜索用户
     * @param query 搜索关键词
     * @return 搜索结果
     */
    Result searchUsers(String query);
}
