package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("privacy_settings")
public class PrivacySetting {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String fieldName;           // 字段名：phone, email, studentId, realName
    private String visibilityLevel;     // 可见性级别：public, friends, private
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Getter and Setter methods
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    
    public String getVisibilityLevel() { return visibilityLevel; }
    public void setVisibilityLevel(String visibilityLevel) { this.visibilityLevel = visibilityLevel; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}