package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 话题查询参数DTO
 */
@Data
public class TopicQueryDTO {
    private Integer page = 1;
    private Integer size = 10;
    private String sort = "latest"; // latest|hot|essence
    private String tag;
    private String search;
    private Long userId;
    
    // 多级标签筛选字段
    private String level1Tag;        // 一级标签代码
    private String[] level2Tags;     // 二级标签代码数组
    private String[] level3Tags;     // 三级标签代码数组
}