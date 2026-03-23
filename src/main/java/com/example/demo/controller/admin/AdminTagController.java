package com.example.demo.controller.admin;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.TagLevel2;
import com.example.demo.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 标签管理控制器（基于 tag_level_2 表）
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagService tagService;

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
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer sortOrder) {
        log.info("管理员创建二级标签，code={}, name={}", code, name);
        return tagService.createLevel2Tag(code, name, icon, color, sortOrder);
    }

    /**
     * 更新二级标签
     */
    @PutMapping("/{id}")
    public ApiResult updateLevel2Tag(
            @PathVariable Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) Boolean isActive) {
        log.info("管理员更新二级标签，id={}, name={}, isActive={}", id, name, isActive);
        return tagService.updateLevel2Tag(id, name, icon, color, sortOrder, isActive);
    }

    /**
     * 删除二级标签
     */
    @DeleteMapping("/{id}")
    public ApiResult deleteLevel2Tag(@PathVariable Integer id) {
        log.info("管理员删除二级标签，id={}", id);
        return tagService.deleteLevel2Tag(id);
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
            @RequestParam Boolean isActive) {
        log.info("管理员更新二级标签状态，id={}, isActive={}", id, isActive);
        return tagService.updateLevel2TagStatus(id, isActive);
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
            @RequestParam String code,
            @RequestParam String name,
            @RequestParam(required = false) String locationType,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer sortOrder) {
        log.info("管理员创建三级标签，code={}, name={}, locationType={}", code, name, locationType);
        return tagService.createLevel3Tag(code, name, locationType, icon, color, address, latitude, longitude, sortOrder);
    }
    
    /**
     * 更新三级标签
     */
    @PutMapping("/level-3/{id}")
    public ApiResult updateLevel3Tag(
            @PathVariable Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String locationType,
            @RequestParam(required = false) String icon,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) Boolean isActive) {
        log.info("管理员更新三级标签，id={}, name={}, isActive={}", id, name, isActive);
        return tagService.updateLevel3Tag(id, name, locationType, icon, color, address, latitude, longitude, sortOrder, isActive);
    }
    
    /**
     * 删除三级标签
     */
    @DeleteMapping("/level-3/{id}")
    public ApiResult deleteLevel3Tag(@PathVariable Integer id) {
        log.info("管理员删除三级标签，id={}", id);
        return tagService.deleteLevel3Tag(id);
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
            @RequestParam Boolean isActive) {
        log.info("管理员更新三级标签状态，id={}, isActive={}", id, isActive);
        return tagService.updateLevel3TagStatus(id, isActive);
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
     * 更新四级标签状态
     */
    @PutMapping("/level-4/{id}/status")
    public ApiResult updateLevel4TagStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("管理员更新四级标签状态，id={}, status={}", id, status);
        return tagService.updateLevel4TagStatus(id, status);
    }
    
    /**
     * 删除四级标签
     */
    @DeleteMapping("/level-4/{id}")
    public ApiResult deleteLevel4Tag(@PathVariable Long id) {
        log.info("管理员删除四级标签，id={}", id);
        return tagService.deleteLevel4Tag(id);
    }
    
    /**
     * 批量删除四级标签
     */
    @DeleteMapping("/level-4/batch")
    public ApiResult batchDeleteLevel4Tags(@RequestParam String ids) {
        log.info("管理员批量删除四级标签，ids={}", ids);
        return tagService.batchDeleteLevel4Tags(ids);
    }
}
