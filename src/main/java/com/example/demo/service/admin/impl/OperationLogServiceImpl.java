package com.example.demo.service.admin.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.admin.OperationLog;
import com.example.demo.mapper.admin.OperationLogMapper;
import com.example.demo.service.admin.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现类
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
