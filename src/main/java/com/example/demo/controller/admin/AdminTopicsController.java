package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Topics;
import com.example.demo.service.TopicsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/topics")
@CrossOrigin
public class AdminTopicsController {

    @Autowired
    private TopicsService topicsService;

    /**
     * 分页查询话题列表
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword
    ) {
        try {
            QueryWrapper<Topics> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like("title", keyword)
                           .or()
                           .like("content", keyword)
                );
            }
            
            queryWrapper.orderByDesc("created_at");
            
            Page<Topics> topicPage = new Page<>(page, size);
            Page<Topics> result = topicsService.page(topicPage, queryWrapper);
            
            return Result.success(Map.of(
                "records", result.getRecords(),
                "total", result.getTotal(),
                "page", page,
                "size", size
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除话题
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            Topics topics = topicsService.getById(id);
            if (topics == null) {
                return Result.error("话题不存在");
            }
            
            boolean success = topicsService.removeById(id);
            if (success) {
                return Result.success("删除成功", null);
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 审核话题
     */
    @PutMapping("/{id}/review")
    public Result review(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            Topics topics = topicsService.getById(id);
            if (topics == null) {
                return Result.error("话题不存在");
            }
            
            Integer status = params.get("status");
            if (status == null) {
                return Result.error("审核状态不能为空");
            }
            
            // 1-通过，0-不通过
            topics.setStatus(status);
            boolean success = topicsService.updateById(topics);
            if (success) {
                return Result.success("审核成功", topics);
            } else {
                return Result.error("审核失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("审核失败：" + e.getMessage());
        }
    }
}
