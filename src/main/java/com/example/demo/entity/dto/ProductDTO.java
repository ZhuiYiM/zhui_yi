// entity/dto/ProductDTO.java
package com.example.demo.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDTO {
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice; // 原价
    private Integer categoryId;
    private List<String> images;
    private String contactInfo; // 联系方式
    private List<String> tradeMethods; // 交易方式
    private String location; // 交易地点
}
