package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.common.ApiResult;
import com.example.demo.entity.Report;
import com.example.demo.entity.dto.ReportCreateDTO;
import com.example.demo.mapper.ReportMapper;
import com.example.demo.service.ReportService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 举报服务实现类
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
    
    @Override
    public ApiResult createReport(ReportCreateDTO reportDTO, Long userId) {
        try {
            Report report = new Report();
            report.setReporterId(userId);
            report.setTargetId(reportDTO.getTargetId());
            report.setTargetType(reportDTO.getTargetType());
            report.setReportType(reportDTO.getReportType());
            
            // 合并 description 和 reason 字段
            String finalReason = reportDTO.getDescription() != null ? reportDTO.getDescription() : reportDTO.getReason();
            report.setReason(finalReason);
            
            // 处理证据和图片
            StringBuilder evidenceBuilder = new StringBuilder();
            if (reportDTO.getImages() != null && !reportDTO.getImages().isEmpty()) {
                evidenceBuilder.append("图片：").append(reportDTO.getImages());
            }
            if (reportDTO.getEvidence() != null && !reportDTO.getEvidence().isEmpty()) {
                if (evidenceBuilder.length() > 0) {
                    evidenceBuilder.append("; ");
                }
                evidenceBuilder.append(reportDTO.getEvidence());
            }
            report.setEvidence(evidenceBuilder.toString());
            
            report.setStatus(0);
            report.setCreatedAt(LocalDateTime.now());
            
            boolean saved = this.save(report);
            
            if (saved) {
                return ApiResult.success("举报成功，我们会尽快处理");
            } else {
                return ApiResult.error(500, "举报失败，请稍后重试");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "举报失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult getReportList(Integer page, Integer size, String status) {
        try {
            Page<Report> reportPage = new Page<>(page, size);
            QueryWrapper<Report> wrapper = new QueryWrapper<>();
            
            // 按状态筛选
            if (status != null && !status.trim().isEmpty()) {
                wrapper.eq("status", Integer.parseInt(status));
            }
            
            // 按创建时间倒序
            wrapper.orderByDesc("created_at");
            
            Page<Report> result = this.page(reportPage, wrapper);
            
            return ApiResult.success(result);
        } catch (Exception e) {
            return ApiResult.error(500, "获取举报列表失败：" + e.getMessage());
        }
    }
    
    @Override
    public ApiResult processReport(Long reportId, Integer status, String result, Long processorId) {
        try {
            Report report = this.getById(reportId);
            if (report == null) {
                return ApiResult.error(404, "举报记录不存在");
            }
            
            report.setStatus(status);
            report.setProcessedAt(LocalDateTime.now());
            report.setProcessorId(processorId);
            report.setProcessResult(result);
            
            boolean updated = this.updateById(report);
            if (updated) {
                return ApiResult.success("处理成功");
            } else {
                return ApiResult.error(500, "处理失败");
            }
        } catch (Exception e) {
            return ApiResult.error(500, "处理失败：" + e.getMessage());
        }
    }
    
    @Override
    public Long countPendingReports() {
        try {
            QueryWrapper<Report> wrapper = new QueryWrapper<>();
            wrapper.eq("status", 0); // 0 表示待处理
            return this.count(wrapper);
        } catch (Exception e) {
            return 0L;
        }
    }
}
