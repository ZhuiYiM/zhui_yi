package com.example.demo.entity.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

import java.util.List;

/**
 * 话题创建DTO
 */
@Data
public class TopicCreateDTO {
    @NotBlank(message = "话题内容不能为空")
    @Size(max = 1000, message = "话题内容不能超过1000字符")
    private String content;
    
    @Size(max = 9, message = "图片数量不能超过9张")
    private List<String> images;
    
    @Size(max = 5, message = "标签数量不能超过 5 个")
    private List<String> tags;
    
    // 分级标签字段
    private String level1TagCode;      // 一级标签代码（身份标签）
    private List<String> topicTagCodes;   // 话题标签代码列表
    private List<String> productTagCodes; // 商品标签代码列表
    private List<String> locationTagCodes; // 地点标签代码列表
    
    // 转发相关字段
    private Boolean isForwarded;  // 是否为转发话题
    private Long forwardedFromTopicId;  // 被转发的话题 ID（如果是转发）
    private Long forwardedFromProductId;  // 被转发的商品 ID（如果是分享商品）
}