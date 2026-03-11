package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 分享信息响应 DTO
 */
@Data
public class ShareResponseDTO {
    
    /**
     * 分享链接
     */
    private String shareUrl;
    
    /**
     * 短链接（可选）
     */
    private String shortUrl;
    
    /**
     * 分享标题
     */
    private String title;
    
    /**
     * 分享描述
     */
    private String description;
    
    /**
     * 分享图片（可选）
     */
    private String imageUrl;
    
    /**
     * 分享类型
     */
    private String shareType;
    
    /**
     * 原始对象 ID
     */
    private Long targetId;
}
