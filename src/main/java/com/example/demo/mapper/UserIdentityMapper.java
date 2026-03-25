package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.UserIdentity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户身份 Mapper 接口
 */
@Mapper
public interface UserIdentityMapper extends BaseMapper<UserIdentity> {
}
