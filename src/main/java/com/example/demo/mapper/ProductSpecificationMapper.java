package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.ProductSpecification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品规格 Mapper
 */
@Mapper
public interface ProductSpecificationMapper extends BaseMapper<ProductSpecification> {
    
    /**
     * 根据商品 ID 获取所有规格
     */
    List<ProductSpecification> selectByProductId(@Param("productId") Integer productId);
    
    /**
     * 批量插入规格
     */
    int batchInsert(@Param("specifications") List<ProductSpecification> specifications);
    
    /**
     * 删除商品的所有规格
     */
    int deleteByProductId(@Param("productId") Integer productId);
}
