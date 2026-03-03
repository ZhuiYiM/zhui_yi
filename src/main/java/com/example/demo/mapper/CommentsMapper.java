package com.example.demo.mapper;

import com.example.demo.entity.Comments;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface CommentsMapper extends BaseMapper<Comments> {
}
