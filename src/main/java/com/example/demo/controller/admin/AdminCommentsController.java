package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Comments;
import com.example.demo.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 评论管理控制器
 */
@RestController
@RequestMapping("/admin/comments")
@CrossOrigin
public class AdminCommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 分页查询评论列表
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        try {
            QueryWrapper<Comments> queryWrapper = new QueryWrapper<>();
            
            // 关键词搜索
            if (keyword != null && !keyword.isEmpty()) {
                queryWrapper.and(wrapper -> 
                    wrapper.like("content", keyword)
                           .or()
                           .like("user_id", keyword)
                );
            }
            
            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            
            queryWrapper.orderByDesc("created_at");
            
            Page<Comments> commentPage = new Page<>(page, limit);
            Page<Comments> result = commentsService.page(commentPage, queryWrapper);
            
            return Result.success(Map.of(
                "list", result.getRecords(),
                "total", result.getTotal(),
                "page", page,
                "limit", limit
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            boolean success = commentsService.removeById(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /**
     * 更新评论状态
     */
    @PutMapping("/{id}/status")
    public Result updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Comments comment = commentsService.getById(id);
            if (comment == null) {
                return Result.error("评论不存在");
            }
            
            Integer status = (Integer) params.get("status");
            if (status == null) {
                return Result.error("状态不能为空");
            }
            
            comment.setStatus(status);
            boolean success = commentsService.updateById(comment);
            
            if (success) {
                return Result.success("更新成功", comment);
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败：" + e.getMessage());
        }
    }
}
