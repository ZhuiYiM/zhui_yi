// entity/dto/LikeDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

@Data
public class LikeDTO {
    private String targetType; // topic, comment, product
    private Integer targetId;
}
