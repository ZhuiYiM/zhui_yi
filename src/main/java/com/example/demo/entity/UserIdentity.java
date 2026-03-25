package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户身份实体类
 */
@Data
@TableName("user_identity")
public class UserIdentity {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户 ID
     */
    private Long userId;
    
    /**
     * 身份类型：student(学生)、staff(教职工)、merchant(商户)、organization(团体/部门)
     */
    private String identityType;
    
    /**
     * 身份显示名称
     */
    private String identityName;
    
    /**
     * 是否已认证：0-未认证，1-已认证
     */
    private Integer verified;
    
    /**
     * 认证通过时间
     */
    private LocalDateTime verifiedAt;
    
    /**
     * 过期时间（可选）
     */
    private LocalDateTime expiresAt;
    
    /**
     * 额外信息（JSON 格式）
     * 商户：店铺名称、营业执照 URL
     * 教职工：工号、部门
     * 团体：组织名称、负责人
     */
    private String extraInfo;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
