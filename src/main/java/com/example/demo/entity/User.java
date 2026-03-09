// entity/User.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.IdType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String realName;
    private String studentId;
    private String gender; // 性别
    private LocalDate birthDate; // 出生日期
    private String college; // 所在学院
    private String major; // 专业班级
    private String bio; // 个人简介
    private String hobbies; // 兴趣爱好（JSON格式存储）
    private Integer status;
    private Integer isVerified;
    private Integer isRealNameVerified;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String department;
    
    // 身份标识字段
    private String role;                // 角色：admin, user
    private Integer isAdmin;            // 是否管理员：0-否，1-是
    private Integer isMerchant;         // 是否商户：0-否，1-是
    private Integer isOrganization;     // 是否团体：0-否，1-是
}
