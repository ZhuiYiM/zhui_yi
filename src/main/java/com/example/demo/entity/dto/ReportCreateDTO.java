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
    private Long targetId; // 被举报的话题 ID、评论 ID、商品 ID、地点 ID
    
    @NotBlank(message = "被举报类型不能为空")
    private String targetType; // 被举报类型：topic-话题、comment-评论、product-商品、location-地点
    
    @NotBlank(message = "举报类型不能为空")
    private String reportType; // 举报类型：pornography-色情低俗、illegal-违法犯罪、political-政治敏感、spam-垃圾广告、fake_info-虚假信息、copyright-侵权内容、harassment-人身攻击、other-其他
    
    private String reason; // 举报原因描述（已废弃，使用 reportType 代替）
    
    private String description; // 详细描述
    
    private String evidence; // 证据（JSON 格式，包含图片和其他证据）
    
    private String images; // 图片 URL（JSON 数组格式）
    
    private String contactInfo; // 联系方式
}
