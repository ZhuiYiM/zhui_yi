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
}