package com.example.demo.service.tags;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.IdentityTags;
import com.example.demo.entity.LocationTag;
import com.example.demo.entity.ProductTag;
import com.example.demo.entity.TopicTag;

import java.util.List;
import java.util.Map;

/**
 * 标签管理服务接口
 * 基于 identity_tags、product_tag、topic_tag、location_tag 四大核心表
 */
public interface TagManagementService {
    
    // ==================== 身份标签管理 ====================
    
    /**
     * 分页查询身份标签
     */
    ApiResult getIdentityTags(Integer pageNum, Integer pageSize, String keyword, Boolean isActive);
    
    /**
     * 创建身份标签
     */
    ApiResult createIdentityTag(IdentityTags tag);
    
    /**
     * 更新身份标签
     */
    ApiResult updateIdentityTag(IdentityTags tag);
    
    /**
     * 删除身份标签
     */
    ApiResult deleteIdentityTag(Integer id);
    
    /**
     * 批量删除身份标签
     */
    ApiResult batchDeleteIdentityTags(List<Integer> ids);
    
    /**
     * 更新身份标签状态
     */
    ApiResult updateIdentityTagStatus(Integer id, Boolean isActive);
    
    // ==================== 话题标签管理（二级标签）====================
    
    /**
     * 分页查询话题标签
     */
    ApiResult getTopicTags(Integer pageNum, Integer pageSize, String keyword, Boolean isActive, String type);
    
    /**
     * 创建话题标签
     */
    ApiResult createTopicTag(TopicTag tag);
    
    /**
     * 更新话题标签
     */
    ApiResult updateTopicTag(TopicTag tag);
    
    /**
     * 删除话题标签
     */
    ApiResult deleteTopicTag(Integer id);
    
    /**
     * 批量删除话题标签
     */
    ApiResult batchDeleteTopicTags(List<Integer> ids);
    
    /**
     * 更新话题标签状态
     */
    ApiResult updateTopicTagStatus(Integer id, Boolean isActive);
    
    // ==================== 地点标签管理（三级标签）====================
    
    /**
     * 分页查询地点标签
     */
    ApiResult getLocationTags(Integer pageNum, Integer pageSize, String keyword, Boolean isActive, String locationType);
    
    /**
     * 创建地点标签
     */
    ApiResult createLocationTag(LocationTag tag);
    
    /**
     * 更新地点标签
     */
    ApiResult updateLocationTag(LocationTag tag);
    
    /**
     * 删除地点标签
     */
    ApiResult deleteLocationTag(Integer id);
    
    /**
     * 批量删除地点标签
     */
    ApiResult batchDeleteLocationTags(List<Integer> ids);
    
    /**
     * 更新地点标签状态
     */
    ApiResult updateLocationTagStatus(Integer id, Boolean isActive);
    
    // ==================== 商品标签管理（四级、五级标签）====================
    
    /**
     * 分页查询商品标签
     */
    ApiResult getProductTags(Integer pageNum, Integer pageSize, String keyword, String status, String category, String type);
    
    /**
     * 创建商品标签
     */
    ApiResult createProductTag(ProductTag tag);
    
    /**
     * 更新商品标签
     */
    ApiResult updateProductTag(ProductTag tag);
    
    /**
     * 删除商品标签
     */
    ApiResult deleteProductTag(Integer id);
    
    /**
     * 批量删除商品标签
     */
    ApiResult batchDeleteProductTags(List<Integer> ids);
    
    /**
     * 更新商品标签状态
     */
    ApiResult updateProductTagStatus(Integer id, String status);
}
