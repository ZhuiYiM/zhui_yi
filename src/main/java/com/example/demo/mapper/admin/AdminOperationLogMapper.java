package com.example.demo.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.admin.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
