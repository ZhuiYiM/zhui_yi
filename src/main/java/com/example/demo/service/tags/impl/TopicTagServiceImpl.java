package com.example.demo.service.tags.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.dto.CustomTagSubmitRequest;
import com.example.demo.entity.TopicTag;
import com.example.demo.mapper.TopicTagMapper;
import com.example.demo.service.tags.TopicTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 话题标签服务实现类
 */
@Service
public class TopicTagServiceImpl extends ServiceImpl<TopicTagMapper, TopicTag> implements TopicTagService {
    
    @Override
    public ApiResult getAllTopicTags() {
        try {
            QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true)
                   .and(w -> w.eq("type", "system").or().eq("status", "active"));
            wrapper.orderByDesc("usage_count");
            
            List<TopicTag> tags = this.list(wrapper);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error(500, "获取话题标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult createCustomTopicTag(String tagName, Long userId) {
        try {
            QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
            wrapper.eq("name", tagName);
            TopicTag existing = this.getOne(wrapper);
            
            if (existing != null) {
                return ApiResult.error(400, "标签已存在");
            }
            
            TopicTag tag = new TopicTag();
            tag.setName(tagName);
            tag.setType("custom");
            tag.setStatus("pending");
            tag.setCreatedBy(userId);
            tag.setUsageCount(0L);
            tag.setTrendScore(0.0);
            tag.setIsActive(true);
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            
            this.save(tag);
            
            Map<String, Object> result = new HashMap<>();
            result.put("id", tag.getId());
            result.put("name", tagName);
            result.put("status", "pending");
            result.put("message", "标签创建成功，等待管理员审核");
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(500, "创建标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult increaseUsage(List<String> tagCodes) {
        try {
            if (tagCodes == null || tagCodes.isEmpty()) {
                return ApiResult.success();
            }
            
            for (String code : tagCodes) {
                QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
                wrapper.eq("name", code);
                TopicTag tag = this.getOne(wrapper);
                
                if (tag != null) {
                    tag.setUsageCount(tag.getUsageCount() + 1);
                    tag.setLastUsedAt(LocalDateTime.now());
                    tag.setTrendScore(tag.getUsageCount() * 0.1);
                    this.updateById(tag);
                }
            }
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error(500, "增加使用次数失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getHotTopicTags(Integer limit) {
        try {
            QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true)
                   .and(w -> w.eq("type", "system").or().eq("status", "active"));
            wrapper.orderByDesc("trend_score");
            wrapper.last("LIMIT " + (limit != null ? limit : 10));
            
            List<TopicTag> tags = this.list(wrapper);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error(500, "获取热门标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult auditCustomTag(Integer tagId, String status) {
        try {
            TopicTag tag = this.getById(tagId);
            if (tag == null) {
                return ApiResult.error(404, "标签不存在");
            }
            
            if (!"custom".equals(tag.getType())) {
                return ApiResult.error(400, "只能审核用户自定义标签");
            }
            
            tag.setStatus(status);
            if ("active".equals(status)) {
                tag.setIsActive(true);
            } else {
                tag.setIsActive(false);
            }
            tag.setUpdatedAt(LocalDateTime.now());
            
            this.updateById(tag);
            
            return ApiResult.success("审核成功");
        } catch (Exception e) {
            return ApiResult.error(500, "审核失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult convertToSystemTag(Integer tagId) {
        try {
            TopicTag tag = this.getById(tagId);
            if (tag == null) {
                return ApiResult.error(404, "标签不存在");
            }
            
            tag.setType("system");
            tag.setStatus("active");
            tag.setUpdatedAt(LocalDateTime.now());
            
            this.updateById(tag);
            
            return ApiResult.success("已转为系统标签");
        } catch (Exception e) {
            return ApiResult.error(500, "转换失败：" + e.getMessage());
        }
    }
    
    // ========== 以下方法从 TagService 迁移而来，用于支持原 tag_level_4 功能 ==========
    
    @Override
    public ApiResult getCustomTopicTags(String keyword, Integer page, Integer size) {
        try {
            // 从 topic_tag 表获取自定义标签（type=custom）
            Page<TopicTag> tagPage = new Page<>(page, size);
            QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("name", keyword);
            }
            
            wrapper.eq("type", "custom")
                   .eq("status", "active")
                   .orderByDesc("usage_count")
                   .orderByDesc("trend_score");
            
            Page<TopicTag> result = this.page(tagPage, wrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", result.getRecords());
            response.put("total", result.getTotal());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("totalPages", (result.getTotal() + size - 1) / size);
            
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.error("获取自定义标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult createOrGetCustomTags(List<String> tagNames) {
        try {
            if (tagNames == null || tagNames.isEmpty()) {
                return ApiResult.error("标签名称不能为空");
            }
            
            List<TopicTag> resultTags = new ArrayList<>();
            
            // 查询已存在的标签
            for (String name : tagNames) {
                QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
                wrapper.eq("name", name).eq("type", "custom");
                TopicTag existingTag = this.getOne(wrapper);
                
                if (existingTag == null) {
                    // 创建新标签
                    TopicTag newTag = new TopicTag();
                    newTag.setName(name);
                    newTag.setType("custom");
                    newTag.setStatus("pending");  // 自定义标签需要审核
                    newTag.setUsageCount(0L);
                    newTag.setTrendScore(0.0);
                    this.save(newTag);
                    resultTags.add(newTag);
                } else {
                    resultTags.add(existingTag);
                }
            }
            
            return ApiResult.success(resultTags);
        } catch (Exception e) {
            return ApiResult.error("创建标签失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getCustomTopicTagsAdmin(Integer pageNum, Integer pageSize, String keyword, String status) {
        try {
            // 查询自定义标签（type=custom）
            Page<TopicTag> page = new Page<>(pageNum, pageSize);
            QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
            wrapper.eq("type", "custom");
            
            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like("name", keyword);
            }
            
            // 状态筛选
            if (status != null && !status.trim().isEmpty()) {
                wrapper.eq("status", status);
            }
            
            // 排序
            wrapper.orderByDesc("usage_count").orderByDesc("trend_score").orderByDesc("created_at");
            
            Page<TopicTag> resultPage = this.page(page, wrapper);
            
            Map<String, Object> response = new HashMap<>();
            response.put("records", resultPage.getRecords());
            response.put("total", resultPage.getTotal());
            response.put("currentPage", pageNum);
            response.put("pageSize", pageSize);
            
            return ApiResult.success(response);
        } catch (Exception e) {
            return ApiResult.error("查询自定义标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult updateCustomTagStatus(Long id, String status) {
        try {
            TopicTag existing = this.getById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            // 确保是自定义标签
            if (!"custom".equals(existing.getType())) {
                return ApiResult.error("只能更新自定义标签的状态");
            }
            
            existing.setStatus(status);
            existing.setUpdatedAt(LocalDateTime.now());
            
            this.updateById(existing);
            
            return ApiResult.success(existing);
        } catch (Exception e) {
            return ApiResult.error("更新状态失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult updateCustomTag(Long id, String name, String icon, String color, Integer sortOrder, String status) {
        try {
            TopicTag existing = this.getById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            // 确保是自定义标签
            if (!"custom".equals(existing.getType())) {
                return ApiResult.error("只能更新自定义标签");
            }
            
            // 更新字段
            if (name != null) {
                existing.setName(name);
            }
            if (status != null) {
                existing.setStatus(status);
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
            existing.setUpdatedAt(LocalDateTime.now());
            
            this.updateById(existing);
            
            return ApiResult.success(existing);
        } catch (Exception e) {
            return ApiResult.error("更新标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult deleteCustomTag(Long id) {
        try {
            TopicTag existing = this.getById(id);
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            // 确保是自定义标签
            if (!"custom".equals(existing.getType())) {
                return ApiResult.error("只能删除自定义标签");
            }
            
            this.removeById(id);
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("删除标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult batchDeleteCustomTags(String ids) {
        try {
            if (ids == null || ids.trim().isEmpty()) {
                return ApiResult.error("请指定要删除的标签 ID");
            }
            
            List<Long> idList = Arrays.stream(ids.split(","))
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            
            for (Long id : idList) {
                TopicTag tag = this.getById(id);
                if (tag != null && "custom".equals(tag.getType())) {
                    this.removeById(id);
                }
            }
            
            return ApiResult.success();
        } catch (Exception e) {
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult submitCustomTag(CustomTagSubmitRequest request, Long userId) {
        try {
            // 检查标签是否已存在
            QueryWrapper<TopicTag> wrapper = new QueryWrapper<>();
            wrapper.eq("name", request.getName().trim());
            TopicTag existing = this.getOne(wrapper);
            
            if (existing != null) {
                if ("system".equals(existing.getType())) {
                    return ApiResult.error(400, "该标签已存在，为系统标签");
                } else if ("custom".equals(existing.getType())) {
                    if ("active".equals(existing.getStatus())) {
                        return ApiResult.error(400, "该标签已存在，请耐心等待审核");
                    } else if ("pending".equals(existing.getStatus()) && 
                               existing.getCreatedBy().equals(userId)) {
                        return ApiResult.error(400, "您已提交过该标签，请耐心等待审核");
                    }
                }
            }
            
            // 创建新的待审核标签
            TopicTag tag = new TopicTag();
            tag.setName(request.getName().trim());
            tag.setType("custom");
            tag.setStatus("pending");
            tag.setDescription(request.getDescription());
            tag.setCreatedBy(userId);
            tag.setUsageCount(0L);
            tag.setTrendScore(0.0);
            tag.setIsActive(false);
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            
            this.save(tag);
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("id", tag.getId());
            result.put("name", tag.getName());
            result.put("status", "pending");
            result.put("message", "标签提交成功，等待管理员审核");
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(500, "提交失败：" + e.getMessage());
        }
    }
}
