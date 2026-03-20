package com.example.demo.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
@TableName("admin_operation_log")
public class OperationLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long adminId;
    
    private String adminName;
    
    private String operation;
    
    private String module;
    
    private Long targetId;
    
    private String detail;
    
    private String ipAddress;
    
    private LocalDateTime createdAt;
}
