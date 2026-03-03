package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TopicComments;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题评论Mapper接口
 */
@Mapper
public interface TopicCommentsMapper extends BaseMapper<TopicComments> {
}