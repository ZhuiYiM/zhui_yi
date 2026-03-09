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
    private String level1TagCode;      // 一级标签代码
    private List<String> level2TagCodes;  // 二级标签代码列表
    private List<String> level3TagCodes;  // 三级标签代码列表
    private List<String> level4TagCodes;  // 四级标签代码列表
}