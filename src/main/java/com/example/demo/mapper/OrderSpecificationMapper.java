package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.OrderSpecification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单规格关联 Mapper
 */
@Mapper
public interface OrderSpecificationMapper extends BaseMapper<OrderSpecification> {
    
    /**
     * 根据订单 ID 获取所有规格
     */
    List<OrderSpecification> selectByOrderId(@Param("orderId") Integer orderId);
    
    /**
     * 批量插入订单规格
     */
    int batchInsert(@Param("specifications") List<OrderSpecification> specifications);
}
