package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.TopicLikes;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题点赞Mapper接口
 */
@Mapper
public interface TopicLikesMapper extends BaseMapper<TopicLikes> {
}