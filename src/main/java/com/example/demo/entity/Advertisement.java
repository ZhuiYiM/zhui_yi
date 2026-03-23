package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 广告实体类
 */
@Data
@TableName("advertisements")
public class Advertisement {

    /**
     * 广告 ID
     */
    @TableId(type = IdType.AUTO)
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
     * 广告位置：home-首页，topicwall-话题墙，mall-交易中心，map-地图
     */
    private String position;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 是否启用：0-禁用，1-启用
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
     * 关联 ID：商品 ID、商家 ID 等，根据广告类型而定
     */
    private Long relatedId;

    /**
     * 筛选标签：JSON 格式存储，用于活动类型广告的标签筛选
     */
    private String filterTags;

    /**
     * 点击次数
     */
    private Integer clickCount;

    /**
     * 展示次数
     */
    private Integer viewCount;

    /**
     * 创建人 ID
     */
    private Long createdBy;

    /**
     * 更新人 ID
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
