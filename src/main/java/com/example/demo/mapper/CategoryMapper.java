package com.example.demo.mapper;

import com.example.demo.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
