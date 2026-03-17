package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.OrderCancelReason;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单取消原因 Mapper
 */
@Mapper
public interface OrderCancelReasonMapper extends BaseMapper<OrderCancelReason> {
}
