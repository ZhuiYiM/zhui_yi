package com.example.demo.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户位置标记创建 DTO
 */
@Data
public class UserLocationMarkCreateDTO {
    
    /**
     * 校区 ID
     */
    private Integer campusId;
    
    /**
     * 位置名称
     */
    private String locationName;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 详细地址描述
     */
    private String addressDetail;
    
    /**
     * 标记类型：merchant_shop/organization_activity/meeting_point
     */
    private String markType;
    
    /**
     * 地点分类：building/area/facility/other
     */
    private String markCategory;
    
    /**
     * 位置描述
     */
    private String description;
    
    /**
     * 联系方式
     */
    private String contactInfo;
    
    /**
     * 图片 URL 列表
     */
    private List<String> images;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 是否永久标记
     */
    private Boolean isPermanent;
    
    /**
     * 可见性：public/friends/private
     */
    private String visibility;
}
