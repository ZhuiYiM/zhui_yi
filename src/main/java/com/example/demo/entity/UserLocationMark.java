package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户位置标记实体类
 */
@Data
@TableName("user_location_mark")
public class UserLocationMark {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 标记者用户 ID
     */
    private Long userId;
    
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
     * 用户身份类型：student/merchant/organization
     */
    private String userIdentityType;
    
    /**
     * 关联的商户 ID 或组织 ID
     */
    private Long relatedEntityId;
    
    /**
     * 位置描述
     */
    private String description;
    
    /**
     * 联系方式
     */
    private String contactInfo;
    
    /**
     * 图片 URL 数组（JSON 格式）
     */
    private String images;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 是否永久标记：0-临时，1-永久
     */
    private Integer isPermanent;
    
    /**
     * 审核状态：pending/approved/rejected
     */
    private String verificationStatus;
    
    /**
     * 审核通过时间
     */
    private LocalDateTime verifiedAt;
    
    /**
     * 审核员 ID
     */
    private Long reviewerId;
    
    /**
     * 审核意见
     */
    private String reviewComment;
    
    /**
     * 可见性：public/friends/private
     */
    private String visibility;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 是否有效：0-无效，1-有效
     */
    private Integer isActive;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}
