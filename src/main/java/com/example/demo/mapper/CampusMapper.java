package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Campus;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CampusMapper extends BaseMapper<Campus> {
}
