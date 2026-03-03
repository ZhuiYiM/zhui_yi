package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.UserFollows;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注Mapper接口
 */
@Mapper
public interface UserFollowsMapper extends BaseMapper<UserFollows> {
}