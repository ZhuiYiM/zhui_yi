package com.example.demo.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_operation_log")
public class AdminOperationLog {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer adminId;
    private String adminName;
    private String operationType;
    private String operationModule;
    private String operationTarget;
    private String operationDetail;
    private String requestMethod;
    private String requestUrl;
    private String ipAddress;
    private String userAgent;
    private Integer executionTime;
    private Integer status;
    private String errorMessage;
    private LocalDateTime createdAt;
}
