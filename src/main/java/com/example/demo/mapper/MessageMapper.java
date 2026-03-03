package com.example.demo.mapper;

import com.example.demo.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
