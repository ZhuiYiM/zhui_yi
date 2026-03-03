// entity/dto/TopicDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

import java.util.List;

@Data
public class TopicDTO {
    private String title;
    private String content;
    private List<String> tags;
}
