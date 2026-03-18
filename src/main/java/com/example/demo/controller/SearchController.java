package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 统一搜索控制器
 * 提供全站搜索功能，支持按类型和标签筛选
 */
@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 统一搜索接口
     * @param query 搜索关键词
     * @param type 搜索类型：all, location, product, topic, user
     * @param tags 标签（逗号分隔）
     * @param quickFilters 快速筛选（逗号分隔）
     * @return 搜索结果
     */
    @GetMapping
    public Result search(
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String quickFilters) {
        
        // 调用服务层进行搜索
        return searchService.searchAll(q, type, tags, quickFilters);
    }
    
    /**
     * 搜索地点（专用接口）
     */
    @GetMapping("/locations")
    public Result searchLocations(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer campusId,
            @RequestParam(required = false) String tags) {
        return searchService.searchLocations(keyword, campusId, tags);
    }
    
    /**
     * 搜索商品（专用接口）
     */
    @GetMapping("/products")
    public Result searchProducts(
            @RequestParam String keyword,
            @RequestParam(required = false) String tags) {
        return searchService.searchProducts(keyword, tags);
    }
    
    /**
     * 搜索话题（专用接口）
     */
    @GetMapping("/topics")
    public Result searchTopics(
            @RequestParam String keyword,
            @RequestParam(required = false) String tags) {
        return searchService.searchTopics(keyword, tags);
    }
    
    /**
     * 搜索用户（专用接口）
     */
    @GetMapping("/users")
    public Result searchUsers(@RequestParam String keyword) {
        return searchService.searchUsers(keyword);
    }
}
