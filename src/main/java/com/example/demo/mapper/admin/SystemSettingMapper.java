package com.example.demo.mapper.admin;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.admin.SystemSetting;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统设置 Mapper
 */
@Mapper
public interface SystemSettingMapper extends BaseMapper<SystemSetting> {
}
