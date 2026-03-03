package com.example.demo.mapper;

import com.example.demo.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
