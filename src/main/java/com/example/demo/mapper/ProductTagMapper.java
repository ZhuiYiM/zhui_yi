package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.ProductTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品标签 Mapper 接口
 */
@Mapper
public interface ProductTagMapper extends BaseMapper<ProductTag> {
}
