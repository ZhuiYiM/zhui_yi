package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Advertisement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 广告 Mapper 接口
 */
@Mapper
public interface AdvertisementMapper extends BaseMapper<Advertisement> {
}
