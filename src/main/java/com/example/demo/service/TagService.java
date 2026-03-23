// service/TagService.java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.TagLevel1;
import com.example.demo.entity.TagLevel2;
import com.example.demo.entity.TagLevel3;
import com.example.demo.entity.TagLevel4;
import com.example.demo.entity.TagLevel5;

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
    
    // ==================== 管理员功能 ====================
    
    /**
     * 分页查询二级标签
     */
    ApiResult getLevel2TagsAdmin(Integer pageNum, Integer pageSize, String keyword, Boolean isActive);
    
    /**
     * 创建二级标签
     */
    ApiResult createLevel2Tag(String code, String name, String icon, String color, Integer sortOrder);
    
    /**
     * 更新二级标签
     */
    ApiResult updateLevel2Tag(Integer id, String name, String icon, String color, Integer sortOrder, Boolean isActive);
    
    /**
     * 删除二级标签
     */
    ApiResult deleteLevel2Tag(Integer id);
    
    /**
     * 批量删除二级标签
     */
    ApiResult batchDeleteLevel2Tags(String ids);
    
    /**
     * 更新二级标签状态
     */
    ApiResult updateLevel2TagStatus(Integer id, Boolean isActive);
    
    // ==================== 三级标签管理 ====================
    
    /**
     * 分页查询三级标签
     */
    ApiResult getLevel3TagsAdmin(Integer pageNum, Integer pageSize, String keyword, Boolean isActive, String locationType);
    
    /**
     * 创建三级标签
     */
    ApiResult createLevel3Tag(String code, String name, String locationType, String icon, String color, 
                              String address, Double latitude, Double longitude, Integer sortOrder);
    
    /**
     * 更新三级标签
     */
    ApiResult updateLevel3Tag(Integer id, String name, String locationType, String icon, String color,
                              String address, Double latitude, Double longitude, Integer sortOrder, Boolean isActive);
    
    /**
     * 删除三级标签
     */
    ApiResult deleteLevel3Tag(Integer id);
    
    /**
     * 批量删除三级标签
     */
    ApiResult batchDeleteLevel3Tags(String ids);
    
    /**
     * 更新三级标签状态
     */
    ApiResult updateLevel3TagStatus(Integer id, Boolean isActive);
    
    // ==================== 四级标签管理 ====================
    
    /**
     * 分页查询四级标签
     */
    ApiResult getLevel4TagsAdmin(Integer pageNum, Integer pageSize, String keyword, String status, String category);
    
    /**
     * 更新四级标签
     */
    ApiResult updateLevel4Tag(Long id, String name, String icon, String color, Integer sortOrder, String status);
    
    /**
     * 更新四级标签状态
     */
    ApiResult updateLevel4TagStatus(Long id, String status);
    
    /**
     * 删除四级标签
     */
    ApiResult deleteLevel4Tag(Long id);
    
    /**
     * 批量删除四级标签
     */
    ApiResult batchDeleteLevel4Tags(String ids);
    
    // ==================== 五级标签管理（商业标签） ====================
    
    /**
     * 获取所有五级标签列表（前端使用）
     */
    ApiResult getLevel5Tags();
    
    /**
     * 分页查询五级标签（管理员）
     */
    ApiResult getLevel5TagsAdmin(Integer pageNum, Integer pageSize, String keyword, String status, String category);
    
    /**
     * 创建五级标签
     */
    ApiResult createLevel5Tag(String code, String name, String category, String icon, String color, Integer sortOrder);
    
    /**
     * 更新五级标签
     */
    ApiResult updateLevel5Tag(Long id, String name, String category, String icon, String color, 
                              Integer sortOrder, Boolean isActive, String status);
    
    /**
     * 删除五级标签
     */
    ApiResult deleteLevel5Tag(Long id);
    
    /**
     * 批量删除五级标签
     */
    ApiResult batchDeleteLevel5Tags(String ids);
    
    /**
     * 更新五级标签状态
     */
    ApiResult updateLevel5TagStatus(Long id, Boolean isActive, String status);
}
