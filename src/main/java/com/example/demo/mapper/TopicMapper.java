// mapper/TopicMapper.java
package com.example.demo.mapper;

import com.example.demo.entity.Topic;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
}
