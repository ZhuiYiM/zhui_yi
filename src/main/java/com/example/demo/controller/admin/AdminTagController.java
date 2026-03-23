package com.example.demo.controller.admin;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.admin.OperationLog;
import com.example.demo.service.admin.OperationLogService;
import com.example.demo.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 标签管理控制器（基于 tag_level_2 表）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 记录操作日志
     */
    private void logOperation(String operation, String module, Long targetId, String detail) {
        try {
            OperationLog log = new OperationLog();
            log.setAdminId(1L); // TODO: 从 Session 获取管理员 ID
            log.setAdminName("admin"); // TODO: 从 Session 获取管理员名称
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

    /**
     * 分页查询二级标签列表
     */
    @GetMapping
    public ApiResult getLevel2Tags(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        log.info("管理员查询二级标签列表，pageNum={}, pageSize={}, keyword={}, isActive={}", 
                pageNum, pageSize, keyword, isActive);
        return tagService.getLevel2TagsAdmin(pageNum, pageSize, keyword, isActive);
    }

    /**
     * 创建二级标签
     */
    @PostMapping
    public ApiResult createLevel2Tag(
            @RequestBody Map<String, Object> params) {
        
        String code = (String) params.get("code");
        String name = (String) params.get("name");
        String icon = (String) params.get("icon");
        String color = (String) params.get("color");
        Integer sortOrder = (Integer) params.get("sortOrder");
        
        log.info("管理员创建二级标签，code={}, name={}", code, name);
        ApiResult result = tagService.createLevel2Tag(code, name, icon, color, sortOrder);
        
        // 记录操作日志
        if (result.getCode() == 200 && result.getData() instanceof Map) {
            Long id = ((Map<String, Object>) result.getData()).get("id") != null ? 
                      Long.valueOf(((Map<String, Object>) result.getData()).get("id").toString()) : null;
            logOperation("create", "tag-level2", id, "创建二级标签：" + name);
        }
        return result;
    }

    /**
     * 更新二级标签
     */
    @PutMapping("/{id}")
    public ApiResult updateLevel2Tag(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        
        String name = (String) params.get("name");
        String icon = (String) params.get("icon");
        String color = (String) params.get("color");
        Integer sortOrder = (Integer) params.get("sortOrder");
        Boolean isActive = (Boolean) params.get("isActive");
        
        log.info("管理员更新二级标签，id={}, name={}, icon={}, color={}, sortOrder={}, isActive={}", 
                id, name, icon, color, sortOrder, isActive);
        ApiResult result = tagService.updateLevel2Tag(id, name, icon, color, sortOrder, isActive);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level2", Long.valueOf(id), "更新二级标签：" + name);
        }
        return result;
    }

    /**
     * 删除二级标签
     */
    @DeleteMapping("/{id}")
    public ApiResult deleteLevel2Tag(@PathVariable Integer id) {
        log.info("管理员删除二级标签，id={}", id);
        ApiResult result = tagService.deleteLevel2Tag(id);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("delete", "tag-level2", Long.valueOf(id), "删除二级标签，ID: " + id);
        }
        return result;
    }

    /**
     * 批量删除二级标签
     */
    @DeleteMapping("/batch")
    public ApiResult batchDeleteLevel2Tags(@RequestParam String ids) {
        log.info("管理员批量删除二级标签，ids={}", ids);
        return tagService.batchDeleteLevel2Tags(ids);
    }

    /**
     * 更新二级标签状态
     */
    @PutMapping("/{id}/status")
    public ApiResult updateLevel2TagStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        Boolean isActive = (Boolean) params.get("isActive");
        log.info("管理员更新二级标签状态，id={}, isActive={}", id, isActive);
        ApiResult result = tagService.updateLevel2TagStatus(id, isActive);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level2", Long.valueOf(id), "更新二级标签状态：" + (isActive ? "启用" : "禁用"));
        }
        return result;
    }
    
    // ==================== 三级标签管理 ====================
    
    /**
     * 分页查询三级标签列表
     */
    @GetMapping("/level-3")
    public ApiResult getLevel3Tags(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String locationType) {
        log.info("管理员查询三级标签列表，pageNum={}, pageSize={}, keyword={}, isActive={}, locationType={}", 
                pageNum, pageSize, keyword, isActive, locationType);
        return tagService.getLevel3TagsAdmin(pageNum, pageSize, keyword, isActive, locationType);
    }
    
    /**
     * 创建三级标签
     */
    @PostMapping("/level-3")
    public ApiResult createLevel3Tag(
            @RequestBody Map<String, Object> params) {
        
        String code = (String) params.get("code");
        String name = (String) params.get("name");
        String locationType = (String) params.get("locationType");
        String icon = (String) params.get("icon");
        String color = (String) params.get("color");
        String address = (String) params.get("address");
        Double latitude = (Double) params.get("latitude");
        Double longitude = (Double) params.get("longitude");
        Integer sortOrder = (Integer) params.get("sortOrder");
        
        log.info("管理员创建三级标签，code={}, name={}, locationType={}", code, name, locationType);
        ApiResult result = tagService.createLevel3Tag(code, name, locationType, icon, color, address, latitude, longitude, sortOrder);
        
        // 记录操作日志
        if (result.getCode() == 200 && result.getData() instanceof Map) {
            Integer id = (Integer)((Map<String, Object>) result.getData()).get("id");
            logOperation("create", "tag-level3", Long.valueOf(id), "创建三级标签：" + name);
        }
        return result;
    }
    
    /**
     * 更新三级标签
     */
    @PutMapping("/level-3/{id}")
    public ApiResult updateLevel3Tag(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        
        String name = (String) params.get("name");
        String locationType = (String) params.get("locationType");
        String icon = (String) params.get("icon");
        String color = (String) params.get("color");
        String address = (String) params.get("address");
        Double latitude = (Double) params.get("latitude");
        Double longitude = (Double) params.get("longitude");
        Integer sortOrder = (Integer) params.get("sortOrder");
        Boolean isActive = (Boolean) params.get("isActive");
        
        log.info("管理员更新三级标签，id={}, name={}, locationType={}, isActive={}", 
                id, name, locationType, isActive);
        ApiResult result = tagService.updateLevel3Tag(id, name, locationType, icon, color, address, latitude, longitude, sortOrder, isActive);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level3", Long.valueOf(id), "更新三级标签：" + name);
        }
        return result;
    }
    
    /**
     * 删除三级标签
     */
    @DeleteMapping("/level-3/{id}")
    public ApiResult deleteLevel3Tag(@PathVariable Integer id) {
        log.info("管理员删除三级标签，id={}", id);
        ApiResult result = tagService.deleteLevel3Tag(id);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("delete", "tag-level3", Long.valueOf(id), "删除三级标签，ID: " + id);
        }
        return result;
    }
    
    /**
     * 批量删除三级标签
     */
    @DeleteMapping("/level-3/batch")
    public ApiResult batchDeleteLevel3Tags(@RequestParam String ids) {
        log.info("管理员批量删除三级标签，ids={}", ids);
        return tagService.batchDeleteLevel3Tags(ids);
    }
    
    /**
     * 更新三级标签状态
     */
    @PutMapping("/level-3/{id}/status")
    public ApiResult updateLevel3TagStatus(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> params) {
        Boolean isActive = (Boolean) params.get("isActive");
        log.info("管理员更新三级标签状态，id={}, isActive={}", id, isActive);
        ApiResult result = tagService.updateLevel3TagStatus(id, isActive);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level3", Long.valueOf(id), "更新三级标签状态：" + (isActive ? "启用" : "禁用"));
        }
        return result;
    }
    
    // ==================== 四级标签管理 ====================
    
    /**
     * 分页查询四级标签列表
     */
    @GetMapping("/level-4")
    public ApiResult getLevel4Tags(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        log.info("管理员查询四级标签列表，pageNum={}, pageSize={}, keyword={}, status={}, category={}", 
                pageNum, pageSize, keyword, status, category);
        return tagService.getLevel4TagsAdmin(pageNum, pageSize, keyword, status, category);
    }
    
    /**
     * 更新四级标签
     */
    @PutMapping("/level-4/{id}")
    public ApiResult updateLevel4Tag(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        
        String name = (String) params.get("name");
        String icon = (String) params.get("icon");
        String color = (String) params.get("color");
        Integer sortOrder = (Integer) params.get("sortOrder");
        String status = (String) params.get("status");
        
        log.info("管理员更新四级标签，id={}, name={}, icon={}, color={}, sortOrder={}, status={}", 
                id, name, icon, color, sortOrder, status);
        ApiResult result = tagService.updateLevel4Tag(id, name, icon, color, sortOrder, status);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level4", id, "更新四级标签：" + name);
        }
        return result;
    }
    
    /**
     * 更新四级标签状态
     */
    @PutMapping("/level-4/{id}/status")
    public ApiResult updateLevel4TagStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        String status = (String) params.get("status");
        log.info("管理员更新四级标签状态，id={}, status={}", id, status);
        ApiResult result = tagService.updateLevel4TagStatus(id, status);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level4", id, "更新四级标签状态：" + status);
        }
        return result;
    }
    
    /**
     * 删除四级标签
     */
    @DeleteMapping("/level-4/{id}")
    public ApiResult deleteLevel4Tag(@PathVariable Long id) {
        log.info("管理员删除四级标签，id={}", id);
        ApiResult result = tagService.deleteLevel4Tag(id);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("delete", "tag-level4", id, "删除四级标签，ID: " + id);
        }
        return result;
    }
    
    /**
     * 批量删除四级标签
     */
    @DeleteMapping("/level-4/batch")
    public ApiResult batchDeleteLevel4Tags(@RequestParam String ids) {
        log.info("管理员批量删除四级标签，ids={}", ids);
        return tagService.batchDeleteLevel4Tags(ids);
    }
    
    // ==================== 五级标签管理（商业标签） ====================
    
    /**
     * 获取所有五级标签列表（前端使用）
     */
    @GetMapping("/level-5/all")
    public ApiResult getLevel5Tags() {
        log.info("获取所有五级标签列表");
        return tagService.getLevel5Tags();
    }
    
    /**
     * 分页查询五级标签列表
     */
    @GetMapping("/level-5")
    public ApiResult getLevel5TagsAdmin(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category) {
        log.info("管理员查询五级标签列表，pageNum={}, pageSize={}, keyword={}, status={}, category={}", 
                pageNum, pageSize, keyword, status, category);
        return tagService.getLevel5TagsAdmin(pageNum, pageSize, keyword, status, category);
    }
    
    /**
     * 创建五级标签
     */
    @PostMapping("/level-5")
    public ApiResult createLevel5Tag(
            @RequestBody Map<String, Object> params) {
        
        String code = (String) params.get("code");
        String name = (String) params.get("name");
        String category = (String) params.get("category");
        String icon = (String) params.get("icon");
        String color = (String) params.get("color");
        Integer sortOrder = (Integer) params.get("sortOrder");
        
        log.info("管理员创建五级标签，code={}, name={}, category={}", code, name, category);
        ApiResult result = tagService.createLevel5Tag(code, name, category, icon, color, sortOrder);
        
        // 记录操作日志
        if (result.getCode() == 200 && result.getData() instanceof Map) {
            Long id = ((Map<String, Object>) result.getData()).get("id") != null ? 
                      Long.valueOf(((Map<String, Object>) result.getData()).get("id").toString()) : null;
            logOperation("create", "tag-level5", id, "创建五级标签：" + name);
        }
        return result;
    }
    
    /**
     * 更新五级标签
     */
    @PutMapping("/level-5/{id}")
    public ApiResult updateLevel5Tag(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        
        String name = (String) params.get("name");
        String category = (String) params.get("category");
        String icon = (String) params.get("icon");
        String color = (String) params.get("color");
        Integer sortOrder = (Integer) params.get("sortOrder");
        Boolean isActive = (Boolean) params.get("isActive");
        String status = (String) params.get("status");
        
        log.info("======= 管理员更新五级标签 =======");
        log.info("ID: {}", id);
        log.info("Name: {}", name);
        log.info("Category: {}", category);
        log.info("Icon: {}", icon);
        log.info("Color: {}", color);
        log.info("SortOrder: {}", sortOrder);
        log.info("IsActive: {}", isActive);
        log.info("Status: {}", status);
        log.info("===================================");
        ApiResult result = tagService.updateLevel5Tag(id, name, category, icon, color, sortOrder, isActive, status);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level5", id, "更新五级标签：" + name);
        }
        return result;
    }
    
    /**
     * 删除五级标签
     */
    @DeleteMapping("/level-5/{id}")
    public ApiResult deleteLevel5Tag(@PathVariable Long id) {
        log.info("管理员删除五级标签，id={}", id);
        ApiResult result = tagService.deleteLevel5Tag(id);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("delete", "tag-level5", id, "删除五级标签，ID: " + id);
        }
        return result;
    }
    
    /**
     * 批量删除五级标签
     */
    @DeleteMapping("/level-5/batch")
    public ApiResult batchDeleteLevel5Tags(@RequestParam String ids) {
        log.info("管理员批量删除五级标签，ids={}", ids);
        return tagService.batchDeleteLevel5Tags(ids);
    }
    
    /**
     * 更新五级标签状态
     */
    @PutMapping("/level-5/{id}/status")
    public ApiResult updateLevel5TagStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Object> params) {
        Boolean isActive = (Boolean) params.get("isActive");
        String status = (String) params.get("status");
        log.info("管理员更新五级标签状态，id={}, isActive={}, status={}", id, isActive, status);
        ApiResult result = tagService.updateLevel5TagStatus(id, isActive, status);
        
        // 记录操作日志
        if (result.getCode() == 200) {
            logOperation("update", "tag-level5", id, "更新五级标签状态：" + (isActive ? "启用" : "禁用"));
        }
        return result;
    }
}
