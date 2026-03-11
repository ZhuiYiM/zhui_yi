package com.example.demo.entity.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 分享请求 DTO
 */
@Data
public class ShareRequestDTO {
    
    /**
     * 分享类型：topic-话题、product-商品
     */
    @NotBlank(message = "分享类型不能为空")
    private String shareType;
    
    /**
     * 被分享对象 ID
     */
    @NotNull(message = "分享对象 ID 不能为空")
    private Long targetId;
    
    /**
     * 分享标题（可选）
     */
    private String title;
    
    /**
     * 分享描述（可选）
     */
    private String description;
}
