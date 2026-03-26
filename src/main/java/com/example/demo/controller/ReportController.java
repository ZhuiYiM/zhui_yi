package com.example.demo.controller;

import com.example.demo.common.ApiResult;
import com.example.demo.entity.dto.ReportCreateDTO;
import com.example.demo.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * 举报控制器
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    /**
     * 创建举报记录
     */
    @PostMapping
    public ApiResult createReport(@Valid @RequestBody ReportCreateDTO reportDTO,
                                  HttpServletRequest request) {
        // 从 request attribute 中获取用户 ID（由 TokenInterceptor 设置）
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return ApiResult.error(401, "未登录或 Token 无效");
        }
        return reportService.createReport(reportDTO, userId);
    }
    
    /**
     * 获取举报列表（管理员）
     */
    @GetMapping
    public ApiResult getReportList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return reportService.getReportList(page, size, status);
    }
    
    /**
     * 处理举报（管理员）
     */
    @PutMapping("/{id}/process")
    public ApiResult processReport(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String result,
            HttpServletRequest request) {
        // TODO: 从 token 中获取管理员 ID
        Long processorId = 1L; // 临时使用固定值
        return reportService.processReport(id, status, result, processorId);
    }
}
