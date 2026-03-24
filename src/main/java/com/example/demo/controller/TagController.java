package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.service.tags.TopicTagService;
import com.example.demo.service.tags.ProductTagService;
import com.example.demo.service.tags.LocationTagService;
import com.example.demo.service.tags.IdentityTagsService;
import com.example.demo.dto.CustomTagSubmitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin
public class TagController {
    
    @Autowired
    private TopicTagService topicTagService;
    
    @Autowired
    private ProductTagService productTagService;
    
    @Autowired
    private LocationTagService locationTagService;
    
    @Autowired
    private IdentityTagsService identityTagsService;
    
    // ========== 话题标签 ==========
    
    /**
     * 获取所有话题标签
     */
    @GetMapping("/topics")
    public ApiResult getAllTopicTags() {
        return topicTagService.getAllTopicTags();
    }
    
    /**
     * 获取身份标签（一级标签）
     */
    @GetMapping("/level1")
    public ApiResult getLevel1Tags() {
        return identityTagsService.getAllIdentityTags();
    }
    
    /**
     * 创建用户自定义话题标签
     */
    @PostMapping("/topics/custom")
    public ApiResult createCustomTopicTag(
        @RequestBody Map<String, String> body,
        HttpServletRequest request
    ) {
        String tagName = body.get("name");
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return ApiResult.error(401, "用户未登录");
        }
        
        return topicTagService.createCustomTopicTag(tagName, userId);
    }
    
    /**
     * 用户提交自定义标签（待审核）
     */
    @PostMapping("/custom/submit")
    public ApiResult submitCustomTag(
        @RequestBody CustomTagSubmitRequest request,
        HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (userId == null) {
            return ApiResult.error(401, "请先登录");
        }
        
        // 参数校验
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResult.error(400, "标签名称不能为空");
        }
        
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 20) {
            return ApiResult.error(400, "标签名称长度为 2-20 个字符");
        }
        
        return topicTagService.submitCustomTag(request, userId);
    }
    
    /**
     * 获取热门话题标签
     */
    @GetMapping("/topics/hot")
    public ApiResult getHotTopicTags(@RequestParam(defaultValue = "10") Integer limit) {
        return topicTagService.getHotTopicTags(limit);
    }
    
    // ========== 商品标签 ==========
    
    /**
     * 获取所有商品标签
     */
    @GetMapping("/products")
    public ApiResult getAllProductTags() {
        return productTagService.getAllProductTags();
    }
    
    /**
     * 创建用户自定义商品标签
     */
    @PostMapping("/products/custom")
    public ApiResult createCustomProductTag(
        @RequestBody Map<String, String> body,
        HttpServletRequest request
    ) {
        String tagName = body.get("name");
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return ApiResult.error(401, "用户未登录");
        }
        
        return productTagService.createCustomProductTag(tagName, userId);
    }
    
    /**
     * 用户提交自定义商品标签（待审核）
     */
    @PostMapping("/products/custom/submit")
    public ApiResult submitCustomProductTag(
        @RequestBody CustomTagSubmitRequest request,
        HttpServletRequest httpRequest
    ) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        
        if (userId == null) {
            return ApiResult.error(401, "请先登录");
        }
        
        // 参数校验
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ApiResult.error(400, "标签名称不能为空");
        }
        
        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 20) {
            return ApiResult.error(400, "标签名称长度为 2-20 个字符");
        }
        
        return productTagService.submitCustomTag(request, userId);
    }
    
    /**
     * 获取热门商品标签
     */
    @GetMapping("/products/hot")
    public ApiResult getHotProductTags(@RequestParam(defaultValue = "10") Integer limit) {
        return productTagService.getHotProductTags(limit);
    }
    
    // ========== 地点标签 ==========
    
    /**
     * 获取所有地点标签
     */
    @GetMapping("/locations")
    public ApiResult getAllLocationTags() {
        return locationTagService.getAllLocationTags();
    }
    
    /**
     * 创建用户自定义地点标签
     */
    @PostMapping("/locations/custom")
    public ApiResult createCustomLocationTag(
        @RequestBody Map<String, String> body,
        HttpServletRequest request
    ) {
        String tagName = body.get("name");
        Long userId = (Long) request.getAttribute("userId");
        
        if (userId == null) {
            return ApiResult.error(401, "用户未登录");
        }
        
        return locationTagService.createCustomLocationTag(tagName, userId);
    }
    
    /**
     * 获取热门地点标签
     */
    @GetMapping("/locations/hot")
    public ApiResult getHotLocationTags(@RequestParam(defaultValue = "10") Integer limit) {
        return locationTagService.getHotLocationTags(limit);
    }
    
    // ========== 管理员功能 ==========
    
    /**
     * 审核用户自定义标签
     */
    @PostMapping("/{type}/{id}/audit")
    public ApiResult auditCustomTag(
        @PathVariable String type,
        @PathVariable Integer id,
        @RequestBody Map<String, String> body
    ) {
        String status = body.get("status"); // active/banned
        
        return switch (type) {
            case "topic" -> topicTagService.auditCustomTag(id, status);
            case "product" -> productTagService.auditCustomTag(id, status);
            case "location" -> locationTagService.auditCustomTag(id, status);
            default -> ApiResult.error(400, "无效的标签类型");
        };
    }
    
    /**
     * 将用户自定义标签转为系统标签
     */
    @PostMapping("/{type}/{id}/convert")
    public ApiResult convertToSystemTag(
        @PathVariable String type,
        @PathVariable Integer id
    ) {
        return switch (type) {
            case "topic" -> topicTagService.convertToSystemTag(id);
            case "product" -> productTagService.convertToSystemTag(id);
            case "location" -> locationTagService.convertToSystemTag(id);
            default -> ApiResult.error(400, "无效的标签类型");
        };
    }
}
