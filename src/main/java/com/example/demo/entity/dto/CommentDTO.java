// entity/dto/CommentDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

@Data
public class CommentDTO {
    private Integer topicId;
    private Integer parentId; // 父评论ID
    private String content;
}
