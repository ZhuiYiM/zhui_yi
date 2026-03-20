package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Report;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/reports")
@CrossOrigin
public class AdminReportsController {

    @Autowired
    private ReportService reportService;

    /**
     * 分页查询举报列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status
    ) {
        try {
            QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
            
            // 类型筛选
            if (type != null && !type.isEmpty()) {
                queryWrapper.eq("type", type);
            }
            
            // 状态筛选
            if (status != null) {
                queryWrapper.eq("status", status);
            }
            
            queryWrapper.orderByDesc("created_at");
            
            Page<Report> reportPage = new Page<>(page, limit);
            Page<Report> result = reportService.page(reportPage, queryWrapper);
            
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
     * 处理举报
     */
    @PostMapping("/process/{id}")
    public Result handle(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        try {
            Report report = reportService.getById(id);
            if (report == null) {
                return Result.error("举报不存在");
            }
            
            Integer status = (Integer) params.get("status");
            String processNote = (String) params.get("processNote");
            
            if (status == null) {
                return Result.error("处理状态不能为空");
            }
            
            // 0-待处理，1-处理中，2-已处理，3-已忽略
            report.setStatus(status);
            report.setProcessResult(processNote);
            
            boolean success = reportService.updateById(report);
            if (success) {
                return Result.success("处理成功", report);
            } else {
                return Result.error("处理失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("处理失败：" + e.getMessage());
        }
    }

    /**
     * 删除举报
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        try {
            boolean success = reportService.removeById(id);
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
     * 获取举报统计
     */
    @GetMapping("/stats")
    public Result stats() {
        try {
            long total = reportService.count();
            long pending = reportService.count(new QueryWrapper<Report>().eq("status", 0));
            long processing = reportService.count(new QueryWrapper<Report>().eq("status", 1));
            long processed = reportService.count(new QueryWrapper<Report>().eq("status", 2));
            long ignored = reportService.count(new QueryWrapper<Report>().eq("status", 3));
            
            // 按类型统计
            Map<String, Object> byType = Map.of(
                "topic", reportService.count(new QueryWrapper<Report>().eq("type", "topic")),
                "product", reportService.count(new QueryWrapper<Report>().eq("type", "product")),
                "user", reportService.count(new QueryWrapper<Report>().eq("type", "user")),
                "comment", reportService.count(new QueryWrapper<Report>().eq("type", "comment"))
            );
            
            // 按状态统计
            Map<String, Object> byStatus = Map.of(
                "pending", pending,
                "processing", processing,
                "processed", processed,
                "ignored", ignored
            );
            
            // 最近处理记录
            QueryWrapper<Report> recentWrapper = new QueryWrapper<>();
            recentWrapper.in("status", 2, 3)
                      .orderByDesc("updated_at")
                      .last("LIMIT 10");
            
            return Result.success(Map.of(
                "total", total,
                "pending", pending,
                "processing", processing,
                "processed", processed,
                "byType", byType,
                "byStatus", byStatus,
                "recent", reportService.list(recentWrapper)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
