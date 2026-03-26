package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.Report;
import com.example.demo.entity.admin.OperationLog;
import com.example.demo.service.admin.OperationLogService;
import com.example.demo.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/reports")
@CrossOrigin
public class AdminReportsController {

    @Autowired
    private ReportService reportService;

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
     * 分页查询举报列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Integer status
    ) {
        try {
            QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
            
            // 类型筛选（被举报对象类型）
            if (targetType != null && !targetType.isEmpty()) {
                queryWrapper.eq("target_type", targetType);
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
                // 记录操作日志
                String statusDesc = switch (status) {
                    case 0 -> "待处理";
                    case 1 -> "处理中";
                    case 2 -> "已处理";
                    case 3 -> "已忽略";
                    default -> "未知状态";
                };
                logOperation("update", "report", Long.valueOf(id), "处理举报：" + statusDesc + (processNote != null ? ", 备注：" + processNote : ""));
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
                // 记录操作日志
                logOperation("delete", "report", Long.valueOf(id), "删除举报，ID: " + id);
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
            
            // 按类型统计（被举报对象类型）
            Map<String, Object> byType = Map.of(
                "topic", reportService.count(new QueryWrapper<Report>().eq("target_type", "topic")),
                "product", reportService.count(new QueryWrapper<Report>().eq("target_type", "product")),
                "user", reportService.count(new QueryWrapper<Report>().eq("target_type", "user")),
                "comment", reportService.count(new QueryWrapper<Report>().eq("target_type", "comment")),
                "location", reportService.count(new QueryWrapper<Report>().eq("target_type", "location"))
            );
            
            // 按状态统计
            Map<String, Object> byStatus = Map.of(
                "pending", pending,
                "processing", processing,
                "processed", processed,
                "ignored", ignored
            );
            
            // 最近处理记录（按处理时间倒序）
            QueryWrapper<Report> recentWrapper = new QueryWrapper<>();
            recentWrapper.in("status", 2, 3)
                      .orderByDesc("processed_at")
                      .last("LIMIT 10");
            
            return Result.success(Map.of(
                "total", total,
                "pending", pending,
                "processing", processing,
                "processed", processed,
                "ignored", ignored,
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
