package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Tags;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签Mapper接口
 */
@Mapper
public interface TagsMapper extends BaseMapper<Tags> {
}