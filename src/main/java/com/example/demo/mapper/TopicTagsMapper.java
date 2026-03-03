package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TopicTags;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题标签关联Mapper接口
 */
@Mapper
public interface TopicTagsMapper extends BaseMapper<TopicTags> {
}