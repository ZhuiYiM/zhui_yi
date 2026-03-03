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
    
    @Size(max = 5, message = "标签数量不能超过5个")
    private List<String> tags;
}