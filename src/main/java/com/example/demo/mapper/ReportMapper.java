package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报 Mapper 接口
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
