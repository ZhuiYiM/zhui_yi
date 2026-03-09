package com.example.demo.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagInfoDTO {
    /**
     * 标签代码
     */
    private String code;
    
    /**
     * 标签名称
     */
    private String name;
    
    /**
     * 图标（仅一级标签需要）
     */
    private String icon;
    
    /**
     * 颜色（仅二级标签需要）
     */
    private String color;
    
    /**
     * 类型（仅三级标签需要）
     */
    private String type;
    
    /**
     * 分类（仅四级标签需要）
     */
    private String category;
}
