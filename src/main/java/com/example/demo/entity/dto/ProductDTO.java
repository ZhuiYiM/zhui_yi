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
    private List<SpecificationDTO> specifications; // 商品规格列表
    
    /**
     * 规格 DTO
     */
    @Data
    public static class SpecificationDTO {
        private String specName; // 规格名称（如：颜色、尺寸）
        private String specValue; // 规格值（如：红色、XL）
        private String specIcon; // 规格图标或颜色代码
        private Integer stockQuantity; // 库存数量
        private BigDecimal priceAdjustment; // 价格调整
        private Integer isDefault; // 是否默认
        private Integer sortOrder; // 排序顺序
    }
}
