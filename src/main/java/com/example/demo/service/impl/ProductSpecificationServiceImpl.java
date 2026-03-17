package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.ProductSpecification;
import com.example.demo.mapper.ProductSpecificationMapper;
import com.example.demo.service.ProductSpecificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品规格服务实现
 */
@Service
public class ProductSpecificationServiceImpl extends ServiceImpl<ProductSpecificationMapper, ProductSpecification> 
    implements ProductSpecificationService {
    
    @Override
    public List<ProductSpecification> getByProductId(Integer productId) {
        LambdaQueryWrapper<ProductSpecification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductSpecification::getProductId, productId)
               .orderByAsc(ProductSpecification::getSortOrder)
               .orderByAsc(ProductSpecification::getSpecName)
               .orderByAsc(ProductSpecification::getSpecValue);
        return list(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSaveForProduct(Integer productId, List<ProductSpecification> specifications) {
        if (specifications == null || specifications.isEmpty()) {
            return;
        }
        
        // 先删除旧的规格
        deleteByProductId(productId);
        
        // 设置商品 ID 并保存新规格
        for (ProductSpecification spec : specifications) {
            spec.setProductId(productId);
            if (spec.getStockQuantity() == null) {
                spec.setStockQuantity(0);
            }
            if (spec.getPriceAdjustment() == null) {
                spec.setPriceAdjustment(java.math.BigDecimal.ZERO);
            }
            if (spec.getIsDefault() == null) {
                spec.setIsDefault(0);
            }
            if (spec.getSortOrder() == null) {
                spec.setSortOrder(0);
            }
        }
        
        // 批量插入
        baseMapper.batchInsert(specifications);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByProductId(Integer productId) {
        baseMapper.deleteByProductId(productId);
    }
}
