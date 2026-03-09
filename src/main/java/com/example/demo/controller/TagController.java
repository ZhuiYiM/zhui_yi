// controller/TagController.java
package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签管理控制器
 * 提供四级标签系统相关的 RESTful API 接口
 */
@RestController
@RequestMapping("/api/topic/tags")
@CrossOrigin
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    /**
     * 获取一级标签（用户身份）
     * GET /api/topic/tags/level1
     */
    @GetMapping("/level1")
    public ApiResult getLevel1Tags() {
        return tagService.getLevel1Tags();
    }
    
    /**
     * 获取二级标签（话题分类）
     * GET /api/topic/tags/level2
     */
    @GetMapping("/level2")
    public ApiResult getLevel2Tags() {
        return tagService.getLevel2Tags();
    }
    
    /**
     * 获取三级标签（地点实体）
     * GET /api/topic/tags/level3
     */
    @GetMapping("/level3")
    public ApiResult getLevel3Tags() {
        return tagService.getLevel3Tags();
    }
    
    /**
     * 获取四级标签列表（支持搜索和分页）
     * GET /api/topic/tags/level4?keyword=关键词&page=1&size=20
     */
    @GetMapping("/level4")
    public ApiResult getLevel4Tags(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return tagService.getLevel4Tags(keyword, page, size);
    }
    
    /**
     * 获取热门标签
     * GET /api/topic/tags/hot?limit=20
     */
    @GetMapping("/hot")
    public ApiResult getHotTags(@RequestParam(defaultValue = "20") Integer limit) {
        return tagService.getHotTags(limit);
    }
    
    /**
     * 批量创建或获取四级标签
     * POST /api/topic/tags/level4/batch
     * Body: {"tagNames": ["编程", "考研", "兼职"]}
     */
    @PostMapping("/level4/batch")
    public ApiResult createOrGetLevel4Tags(@RequestBody Map<String, List<String>> requestBody) {
        List<String> tagNames = requestBody.get("tagNames");
        return tagService.createOrGetLevel4Tags(tagNames);
    }
    
    /**
     * 获取热门标签组合
     * GET /api/topic/tags/hot/combo
     */
    @GetMapping("/hot/combo")
    public ApiResult getHotTagsCombo() {
        return tagService.getHotTagsCombo();
    }
}
