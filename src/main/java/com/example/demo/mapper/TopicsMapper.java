package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Topics;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题Mapper接口
 */
@Mapper
public interface TopicsMapper extends BaseMapper<Topics> {
}