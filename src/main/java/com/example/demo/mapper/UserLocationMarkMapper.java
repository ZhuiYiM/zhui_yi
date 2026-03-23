package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.UserLocationMark;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户位置标记 Mapper
 */
@Mapper
public interface UserLocationMarkMapper extends BaseMapper<UserLocationMark> {
}
