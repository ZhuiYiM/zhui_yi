package com.example.demo.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.common.Result;
import com.example.demo.entity.admin.OperationLog;
import com.example.demo.service.admin.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 操作日志控制器
 */
@RestController
@RequestMapping("/admin/logs")
@CrossOrigin
public class AdminLogsController {

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 分页查询操作日志
     */
    @GetMapping
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        try {
            QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
            
            // 模块筛选
            if (module != null && !module.isEmpty()) {
                queryWrapper.eq("module", module);
            }
            
            // 操作类型筛选
            if (operation != null && !operation.isEmpty()) {
                queryWrapper.eq("operation", operation);
            }
            
            // 操作人筛选
            if (operatorId != null) {
                queryWrapper.eq("operator_id", operatorId);
            }
            
            // 时间范围筛选
            if (startTime != null) {
                queryWrapper.ge("created_at", startTime);
            }
            if (endTime != null) {
                queryWrapper.le("created_at", endTime);
            }
            
            queryWrapper.orderByDesc("created_at");
            
            Page<OperationLog> logPage = new Page<>(page, limit);
            Page<OperationLog> result = operationLogService.page(logPage, queryWrapper);
            
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
     * 获取操作类型统计
     */
    @GetMapping("/stats")
    public Result stats() {
        try {
            // 按模块统计
            QueryWrapper<OperationLog> moduleWrapper = new QueryWrapper<>();
            moduleWrapper.select("module", "COUNT(*) as count")
                        .groupBy("module");
            
            // 按操作类型统计
            QueryWrapper<OperationLog> operationWrapper = new QueryWrapper<>();
            operationWrapper.select("operation", "COUNT(*) as count")
                           .groupBy("operation");
            
            // 最近操作记录
            QueryWrapper<OperationLog> recentWrapper = new QueryWrapper<>();
            recentWrapper.orderByDesc("created_at")
                        .last("LIMIT 10");
            
            return Result.success(Map.of(
                "byModule", operationLogService.listMaps(moduleWrapper),
                "byOperation", operationLogService.listMaps(operationWrapper),
                "recent", operationLogService.list(recentWrapper)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
