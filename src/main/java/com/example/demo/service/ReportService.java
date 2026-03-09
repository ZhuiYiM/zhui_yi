package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Report;
import com.example.demo.entity.dto.ReportCreateDTO;
import com.example.demo.common.ApiResult;

/**
 * 举报服务接口
 */
public interface ReportService extends IService<Report> {
    
    /**
     * 创建举报记录
     */
    ApiResult createReport(ReportCreateDTO reportDTO, Long userId);
    
    /**
     * 获取举报列表（管理员）
     */
    ApiResult getReportList(Integer page, Integer size, String status);
    
    /**
     * 处理举报（管理员）
     */
    ApiResult processReport(Long reportId, Integer status, String result, Long processorId);
}
