package com.example.demo.service.tags;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.ApiResult;
import com.example.demo.dto.CustomTagSubmitRequest;
import com.example.demo.entity.ProductTag;

import java.util.List;

/**
 * 商品标签服务接口
 */
public interface ProductTagService extends IService<ProductTag> {
    
    /**
     * 获取所有商品标签（系统 + 用户自定义，已审核通过）
     */
    ApiResult getAllProductTags();
    
    /**
     * 创建用户自定义商品标签
     */
    ApiResult createCustomProductTag(String tagName, Long userId);
    
    /**
     * 用户提交自定义标签（待审核）
     */
    ApiResult submitCustomTag(CustomTagSubmitRequest request, Long userId);
    
    /**
     * 增加标签使用次数
     */
    ApiResult increaseUsage(List<String> tagCodes);
    
    /**
     * 获取热门商品标签
     */
    ApiResult getHotProductTags(Integer limit);
    
    /**
     * 审核用户自定义标签
     */
    ApiResult auditCustomTag(Integer tagId, String status);
    
    /**
     * 将用户自定义标签转为系统标签
     */
    ApiResult convertToSystemTag(Integer tagId);
}
