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
}
