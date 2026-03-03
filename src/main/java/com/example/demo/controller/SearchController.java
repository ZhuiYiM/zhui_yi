// controller/SearchController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@CrossOrigin
public class SearchController {

    @Autowired
    private SearchService searchService;

    // 综合搜索
    @GetMapping("/all")
    public Result searchAll(@RequestParam String keyword,
                            @RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer size) {
        return searchService.searchAll(keyword, page, size);
    }

    // 搜索商品
    @GetMapping("/products")
    public Result searchProducts(@RequestParam String keyword,
                                 @RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer size) {
        return searchService.searchProducts(keyword, page, size);
    }

    // 搜索话题
    @GetMapping("/topics")
    public Result searchTopics(@RequestParam String keyword,
                               @RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size) {
        return searchService.searchTopics(keyword, page, size);
    }

    // 搜索用户
    @GetMapping("/users")
    public Result searchUsers(@RequestParam String keyword,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size) {
        return searchService.searchUsers(keyword, page, size);
    }
}
