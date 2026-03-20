package com.example.demo.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.admin.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
