package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.ProductSpecification;

import java.util.List;

/**
 * 商品规格服务接口
 */
public interface ProductSpecificationService extends IService<ProductSpecification> {
    
    /**
     * 根据商品 ID 获取所有规格
     */
    List<ProductSpecification> getByProductId(Integer productId);
    
    /**
     * 为商品批量保存规格
     */
    void batchSaveForProduct(Integer productId, List<ProductSpecification> specifications);
    
    /**
     * 删除商品的所有规格
     */
    void deleteByProductId(Integer productId);
}
