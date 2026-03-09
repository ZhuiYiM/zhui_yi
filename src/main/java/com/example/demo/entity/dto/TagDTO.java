// entity/dto/TagDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

/**
 * 标签数据传输对象
 */
@Data
public class TagDTO {
    
    private Long id;
    private String code;
    private String name;
    private String icon;
    private String color;
    private Integer sortOrder;
    private Integer usageCount;
    private Boolean isActive;
}
