package com.example.demo.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * 商品详情响应 DTO（包含规格信息）
 */
@Data
public class ProductDetailResponseDTO {
    
    private ProductDTO product;
    private List<SpecificationGroupDTO> specifications;
    private Boolean isFavorite;
    
    /**
     * 规格组 DTO（按规格名称分组）
     */
    @Data
    public static class SpecificationGroupDTO {
        private String specName; // 规格名称（如：颜色）
        private List<SpecValueDTO> values; // 可选的规格值列表
        
        /**
         * 规格值 DTO
         */
        @Data
        public static class SpecValueDTO {
            private Integer specId; // 规格 ID
            private String specValue; // 规格值（如：红色）
            private String specIcon; // 图标或颜色代码
            private Integer stockQuantity; // 库存
            private String priceAdjustment; // 价格调整（字符串格式）
            private Boolean available; // 是否可用
        }
    }
}
