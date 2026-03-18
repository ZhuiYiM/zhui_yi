package com.example.demo.entity.admin;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("admin_role")
public class AdminRole {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String roleName;
    private String roleCode;
    private String permissions;
    private String description;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
