// mapper/ProductMapper.java
package com.example.demo.mapper;

import com.example.demo.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
