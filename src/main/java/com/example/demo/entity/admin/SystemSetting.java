package com.example.demo.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统设置实体类
 */
@Data
@TableName("system_settings")
public class SystemSetting {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String settingKey;
    
    private String settingValue;
    
    private String description;
    
    private LocalDateTime updatedAt;
}
