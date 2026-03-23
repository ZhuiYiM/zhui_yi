// service/impl/TagServiceImpl.java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.TagLevel1;
import com.example.demo.entity.TagLevel2;
import com.example.demo.entity.TagLevel3;
import com.example.demo.entity.TagLevel4;
import com.example.demo.mapper.TagLevel1Mapper;
import com.example.demo.mapper.TagLevel2Mapper;
import com.example.demo.mapper.TagLevel3Mapper;
import com.example.demo.mapper.TagLevel4Mapper;
import com.example.demo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 标签服务实现类
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagLevel4Mapper, TagLevel4> implements TagService {
    
    @Autowired
    private TagLevel1Mapper tagLevel1Mapper;
    
    @Autowired
    private TagLevel2Mapper tagLevel2Mapper;
    
    @Autowired
    private TagLevel3Mapper tagLevel3Mapper;
    
    @Autowired
    private TagLevel4Mapper tagLevel4Mapper;
    
    @Override
    public ApiResult getLevel1Tags() {
        try {
            QueryWrapper<TagLevel1> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true)  // 只返回启用的标签
                   .orderByAsc("sort_order");
            List<TagLevel1> tags = tagLevel1Mapper.selectList(wrapper);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error("获取一级标签失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getLevel2Tags() {
        try {
            QueryWrapper<TagLevel2> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true)
                   .orderByAsc("sort_order");
            List<TagLevel2> tags = tagLevel2Mapper.selectList(wrapper);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error("获取二级标签失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getLevel3Tags() {
        try {
            QueryWrapper<TagLevel3> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true)
                   .orderByAsc("sort_order");
            List<TagLevel3> tags = tagLevel3Mapper.selectList(wrapper);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error("获取三级标签失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getLevel4Tags(String keyword, Integer page, Integer size) {
        try {
            Page<TagLevel4> tagPage = new Page<>(page, size);
            QueryWrapper<TagLevel4> wrapper = new QueryWrapper<>();
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("name", keyword);
            }
            
            wrapper.eq("status", "active")
                   .orderByDesc("usage_count")
                   .orderByDesc("trend_score");
            
            Page<TagLevel4> result = tagLevel4Mapper.selectPage(tagPage, wrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", result.getRecords());
            response.put("total", result.getTotal());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalPages", (result.getTotal() + size - 1) / size);
            
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.error("获取四级标签失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getHotTags(Integer limit) {
        try {
            if (limit == null || limit <= 0) {
                limit = 20;
            }
            
            QueryWrapper<TagLevel4> wrapper = new QueryWrapper<>();
            wrapper.eq("status", "active")
                   .orderByDesc("trend_score")
                   .orderByDesc("usage_count")
                   .last("LIMIT " + limit);
            
            List<TagLevel4> hotTags = tagLevel4Mapper.selectList(wrapper);
            return ApiResult.success(hotTags);
        } catch (Exception e) {
            return ApiResult.error("获取热门标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult createOrGetLevel4Tags(List<String> tagNames) {
        try {
            if (tagNames == null || tagNames.isEmpty()) {
                return ApiResult.error("标签名称不能为空");
            }
            
            List<TagLevel4> resultTags = new ArrayList<>();
            
            // 查询已存在的标签
            List<TagLevel4> existingTags = tagLevel4Mapper.selectByNames(tagNames);
            Map<String, TagLevel4> existingMap = existingTags.stream()
                    .collect(Collectors.toMap(TagLevel4::getName, tag -> tag));
            
            // 找出需要新建的标签
            List<String> newTagNames = tagNames.stream()
                    .filter(name -> !existingMap.containsKey(name))
                    .collect(Collectors.toList());
            
            // 批量插入新标签
            if (!newTagNames.isEmpty()) {
                List<TagLevel4> newTags = new ArrayList<>();
                for (String name : newTagNames) {
                    TagLevel4 tag = new TagLevel4();
                    tag.setName(name);
                    tag.setStatus("active");
                    tag.setUsageCount(0L);
                    tag.setTrendScore(java.math.BigDecimal.ZERO);
                    tag.setCreatedAt(LocalDateTime.now());
                    tag.setUpdatedAt(LocalDateTime.now());
                    newTags.add(tag);
                }
                
                for (TagLevel4 tag : newTags) {
                    tagLevel4Mapper.insert(tag);
                    resultTags.add(tag);
                }
            }
            
            // 合并已存在和新创建的标签
            resultTags.addAll(existingTags);
            
            return ApiResult.success(resultTags);
        } catch (Exception e) {
            return ApiResult.error("创建标签失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getHotTagsCombo() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // 获取热门二级标签（按使用次数）
            QueryWrapper<TagLevel2> level2Wrapper = new QueryWrapper<>();
            level2Wrapper.eq("is_active", true)
                    .orderByDesc("usage_count")
                    .last("LIMIT 10");
            List<TagLevel2> hotLevel2Tags = tagLevel2Mapper.selectList(level2Wrapper);
            
            // 获取热门三级标签（按使用次数）
            QueryWrapper<TagLevel3> level3Wrapper = new QueryWrapper<>();
            level3Wrapper.eq("is_active", true)
                    .orderByDesc("usage_count")
                    .last("LIMIT 10");
            List<TagLevel3> hotLevel3Tags = tagLevel3Mapper.selectList(level3Wrapper);
            
            // 获取热门四级标签（按趋势分数和使用次数）
            QueryWrapper<TagLevel4> level4Wrapper = new QueryWrapper<>();
            level4Wrapper.eq("status", "active")
                    .orderByDesc("trend_score")
                    .orderByDesc("usage_count")
                    .last("LIMIT 10");
            List<TagLevel4> hotLevel4Tags = tagLevel4Mapper.selectList(level4Wrapper);
            
            result.put("level2", hotLevel2Tags);
            result.put("level3", hotLevel3Tags);
            result.put("level4", hotLevel4Tags);
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error("获取热门标签组合失败：" + e.getMessage());
        }
    }
    
    // ==================== 管理员功能实现 ====================
    
    @Override
    public ApiResult getLevel2TagsAdmin(Integer pageNum, Integer pageSize, String keyword, Boolean isActive) {
        try {
            Page<TagLevel2> page = new Page<>(pageNum, pageSize);
            QueryWrapper<TagLevel2> wrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("name", keyword).or().like("code", keyword));
            }
            
            // 状态筛选
            if (isActive != null) {
                wrapper.eq("is_active", isActive ? 1 : 0);
            }
            
            // 排序
            wrapper.orderByAsc("sort_order").orderByDesc("created_at");
            
            Page<TagLevel2> resultPage = tagLevel2Mapper.selectPage(page, wrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", resultPage.getRecords());
            response.put("total", resultPage.getTotal());
            response.put("currentPage", pageNum);
            response.put("pageSize", pageSize);
            
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.error("查询二级标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult createLevel2Tag(String code, String name, String icon, String color, Integer sortOrder) {
        try {
            // 参数校验
            if (code == null || code.trim().isEmpty()) {
                return ApiResult.error("标签代码不能为空");
            }
            if (name == null || name.trim().isEmpty()) {
                return ApiResult.error("标签名称不能为空");
            }
            
            // 检查代码是否已存在
            QueryWrapper<TagLevel2> wrapper = new QueryWrapper<>();
            wrapper.eq("code", code);
            TagLevel2 existing = tagLevel2Mapper.selectOne(wrapper);
            if (existing != null) {
                return ApiResult.error("标签代码已存在");
            }
            
            // 创建新标签
            TagLevel2 tag = new TagLevel2();
            tag.setCode(code);
            tag.setName(name);
            tag.setIcon(icon);
            tag.setColor(color);
            tag.setSortOrder(sortOrder != null ? sortOrder : 0);
            tag.setIsActive(true);
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            
            tagLevel2Mapper.insert(tag);
            
            return ApiResult.success(tag);
        } catch (Exception e) {
            return ApiResult.error("创建标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateLevel2Tag(Integer id, String name, String icon, String color, Integer sortOrder, Boolean isActive) {
        try {
            TagLevel2 existing = tagLevel2Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            // 更新字段
            if (name != null) {
                existing.setName(name);
            }
            if (icon != null) {
                existing.setIcon(icon);
            }
            if (color != null) {
                existing.setColor(color);
            }
            if (sortOrder != null) {
                existing.setSortOrder(sortOrder);
            }
            if (isActive != null) {
                existing.setIsActive(isActive);
            }
            existing.setUpdatedAt(LocalDateTime.now());
            
            tagLevel2Mapper.updateById(existing);
            
            return ApiResult.success(existing);
        } catch (Exception e) {
            return ApiResult.error("更新标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult deleteLevel2Tag(Integer id) {
        try {
            TagLevel2 existing = tagLevel2Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            tagLevel2Mapper.deleteById(id);
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("删除标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult batchDeleteLevel2Tags(String ids) {
        try {
            if (ids == null || ids.trim().isEmpty()) {
                return ApiResult.error("请指定要删除的标签 ID");
            }
            
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            
            for (Integer id : idList) {
                tagLevel2Mapper.deleteById(id);
            }
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateLevel2TagStatus(Integer id, Boolean isActive) {
        try {
            TagLevel2 existing = tagLevel2Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            existing.setIsActive(isActive);
            existing.setUpdatedAt(LocalDateTime.now());
            
            tagLevel2Mapper.updateById(existing);
            
            return ApiResult.success(existing);
        } catch (Exception e) {
            return ApiResult.error("更新状态失败：" + e.getMessage());
        }
    }
    
    // ==================== 三级标签管理实现 ====================
    
    @Override
    public ApiResult getLevel3TagsAdmin(Integer pageNum, Integer pageSize, String keyword, Boolean isActive, String locationType) {
        try {
            Page<TagLevel3> page = new Page<>(pageNum, pageSize);
            QueryWrapper<TagLevel3> wrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.and(w -> w.like("name", keyword).or().like("code", keyword));
            }
            
            // 状态筛选
            if (isActive != null) {
                wrapper.eq("is_active", isActive ? 1 : 0);
            }
            
            // 地点类型筛选
            if (locationType != null && !locationType.trim().isEmpty()) {
                wrapper.eq("location_type", locationType);
            }
            
            // 排序
            wrapper.orderByAsc("sort_order").orderByDesc("created_at");
            
            Page<TagLevel3> resultPage = tagLevel3Mapper.selectPage(page, wrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", resultPage.getRecords());
            response.put("total", resultPage.getTotal());
            response.put("currentPage", pageNum);
            response.put("pageSize", pageSize);
            
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.error("查询三级标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult createLevel3Tag(String code, String name, String locationType, String icon, String color,
                                      String address, Double latitude, Double longitude, Integer sortOrder) {
        try {
            // 参数校验
            if (code == null || code.trim().isEmpty()) {
                return ApiResult.error("标签代码不能为空");
            }
            if (name == null || name.trim().isEmpty()) {
                return ApiResult.error("标签名称不能为空");
            }
            
            // 检查代码是否已存在
            QueryWrapper<TagLevel3> wrapper = new QueryWrapper<>();
            wrapper.eq("code", code);
            TagLevel3 existing = tagLevel3Mapper.selectOne(wrapper);
            if (existing != null) {
                return ApiResult.error("标签代码已存在");
            }
            
            // 创建新标签
            TagLevel3 tag = new TagLevel3();
            tag.setCode(code);
            tag.setName(name);
            tag.setLocationType(locationType);
            tag.setIcon(icon);
            tag.setColor(color);
            tag.setAddress(address);
            if (latitude != null) {
                tag.setLatitude(new java.math.BigDecimal(latitude.toString()));
            }
            if (longitude != null) {
                tag.setLongitude(new java.math.BigDecimal(longitude.toString()));
            }
            tag.setSortOrder(sortOrder != null ? sortOrder : 0);
            tag.setIsActive(true);
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            
            tagLevel3Mapper.insert(tag);
            
            return ApiResult.success(tag);
        } catch (Exception e) {
            return ApiResult.error("创建标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateLevel3Tag(Integer id, String name, String locationType, String icon, String color,
                                      String address, Double latitude, Double longitude, Integer sortOrder, Boolean isActive) {
        try {
            TagLevel3 existing = tagLevel3Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            // 更新字段
            if (name != null) {
                existing.setName(name);
            }
            if (locationType != null) {
                existing.setLocationType(locationType);
            }
            if (icon != null) {
                existing.setIcon(icon);
            }
            if (color != null) {
                existing.setColor(color);
            }
            if (address != null) {
                existing.setAddress(address);
            }
            if (latitude != null) {
                existing.setLatitude(new java.math.BigDecimal(latitude.toString()));
            }
            if (longitude != null) {
                existing.setLongitude(new java.math.BigDecimal(longitude.toString()));
            }
            if (sortOrder != null) {
                existing.setSortOrder(sortOrder);
            }
            if (isActive != null) {
                existing.setIsActive(isActive);
            }
            existing.setUpdatedAt(LocalDateTime.now());
            
            tagLevel3Mapper.updateById(existing);
            
            return ApiResult.success(existing);
        } catch (Exception e) {
            return ApiResult.error("更新标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult deleteLevel3Tag(Integer id) {
        try {
            TagLevel3 existing = tagLevel3Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            tagLevel3Mapper.deleteById(id);
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("删除标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult batchDeleteLevel3Tags(String ids) {
        try {
            if (ids == null || ids.trim().isEmpty()) {
                return ApiResult.error("请指定要删除的标签 ID");
            }
            
            List<Integer> idList = Arrays.stream(ids.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            
            for (Integer id : idList) {
                tagLevel3Mapper.deleteById(id);
            }
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateLevel3TagStatus(Integer id, Boolean isActive) {
        try {
            TagLevel3 existing = tagLevel3Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            existing.setIsActive(isActive);
            existing.setUpdatedAt(LocalDateTime.now());
            
            tagLevel3Mapper.updateById(existing);
            
            return ApiResult.success(existing);
        } catch (Exception e) {
            return ApiResult.error("更新状态失败：" + e.getMessage());
        }
    }
    
    // ==================== 四级标签管理实现 ====================
    
    @Override
    public ApiResult getLevel4TagsAdmin(Integer pageNum, Integer pageSize, String keyword, String status, String category) {
        try {
            Page<TagLevel4> page = new Page<>(pageNum, pageSize);
            QueryWrapper<TagLevel4> wrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("name", keyword);
            }
            
            // 状态筛选
            if (status != null && !status.trim().isEmpty()) {
                wrapper.eq("status", status);
            }
            
            // 分类筛选
            if (category != null && !category.trim().isEmpty()) {
                wrapper.eq("category", category);
            }
            
            // 排序
            wrapper.orderByDesc("usage_count").orderByDesc("trend_score").orderByDesc("created_at");
            
            Page<TagLevel4> resultPage = tagLevel4Mapper.selectPage(page, wrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", resultPage.getRecords());
            response.put("total", resultPage.getTotal());
            response.put("currentPage", pageNum);
            response.put("pageSize", pageSize);
            
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.error("查询四级标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult updateLevel4TagStatus(Long id, String status) {
        try {
            TagLevel4 existing = tagLevel4Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            existing.setStatus(status);
            existing.setUpdatedAt(LocalDateTime.now());
            
            tagLevel4Mapper.updateById(existing);
            
            return ApiResult.success(existing);
        } catch (Exception e) {
            return ApiResult.error("更新状态失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult deleteLevel4Tag(Long id) {
        try {
            TagLevel4 existing = tagLevel4Mapper.selectById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            tagLevel4Mapper.deleteById(id);
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("删除标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult batchDeleteLevel4Tags(String ids) {
        try {
            if (ids == null || ids.trim().isEmpty()) {
                return ApiResult.error("请指定要删除的标签 ID");
            }
            
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            for (Long id : idList) {
                tagLevel4Mapper.deleteById(id);
            }
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
}
