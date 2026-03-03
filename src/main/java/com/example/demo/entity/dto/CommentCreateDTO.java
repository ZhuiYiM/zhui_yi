package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 评论创建DTO
 */
@Data
public class CommentCreateDTO {
    private String content;
    private Long parentId; // 可选，回复某条评论时使用
}