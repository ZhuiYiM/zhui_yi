package com.example.demo.service.tags;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.LocationTag;

import java.util.List;

/**
 * 地点标签服务接口
 */
public interface LocationTagService extends IService<LocationTag> {
    
    /**
     * 获取所有地点标签（系统 + 用户自定义，已审核通过）
     */
    ApiResult getAllLocationTags();
    
    /**
     * 创建用户自定义地点标签
     */
    ApiResult createCustomLocationTag(String tagName, Long userId);
    
    /**
     * 增加标签使用次数
     */
    ApiResult increaseUsage(List<String> tagCodes);
    
    /**
     * 获取热门地点标签
     */
    ApiResult getHotLocationTags(Integer limit);
    
    /**
     * 审核用户自定义标签
     */
    ApiResult auditCustomTag(Integer tagId, String status);
    
    /**
     * 将用户自定义标签转为系统标签
     */
    ApiResult convertToSystemTag(Integer tagId);
}
