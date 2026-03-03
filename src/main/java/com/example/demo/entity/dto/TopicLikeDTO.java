package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 话题点赞DTO
 */
@Data
public class TopicLikeDTO {
    private String action; // "like" 或 "unlike"
}