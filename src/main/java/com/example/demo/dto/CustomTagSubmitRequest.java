package com.example.demo.dto;

import lombok.Data;

/**
 * 用户提交自定义标签请求 DTO
 */
@Data
public class CustomTagSubmitRequest {
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 补充说明
     */
    private String description;
}
