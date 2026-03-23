package com.example.demo.dto;

import lombok.Data;

/**
 * 广告查询参数 DTO
 */
@Data
public class AdvertisementQueryDTO {

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 广告标题（模糊查询）
     */
    private String title;

    /**
     * 广告位置
     */
    private String position;

    /**
     * 是否启用
     */
    private Integer isActive;
}
