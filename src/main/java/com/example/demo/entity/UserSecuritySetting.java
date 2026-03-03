package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_security_settings")
public class UserSecuritySetting {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Boolean twoFactorAuth;          // 双因素认证
    private Boolean loginNotifications;     // 登录通知
    private Boolean deviceVerification;     // 新设备验证
    private LocalDateTime updatedAt;
}
