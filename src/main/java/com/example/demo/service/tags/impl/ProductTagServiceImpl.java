package com.example.demo.service.tags.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.dto.CustomTagSubmitRequest;
import com.example.demo.entity.ProductTag;
import com.example.demo.mapper.ProductTagMapper;
import com.example.demo.service.tags.ProductTagService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品标签服务实现类
 */
@Service
public class ProductTagServiceImpl extends ServiceImpl<ProductTagMapper, ProductTag> implements ProductTagService {
    
    @Override
    public ApiResult getAllProductTags() {
        try {
            QueryWrapper<ProductTag> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true)
                   .and(w -> w.eq("type", "system").or().eq("status", "active"));
            wrapper.orderByDesc("usage_count");
            
            List<ProductTag> tags = this.list(wrapper);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error(500, "获取商品标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult createCustomProductTag(String tagName, Long userId) {
        try {
            QueryWrapper<ProductTag> wrapper = new QueryWrapper<>();
            wrapper.eq("name", tagName);
            ProductTag existing = this.getOne(wrapper);
            
            if (existing != null) {
                return ApiResult.error(400, "标签已存在");
            }
            
            ProductTag tag = new ProductTag();
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
    public ApiResult submitCustomTag(CustomTagSubmitRequest request, Long userId) {
        try {
            // 检查标签是否已存在
            QueryWrapper<ProductTag> wrapper = new QueryWrapper<>();
            wrapper.eq("name", request.getName().trim());
            ProductTag existing = this.getOne(wrapper);
            
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
            ProductTag tag = new ProductTag();
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
    
    @Override
    @Transactional
    public ApiResult increaseUsage(List<String> tagCodes) {
        try {
            if (tagCodes == null || tagCodes.isEmpty()) {
                return ApiResult.success();
            }
            
            for (String code : tagCodes) {
                QueryWrapper<ProductTag> wrapper = new QueryWrapper<>();
                wrapper.eq("name", code);
                ProductTag tag = this.getOne(wrapper);
                
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
    public ApiResult getHotProductTags(Integer limit) {
        try {
            QueryWrapper<ProductTag> wrapper = new QueryWrapper<>();
            wrapper.eq("is_active", true)
                   .and(w -> w.eq("type", "system").or().eq("status", "active"));
            wrapper.orderByDesc("trend_score");
            wrapper.last("LIMIT " + (limit != null ? limit : 10));
            
            List<ProductTag> tags = this.list(wrapper);
            return ApiResult.success(tags);
        } catch (Exception e) {
            return ApiResult.error(500, "获取热门标签失败：" + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public ApiResult auditCustomTag(Integer tagId, String status) {
        try {
            ProductTag tag = this.getById(tagId);
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
            ProductTag tag = this.getById(tagId);
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
}
