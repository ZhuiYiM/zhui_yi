package com.example.demo.controller.admin;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.IdentityTags;
import com.example.demo.entity.LocationTag;
import com.example.demo.entity.ProductTag;
import com.example.demo.entity.TopicTag;
import com.example.demo.entity.admin.OperationLog;
import com.example.demo.service.admin.OperationLogService;
import com.example.demo.service.tags.TagManagementService;
import com.example.demo.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器（新版）
 * 基于 identity_tags、product_tag、topic_tag、location_tag 四大核心表
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {
    
    private final TagManagementService tagManagementService;
    
    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private HttpServletRequest request;
    
    /**
     * 记录操作日志
     */
    private void logOperation(String operation, String module, Long targetId, String detail) {
        try {
            OperationLog log = new OperationLog();
            log.setAdminId(jwtUtil.getCurrentAdminId());
            log.setAdminName(jwtUtil.getCurrentAdminUsername());
            log.setOperation(operation);
            log.setModule(module);
            log.setTargetId(targetId);
            log.setDetail(detail);
            log.setIpAddress(request.getRemoteAddr());
            log.setCreatedAt(LocalDateTime.now());
            operationLogService.save(log);
        } catch (Exception e) {
            log.error("记录操作日志失败：{}", e.getMessage());
        }
    }
    
    // ==================== 身份标签管理 ====================
    
    /**
     * 分页查询身份标签列表
     */
    @GetMapping("/identity")
    public ApiResult getIdentityTags(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        log.info("管理员查询身份标签列表，pageNum={}, pageSize={}, keyword={}, isActive={}", 
                pageNum, pageSize, keyword, isActive);
        return tagManagementService.getIdentityTags(pageNum, pageSize, keyword, isActive);
    }
    
    /**
     * 创建身份标签
     */
    @PostMapping("/identity")
    public ApiResult createIdentityTag(@RequestBody Map<String, Object> params) {
        IdentityTags tag = new IdentityTags();
        tag.setCode((String) params.get("code"));
        tag.setName((String) params.get("name"));
        tag.setIcon((String) params.get("icon"));
        tag.setDescription((String) params.get("description"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        tag.setIsActive((Boolean) params.get("isActive"));
        
        log.info("管理员创建身份标签，code={}, name={}", tag.getCode(), tag.getName());
        ApiResult result = tagManagementService.createIdentityTag(tag);
        
        if (result.getCode() == 200 && result.getData() instanceof Map) {
            Integer id = (Integer)((Map<String, Object>) result.getData()).get("id");
            logOperation("create", "identity-tags", Long.valueOf(id), "创建身份标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 更新身份标签
     */
    @PutMapping("/identity/{id}")
    public ApiResult updateIdentityTag(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        
        IdentityTags tag = new IdentityTags();
        tag.setId(id);
        tag.setName((String) params.get("name"));
        tag.setCode((String) params.get("code"));
        tag.setIcon((String) params.get("icon"));
        tag.setDescription((String) params.get("description"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        tag.setIsActive((Boolean) params.get("isActive"));
        
        log.info("管理员更新身份标签，id={}, name={}", id, tag.getName());
        ApiResult result = tagManagementService.updateIdentityTag(tag);
        
        if (result.getCode() == 200) {
            logOperation("update", "identity-tags", Long.valueOf(id), "更新身份标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 删除身份标签
     */
    @DeleteMapping("/identity/{id}")
    public ApiResult deleteIdentityTag(@PathVariable Integer id) {
        log.info("管理员删除身份标签，id={}", id);
        ApiResult result = tagManagementService.deleteIdentityTag(id);
        
        if (result.getCode() == 200) {
            logOperation("delete", "identity-tags", Long.valueOf(id), "删除身份标签，ID: " + id);
        }
        return result;
    }
    
    /**
     * 批量删除身份标签
     */
    @DeleteMapping("/identity/batch")
    public ApiResult batchDeleteIdentityTags(@RequestParam String ids) {
        log.info("管理员批量删除身份标签，ids={}", ids);
        List<Integer> idList = parseIds(ids);
        return tagManagementService.batchDeleteIdentityTags(idList);
    }
    
    /**
     * 更新身份标签状态
     */
    @PutMapping("/identity/{id}/status")
    public ApiResult updateIdentityTagStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        Boolean isActive = (Boolean) params.get("isActive");
        log.info("管理员更新身份标签状态，id={}, isActive={}", id, isActive);
        ApiResult result = tagManagementService.updateIdentityTagStatus(id, isActive);
        
        if (result.getCode() == 200) {
            logOperation("update", "identity-tags", Long.valueOf(id), "更新身份标签状态：" + (isActive ? "启用" : "禁用"));
        }
        return result;
    }
    
    // ==================== 话题标签管理（二级标签）====================
    
    /**
     * 分页查询话题标签列表
     */
    @GetMapping("/topic")
    public ApiResult getTopicTags(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String type) {
        log.info("管理员查询话题标签列表，pageNum={}, pageSize={}, keyword={}, isActive={}, type={}", 
                pageNum, pageSize, keyword, isActive, type);
        return tagManagementService.getTopicTags(pageNum, pageSize, keyword, isActive, type);
    }
    
    /**
     * 创建话题标签
     */
    @PostMapping("/topic")
    public ApiResult createTopicTag(@RequestBody Map<String, Object> params) {
        TopicTag tag = new TopicTag();
        tag.setCode((String) params.get("code"));
        tag.setName((String) params.get("name"));
        tag.setIcon((String) params.get("icon"));
        tag.setColor((String) params.get("color"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        tag.setType((String) params.get("type"));
        
        log.info("管理员创建话题标签，code={}, name={}", tag.getCode(), tag.getName());
        ApiResult result = tagManagementService.createTopicTag(tag);
        
        if (result.getCode() == 200 && result.getData() instanceof Map) {
            Integer id = (Integer)((Map<String, Object>) result.getData()).get("id");
            logOperation("create", "topic-tags", Long.valueOf(id), "创建话题标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 更新话题标签
     */
    @PutMapping("/topic/{id}")
    public ApiResult updateTopicTag(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        
        TopicTag tag = new TopicTag();
        tag.setId(id);
        tag.setName((String) params.get("name"));
        tag.setCode((String) params.get("code"));
        tag.setIcon((String) params.get("icon"));
        tag.setColor((String) params.get("color"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        tag.setType((String) params.get("type"));
        tag.setIsActive((Boolean) params.get("isActive"));
        
        log.info("管理员更新话题标签，id={}, name={}", id, tag.getName());
        ApiResult result = tagManagementService.updateTopicTag(tag);
        
        if (result.getCode() == 200) {
            logOperation("update", "topic-tags", Long.valueOf(id), "更新话题标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 删除话题标签
     */
    @DeleteMapping("/topic/{id}")
    public ApiResult deleteTopicTag(@PathVariable Integer id) {
        log.info("管理员删除话题标签，id={}", id);
        ApiResult result = tagManagementService.deleteTopicTag(id);
        
        if (result.getCode() == 200) {
            logOperation("delete", "topic-tags", Long.valueOf(id), "删除话题标签，ID: " + id);
        }
        return result;
    }
    
    /**
     * 批量删除话题标签
     */
    @DeleteMapping("/topic/batch")
    public ApiResult batchDeleteTopicTags(@RequestParam String ids) {
        log.info("管理员批量删除话题标签，ids={}", ids);
        List<Integer> idList = parseIds(ids);
        return tagManagementService.batchDeleteTopicTags(idList);
    }
    
    /**
     * 更新话题标签状态
     */
    @PutMapping("/topic/{id}/status")
    public ApiResult updateTopicTagStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        Boolean isActive = (Boolean) params.get("isActive");
        log.info("管理员更新话题标签状态，id={}, isActive={}", id, isActive);
        ApiResult result = tagManagementService.updateTopicTagStatus(id, isActive);
        
        if (result.getCode() == 200) {
            logOperation("update", "topic-tags", Long.valueOf(id), "更新话题标签状态：" + (isActive ? "启用" : "禁用"));
        }
        return result;
    }
    
    // ==================== 地点标签管理（三级标签）====================
    
    /**
     * 分页查询地点标签列表
     */
    @GetMapping("/location")
    public ApiResult getLocationTags(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String type) {
        log.info("管理员查询地点标签列表，pageNum={}, pageSize={}, keyword={}, isActive={}, type={}", 
                pageNum, pageSize, keyword, isActive, type);
        return tagManagementService.getLocationTags(pageNum, pageSize, keyword, isActive, type);
    }
    
    /**
     * 创建地点标签
     */
    @PostMapping("/location")
    public ApiResult createLocationTag(@RequestBody Map<String, Object> params) {
        LocationTag tag = new LocationTag();
        tag.setName((String) params.get("name"));
        tag.setType((String) params.get("type"));
        tag.setIcon((String) params.get("icon"));
        tag.setColor((String) params.get("color"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        
        log.info("管理员创建地点标签，name={}, type={}", tag.getName(), tag.getType());
        ApiResult result = tagManagementService.createLocationTag(tag);
        
        if (result.getCode() == 200 && result.getData() instanceof Map) {
            Integer id = (Integer)((Map<String, Object>) result.getData()).get("id");
            logOperation("create", "location-tags", Long.valueOf(id), "创建地点标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 更新地点标签
     */
    @PutMapping("/location/{id}")
    public ApiResult updateLocationTag(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        
        LocationTag tag = new LocationTag();
        tag.setId(id);
        tag.setName((String) params.get("name"));
        tag.setType((String) params.get("type"));
        tag.setIcon((String) params.get("icon"));
        tag.setColor((String) params.get("color"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        tag.setIsActive((Boolean) params.get("isActive"));
        
        log.info("管理员更新地点标签，id={}, name={}", id, tag.getName());
        ApiResult result = tagManagementService.updateLocationTag(tag);
        
        if (result.getCode() == 200) {
            logOperation("update", "location-tags", Long.valueOf(id), "更新地点标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 删除地点标签
     */
    @DeleteMapping("/location/{id}")
    public ApiResult deleteLocationTag(@PathVariable Integer id) {
        log.info("管理员删除地点标签，id={}", id);
        ApiResult result = tagManagementService.deleteLocationTag(id);
        
        if (result.getCode() == 200) {
            logOperation("delete", "location-tags", Long.valueOf(id), "删除地点标签，ID: " + id);
        }
        return result;
    }
    
    /**
     * 批量删除地点标签
     */
    @DeleteMapping("/location/batch")
    public ApiResult batchDeleteLocationTags(@RequestParam String ids) {
        log.info("管理员批量删除地点标签，ids={}", ids);
        List<Integer> idList = parseIds(ids);
        return tagManagementService.batchDeleteLocationTags(idList);
    }
    
    /**
     * 更新地点标签状态
     */
    @PutMapping("/location/{id}/status")
    public ApiResult updateLocationTagStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        Boolean isActive = (Boolean) params.get("isActive");
        log.info("管理员更新地点标签状态，id={}, isActive={}", id, isActive);
        ApiResult result = tagManagementService.updateLocationTagStatus(id, isActive);
        
        if (result.getCode() == 200) {
            logOperation("update", "location-tags", Long.valueOf(id), "更新地点标签状态：" + (isActive ? "启用" : "禁用"));
        }
        return result;
    }
    
    // ==================== 商品标签管理（四级、五级标签）====================
    
    /**
     * 分页查询商品标签列表
     */
    @GetMapping("/product")
    public ApiResult getProductTags(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String type) {
        log.info("管理员查询商品标签列表，pageNum={}, pageSize={}, keyword={}, status={}, category={}, type={}", 
                pageNum, pageSize, keyword, status, category, type);
        return tagManagementService.getProductTags(pageNum, pageSize, keyword, status, category, type);
    }
    
    /**
     * 创建商品标签
     */
    @PostMapping("/product")
    public ApiResult createProductTag(@RequestBody Map<String, Object> params) {
        ProductTag tag = new ProductTag();
        tag.setName((String) params.get("name"));
        tag.setCode((String) params.get("code"));
        tag.setCategory((String) params.get("category"));
        tag.setType((String) params.get("type"));
        tag.setIcon((String) params.get("icon"));
        tag.setColor((String) params.get("color"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        tag.setStatus((String) params.get("status"));
        
        log.info("管理员创建商品标签，name={}, category={}", tag.getName(), tag.getCategory());
        ApiResult result = tagManagementService.createProductTag(tag);
        
        if (result.getCode() == 200 && result.getData() instanceof Map) {
            Integer id = (Integer)((Map<String, Object>) result.getData()).get("id");
            logOperation("create", "product-tags", Long.valueOf(id), "创建商品标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 更新商品标签
     */
    @PutMapping("/product/{id}")
    public ApiResult updateProductTag(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        
        ProductTag tag = new ProductTag();
        tag.setId(id);
        tag.setName((String) params.get("name"));
        tag.setCode((String) params.get("code"));
        tag.setCategory((String) params.get("category"));
        tag.setType((String) params.get("type"));
        tag.setIcon((String) params.get("icon"));
        tag.setColor((String) params.get("color"));
        tag.setSortOrder((Integer) params.get("sortOrder"));
        tag.setStatus((String) params.get("status"));
        
        log.info("管理员更新商品标签，id={}, name={}", id, tag.getName());
        ApiResult result = tagManagementService.updateProductTag(tag);
        
        if (result.getCode() == 200) {
            logOperation("update", "product-tags", Long.valueOf(id), "更新商品标签：" + tag.getName());
        }
        return result;
    }
    
    /**
     * 删除商品标签
     */
    @DeleteMapping("/product/{id}")
    public ApiResult deleteProductTag(@PathVariable Integer id) {
        log.info("管理员删除商品标签，id={}", id);
        ApiResult result = tagManagementService.deleteProductTag(id);
        
        if (result.getCode() == 200) {
            logOperation("delete", "product-tags", Long.valueOf(id), "删除商品标签，ID: " + id);
        }
        return result;
    }
    
    /**
     * 批量删除商品标签
     */
    @DeleteMapping("/product/batch")
    public ApiResult batchDeleteProductTags(@RequestParam String ids) {
        log.info("管理员批量删除商品标签，ids={}", ids);
        List<Integer> idList = parseIds(ids);
        return tagManagementService.batchDeleteProductTags(idList);
    }
    
    /**
     * 更新商品标签状态
     */
    @PutMapping("/product/{id}/status")
    public ApiResult updateProductTagStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        String status = (String) params.get("status");
        log.info("管理员更新商品标签状态，id={}, status={}", id, status);
        ApiResult result = tagManagementService.updateProductTagStatus(id, status);
        
        if (result.getCode() == 200) {
            logOperation("update", "product-tags", Long.valueOf(id), "更新商品标签状态：" + status);
        }
        return result;
    }
    
    /**
     * 解析 ID 字符串为列表
     */
    private List<Integer> parseIds(String ids) {
        return java.util.Arrays.stream(ids.split(","))
                .map(String::trim)
                .map(Integer::valueOf)
                .toList();
    }
}
