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
    private Integer categoryId;
    private List<String> images;
}
