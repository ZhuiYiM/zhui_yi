package com.example.demo.mapper;

import com.example.demo.entity.Location;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

@Mapper
public interface LocationMapper extends BaseMapper<Location> {
}
