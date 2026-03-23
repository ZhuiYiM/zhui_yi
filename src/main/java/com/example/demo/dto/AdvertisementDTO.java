package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 广告创建/更新 DTO
 */
@Data
public class AdvertisementDTO {

    /**
     * 广告 ID（更新时需要）
     */
    private Long id;

    /**
     * 广告标题
     */
    private String title;

    /**
     * 广告图片 URL
     */
    private String imageUrl;

    /**
     * 跳转链接
     */
    private String linkUrl;

    /**
     * 广告内容描述
     */
    private String content;

    /**
     * 广告位置
     */
    private String position;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Integer isActive;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}
