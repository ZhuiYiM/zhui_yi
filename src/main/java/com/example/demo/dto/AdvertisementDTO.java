package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 广告类型：activity-活动标签筛选，merchant-商家页面，product-商品广告
     */
    private String adType;

    /**
     * 关联 ID：商品 ID、商家 ID 等
     */
    private Long relatedId;

    /**
     * 筛选标签：JSON 格式
     */
    private String filterTags;
}
