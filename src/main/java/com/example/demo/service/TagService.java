// service/TagService.java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.TagLevel1;
import com.example.demo.entity.TagLevel2;
import com.example.demo.entity.TagLevel3;
import com.example.demo.entity.TagLevel4;

import java.util.List;

/**
 * 标签服务接口
 */
public interface TagService extends IService<TagLevel4> {
    
    /**
     * 获取所有一级标签
     */
    ApiResult getLevel1Tags();
    
    /**
     * 获取所有二级标签
     */
    ApiResult getLevel2Tags();
    
    /**
     * 获取所有三级标签
     */
    ApiResult getLevel3Tags();
    
    /**
     * 获取四级标签列表（支持搜索和分页）
     */
    ApiResult getLevel4Tags(String keyword, Integer page, Integer size);
    
    /**
     * 获取热门标签
     */
    ApiResult getHotTags(Integer limit);
    
    /**
     * 创建或获取四级标签
     */
    ApiResult createOrGetLevel4Tags(List<String> tagNames);
    
    /**
     * 获取热门标签组合
     */
    ApiResult getHotTagsCombo();
}
