package com.example.demo.service.tags.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.IdentityTags;
import com.example.demo.entity.LocationTag;
import com.example.demo.entity.ProductTag;
import com.example.demo.entity.TopicTag;
import com.example.demo.mapper.IdentityTagsMapper;
import com.example.demo.mapper.LocationTagMapper;
import com.example.demo.mapper.ProductTagMapper;
import com.example.demo.mapper.TopicTagMapper;
import com.example.demo.service.tags.TagManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签管理服务实现类
 * 基于 identity_tags、product_tag、topic_tag、location_tag 四大核心表
 */
@Slf4j
@Service
public class TagManagementServiceImpl extends ServiceImpl<TopicTagMapper, TopicTag> implements TagManagementService {
    
    @Autowired
    private IdentityTagsMapper identityTagsMapper;
    
    @Autowired
    private LocationTagMapper locationTagMapper;
    
    @Autowired
    private ProductTagMapper productTagMapper;
    
    @Autowired
    private TopicTagMapper topicTagMapper;
    
    // ==================== 身份标签管理 ====================
    
    @Override
    public ApiResult getIdentityTags(Integer pageNum, Integer pageSize, String keyword, Boolean isActive) {
        log.info("查询身份标签，pageNum={}, pageSize={}, keyword={}, isActive={}", pageNum, pageSize, keyword, isActive);
        
        try {
            Page<IdentityTags> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<IdentityTags> wrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(IdentityTags::getName, keyword)
                        .or().like(IdentityTags::getCode, keyword));
            }
            
            if (isActive != null) {
                wrapper.eq(IdentityTags::getIsActive, isActive);
            }
            
            wrapper.orderByDesc(IdentityTags::getSortOrder)
                   .orderByDesc(IdentityTags::getId);
            
            Page<IdentityTags> resultPage = identityTagsMapper.selectPage(page, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", resultPage.getRecords());
            data.put("total", resultPage.getTotal());
            data.put("pageNum", resultPage.getCurrent());
            data.put("pageSize", resultPage.getSize());
            
            return ApiResult.success(data);
        } catch (Exception e) {
            log.error("查询身份标签失败", e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult createIdentityTag(IdentityTags tag) {
        try {
            tag.setCreatedAt(LocalDateTime.now());
            if (tag.getIsActive() == null) {
                tag.setIsActive(true);
            }
            if (tag.getSortOrder() == null) {
                tag.setSortOrder(0);
            }
            
            boolean saved = identityTagsMapper.insert(tag) > 0;
            if (saved) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", tag.getId());
                return ApiResult.success("创建成功", data);
            }
            return ApiResult.error("创建失败");
        } catch (Exception e) {
            log.error("创建身份标签失败", e);
            return ApiResult.error("创建失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateIdentityTag(IdentityTags tag) {
        try {
            IdentityTags existing = identityTagsMapper.selectById(tag.getId());
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            if (tag.getName() != null) existing.setName(tag.getName());
            if (tag.getCode() != null) existing.setCode(tag.getCode());
            if (tag.getIcon() != null) existing.setIcon(tag.getIcon());
            if (tag.getDescription() != null) existing.setDescription(tag.getDescription());
            if (tag.getSortOrder() != null) existing.setSortOrder(tag.getSortOrder());
            if (tag.getIsActive() != null) existing.setIsActive(tag.getIsActive());
            
            boolean updated = identityTagsMapper.updateById(existing) > 0;
            return updated ? ApiResult.success("更新成功", null) : ApiResult.error("更新失败");
        } catch (Exception e) {
            log.error("更新身份标签失败", e);
            return ApiResult.error("更新失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult deleteIdentityTag(Integer id) {
        try {
            boolean removed = identityTagsMapper.deleteById(id) > 0;
            return removed ? ApiResult.success("删除成功", null) : ApiResult.error("删除失败");
        } catch (Exception e) {
            log.error("删除身份标签失败", e);
            return ApiResult.error("删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult batchDeleteIdentityTags(List<Integer> ids) {
        try {
            int count = 0;
            for (Integer id : ids) {
                count += identityTagsMapper.deleteById(id);
            }
            return count > 0 ? ApiResult.success("批量删除成功", null) : ApiResult.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除身份标签失败", e);
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateIdentityTagStatus(Integer id, Boolean isActive) {
        try {
            IdentityTags tag = identityTagsMapper.selectById(id);
            if (tag == null) {
                return ApiResult.error("标签不存在");
            }
            tag.setIsActive(isActive);
            boolean updated = identityTagsMapper.updateById(tag) > 0;
            return updated ? ApiResult.success("状态更新成功", null) : ApiResult.error("状态更新失败");
        } catch (Exception e) {
            log.error("更新身份标签状态失败", e);
            return ApiResult.error("状态更新失败：" + e.getMessage());
        }
    }
    
    // ==================== 话题标签管理（二级标签）====================
    
    @Override
    public ApiResult getTopicTags(Integer pageNum, Integer pageSize, String keyword, Boolean isActive, String type) {
        log.info("查询话题标签，pageNum={}, pageSize={}, keyword={}, isActive={}, type={}", pageNum, pageSize, keyword, isActive, type);
        
        try {
            Page<TopicTag> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<TopicTag> wrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(TopicTag::getName, keyword)
                        .or().like(TopicTag::getCode, keyword));
            }
            
            if (isActive != null) {
                wrapper.eq(TopicTag::getIsActive, isActive);
            }
            
            if (StringUtils.hasText(type)) {
                wrapper.eq(TopicTag::getType, type);
            }
            
            wrapper.orderByDesc(TopicTag::getSortOrder)
                   .orderByDesc(TopicTag::getId);
            
            Page<TopicTag> resultPage = topicTagMapper.selectPage(page, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", resultPage.getRecords());
            data.put("total", resultPage.getTotal());
            data.put("pageNum", resultPage.getCurrent());
            data.put("pageSize", resultPage.getSize());
            
            return ApiResult.success(data);
        } catch (Exception e) {
            log.error("查询话题标签失败", e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult createTopicTag(TopicTag tag) {
        try {
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            if (tag.getType() == null) {
                tag.setType("system");
            }
            if (tag.getIsActive() == null) {
                tag.setIsActive(true);
            }
            if (tag.getStatus() == null) {
                tag.setStatus("active");
            }
            if (tag.getUsageCount() == null) {
                tag.setUsageCount(0L);
            }
            if (tag.getTrendScore() == null) {
                tag.setTrendScore(0.0);
            }
            if (tag.getSortOrder() == null) {
                tag.setSortOrder(0);
            }
            
            boolean saved = topicTagMapper.insert(tag) > 0;
            if (saved) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", tag.getId());
                return ApiResult.success("创建成功", data);
            }
            return ApiResult.error("创建失败");
        } catch (Exception e) {
            log.error("创建话题标签失败", e);
            return ApiResult.error("创建失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateTopicTag(TopicTag tag) {
        try {
            TopicTag existing = topicTagMapper.selectById(tag.getId());
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            if (tag.getName() != null) existing.setName(tag.getName());
            if (tag.getCode() != null) existing.setCode(tag.getCode());
            if (tag.getIcon() != null) existing.setIcon(tag.getIcon());
            if (tag.getColor() != null) existing.setColor(tag.getColor());
            if (tag.getSortOrder() != null) existing.setSortOrder(tag.getSortOrder());
            if (tag.getIsActive() != null) existing.setIsActive(tag.getIsActive());
            if (tag.getType() != null) existing.setType(tag.getType());
            existing.setUpdatedAt(LocalDateTime.now());
            
            boolean updated = topicTagMapper.updateById(existing) > 0;
            return updated ? ApiResult.success("更新成功", null) : ApiResult.error("更新失败");
        } catch (Exception e) {
            log.error("更新话题标签失败", e);
            return ApiResult.error("更新失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult deleteTopicTag(Integer id) {
        try {
            boolean removed = topicTagMapper.deleteById(id) > 0;
            return removed ? ApiResult.success("删除成功", null) : ApiResult.error("删除失败");
        } catch (Exception e) {
            log.error("删除话题标签失败", e);
            return ApiResult.error("删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult batchDeleteTopicTags(List<Integer> ids) {
        try {
            int count = 0;
            for (Integer id : ids) {
                count += topicTagMapper.deleteById(id);
            }
            return count > 0 ? ApiResult.success("批量删除成功", null) : ApiResult.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除话题标签失败", e);
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateTopicTagStatus(Integer id, Boolean isActive) {
        try {
            TopicTag tag = topicTagMapper.selectById(id);
            if (tag == null) {
                return ApiResult.error("标签不存在");
            }
            tag.setIsActive(isActive);
            tag.setUpdatedAt(LocalDateTime.now());
            boolean updated = topicTagMapper.updateById(tag) > 0;
            return updated ? ApiResult.success("状态更新成功", null) : ApiResult.error("状态更新失败");
        } catch (Exception e) {
            log.error("更新话题标签状态失败", e);
            return ApiResult.error("状态更新失败：" + e.getMessage());
        }
    }
    
    // ==================== 地点标签管理（三级标签）====================
    
    @Override
    public ApiResult getLocationTags(Integer pageNum, Integer pageSize, String keyword, Boolean isActive, String locationType) {
        log.info("查询地点标签，pageNum={}, pageSize={}, keyword={}, isActive={}, locationType={}", pageNum, pageSize, keyword, isActive, locationType);
        
        try {
            Page<LocationTag> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<LocationTag> wrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(LocationTag::getName, keyword));
            }
            
            if (isActive != null) {
                wrapper.eq(LocationTag::getIsActive, isActive);
            }
            
            if (StringUtils.hasText(locationType)) {
                wrapper.eq(LocationTag::getType, locationType);
            }
            
            wrapper.orderByDesc(LocationTag::getSortOrder)
                   .orderByDesc(LocationTag::getId);
            
            Page<LocationTag> resultPage = locationTagMapper.selectPage(page, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", resultPage.getRecords());
            data.put("total", resultPage.getTotal());
            data.put("pageNum", resultPage.getCurrent());
            data.put("pageSize", resultPage.getSize());
            
            return ApiResult.success(data);
        } catch (Exception e) {
            log.error("查询地点标签失败", e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult createLocationTag(LocationTag tag) {
        try {
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            if (tag.getType() == null) {
                tag.setType("location");
            }
            if (tag.getIsActive() == null) {
                tag.setIsActive(true);
            }
            if (tag.getStatus() == null) {
                tag.setStatus("active");
            }
            if (tag.getUsageCount() == null) {
                tag.setUsageCount(0L);
            }
            if (tag.getTrendScore() == null) {
                tag.setTrendScore(0.0);
            }
            if (tag.getSortOrder() == null) {
                tag.setSortOrder(0);
            }
            
            boolean saved = locationTagMapper.insert(tag) > 0;
            if (saved) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", tag.getId());
                return ApiResult.success("创建成功", data);
            }
            return ApiResult.error("创建失败");
        } catch (Exception e) {
            log.error("创建地点标签失败", e);
            return ApiResult.error("创建失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateLocationTag(LocationTag tag) {
        try {
            LocationTag existing = locationTagMapper.selectById(tag.getId());
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            if (tag.getName() != null) existing.setName(tag.getName());
            if (tag.getType() != null) existing.setType(tag.getType());
            if (tag.getIcon() != null) existing.setIcon(tag.getIcon());
            if (tag.getColor() != null) existing.setColor(tag.getColor());
            if (tag.getSortOrder() != null) existing.setSortOrder(tag.getSortOrder());
            if (tag.getIsActive() != null) existing.setIsActive(tag.getIsActive());
            existing.setUpdatedAt(LocalDateTime.now());
            
            boolean updated = locationTagMapper.updateById(existing) > 0;
            return updated ? ApiResult.success("更新成功", null) : ApiResult.error("更新失败");
        } catch (Exception e) {
            log.error("更新地点标签失败", e);
            return ApiResult.error("更新失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult deleteLocationTag(Integer id) {
        try {
            boolean removed = locationTagMapper.deleteById(id) > 0;
            return removed ? ApiResult.success("删除成功", null) : ApiResult.error("删除失败");
        } catch (Exception e) {
            log.error("删除地点标签失败", e);
            return ApiResult.error("删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult batchDeleteLocationTags(List<Integer> ids) {
        try {
            int count = 0;
            for (Integer id : ids) {
                count += locationTagMapper.deleteById(id);
            }
            return count > 0 ? ApiResult.success("批量删除成功", null) : ApiResult.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除地点标签失败", e);
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateLocationTagStatus(Integer id, Boolean isActive) {
        try {
            LocationTag tag = locationTagMapper.selectById(id);
            if (tag == null) {
                return ApiResult.error("标签不存在");
            }
            tag.setIsActive(isActive);
            tag.setUpdatedAt(LocalDateTime.now());
            boolean updated = locationTagMapper.updateById(tag) > 0;
            return updated ? ApiResult.success("状态更新成功", null) : ApiResult.error("状态更新失败");
        } catch (Exception e) {
            log.error("更新地点标签状态失败", e);
            return ApiResult.error("状态更新失败：" + e.getMessage());
        }
    }
    
    // ==================== 商品标签管理（四级、五级标签）====================
    
    @Override
    public ApiResult getProductTags(Integer pageNum, Integer pageSize, String keyword, String status, String category, String type) {
        log.info("查询商品标签，pageNum={}, pageSize={}, keyword={}, status={}, category={}, type={}", pageNum, pageSize, keyword, status, category, type);
        
        try {
            Page<ProductTag> page = new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 10);
            LambdaQueryWrapper<ProductTag> wrapper = new LambdaQueryWrapper<>();
            
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(ProductTag::getName, keyword)
                        .or().like(ProductTag::getCode, keyword));
            }
            
            if (StringUtils.hasText(status)) {
                wrapper.eq(ProductTag::getStatus, status);
            }
            
            if (StringUtils.hasText(category)) {
                wrapper.eq(ProductTag::getCategory, category);
            }
            
            if (StringUtils.hasText(type)) {
                wrapper.eq(ProductTag::getType, type);
            }
            
            wrapper.orderByDesc(ProductTag::getSortOrder)
                   .orderByDesc(ProductTag::getId);
            
            Page<ProductTag> resultPage = productTagMapper.selectPage(page, wrapper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("records", resultPage.getRecords());
            data.put("total", resultPage.getTotal());
            data.put("pageNum", resultPage.getCurrent());
            data.put("pageSize", resultPage.getSize());
            
            return ApiResult.success(data);
        } catch (Exception e) {
            log.error("查询商品标签失败", e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult createProductTag(ProductTag tag) {
        try {
            tag.setCreatedAt(LocalDateTime.now());
            tag.setUpdatedAt(LocalDateTime.now());
            if (tag.getType() == null) {
                tag.setType("custom");
            }
            if (tag.getIsActive() == null) {
                tag.setIsActive(true);
            }
            if (tag.getStatus() == null) {
                tag.setStatus("pending");
            }
            if (tag.getUsageCount() == null) {
                tag.setUsageCount(0L);
            }
            if (tag.getTrendScore() == null) {
                tag.setTrendScore(0.0);
            }
            if (tag.getSortOrder() == null) {
                tag.setSortOrder(0);
            }
            
            boolean saved = productTagMapper.insert(tag) > 0;
            if (saved) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", tag.getId());
                return ApiResult.success("创建成功", data);
            }
            return ApiResult.error("创建失败");
        } catch (Exception e) {
            log.error("创建商品标签失败", e);
            return ApiResult.error("创建失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateProductTag(ProductTag tag) {
        try {
            ProductTag existing = productTagMapper.selectById(tag.getId());
            if (existing == null) {
                return ApiResult.error("标签不存在");
            }
            
            if (tag.getName() != null) existing.setName(tag.getName());
            if (tag.getCode() != null) existing.setCode(tag.getCode());
            if (tag.getCategory() != null) existing.setCategory(tag.getCategory());
            if (tag.getIcon() != null) existing.setIcon(tag.getIcon());
            if (tag.getColor() != null) existing.setColor(tag.getColor());
            if (tag.getSortOrder() != null) existing.setSortOrder(tag.getSortOrder());
            if (tag.getStatus() != null) existing.setStatus(tag.getStatus());
            if (tag.getType() != null) existing.setType(tag.getType());
            existing.setUpdatedAt(LocalDateTime.now());
            
            boolean updated = productTagMapper.updateById(existing) > 0;
            return updated ? ApiResult.success("更新成功", null) : ApiResult.error("更新失败");
        } catch (Exception e) {
            log.error("更新商品标签失败", e);
            return ApiResult.error("更新失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult deleteProductTag(Integer id) {
        try {
            boolean removed = productTagMapper.deleteById(id) > 0;
            return removed ? ApiResult.success("删除成功", null) : ApiResult.error("删除失败");
        } catch (Exception e) {
            log.error("删除商品标签失败", e);
            return ApiResult.error("删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult batchDeleteProductTags(List<Integer> ids) {
        try {
            int count = 0;
            for (Integer id : ids) {
                count += productTagMapper.deleteById(id);
            }
            return count > 0 ? ApiResult.success("批量删除成功", null) : ApiResult.error("批量删除失败");
        } catch (Exception e) {
            log.error("批量删除商品标签失败", e);
            return ApiResult.error("批量删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult updateProductTagStatus(Integer id, String status) {
        try {
            ProductTag tag = productTagMapper.selectById(id);
            if (tag == null) {
                return ApiResult.error("标签不存在");
            }
            tag.setStatus(status);
            if ("active".equals(status)) {
                tag.setIsActive(true);
            } else if ("banned".equals(status)) {
                tag.setIsActive(false);
            }
            tag.setUpdatedAt(LocalDateTime.now());
            boolean updated = productTagMapper.updateById(tag) > 0;
            return updated ? ApiResult.success("状态更新成功", null) : ApiResult.error("状态更新失败");
        } catch (Exception e) {
            log.error("更新商品标签状态失败", e);
            return ApiResult.error("状态更新失败：" + e.getMessage());
        }
    }
}
