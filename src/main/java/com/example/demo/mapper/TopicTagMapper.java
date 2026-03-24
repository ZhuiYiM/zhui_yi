package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TopicTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题标签 Mapper 接口
 */
@Mapper
public interface TopicTagMapper extends BaseMapper<TopicTag> {
}
