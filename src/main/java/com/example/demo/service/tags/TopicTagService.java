package com.example.demo.service.tags;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.ApiResult;
import com.example.demo.dto.CustomTagSubmitRequest;
import com.example.demo.entity.TopicTag;

import java.util.List;

/**
 * 话题标签服务接口
 */
public interface TopicTagService extends IService<TopicTag> {
    
    /**
     * 获取所有话题标签（系统 + 用户自定义，已审核通过）
     */
    ApiResult getAllTopicTags();
    
    /**
     * 创建用户自定义话题标签
     */
    ApiResult createCustomTopicTag(String tagName, Long userId);
    
    /**
     * 增加标签使用次数
     */
    ApiResult increaseUsage(List<String> tagCodes);
    
    /**
     * 获取热门话题标签
     */
    ApiResult getHotTopicTags(Integer limit);
    
    /**
     * 审核用户自定义标签
     */
    ApiResult auditCustomTag(Integer tagId, String status);
    
    /**
     * 将用户自定义标签转为系统标签
     */
    ApiResult convertToSystemTag(Integer tagId);
    
    // ========== 以下方法从 TagService 迁移而来，用于支持原 tag_level_4 功能 ==========
    
    /**
     * 获取自定义标签列表（支持搜索和分页）
     */
    ApiResult getCustomTopicTags(String keyword, Integer page, Integer size);
    
    /**
     * 创建或获取自定义标签
     */
    ApiResult createOrGetCustomTags(List<String> tagNames);
    
    /**
     * 分页查询自定义标签（管理员）
     */
    ApiResult getCustomTopicTagsAdmin(Integer pageNum, Integer pageSize, String keyword, String status);
    
    /**
     * 更新自定义标签状态
     */
    ApiResult updateCustomTagStatus(Long id, String status);
    
    /**
     * 更新自定义标签
     */
    ApiResult updateCustomTag(Long id, String name, String icon, String color, Integer sortOrder, String status);
    
    /**
     * 删除自定义标签
     */
    ApiResult deleteCustomTag(Long id);
    
    /**
     * 批量删除自定义标签
     */
    ApiResult batchDeleteCustomTags(String ids);
    
    /**
     * 用户提交自定义标签（待审核）
     */
    ApiResult submitCustomTag(CustomTagSubmitRequest request, Long userId);
}
