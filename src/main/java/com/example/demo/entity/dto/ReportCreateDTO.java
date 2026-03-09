package com.example.demo.entity.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 举报请求 DTO
 */
@Data
public class ReportCreateDTO {
    
    @NotNull(message = "被举报对象 ID 不能为空")
    private Long targetId; // 被举报的话题 ID 或评论 ID
    
    @NotBlank(message = "举报类型不能为空")
    private String reportType; // 举报类型：spam-垃圾广告、illegal-违法违规、fraud-诈骗、harassment-骚扰、other-其他
    
    private String reason; // 举报原因描述
    
    private String evidence; // 证据（可选）
}
