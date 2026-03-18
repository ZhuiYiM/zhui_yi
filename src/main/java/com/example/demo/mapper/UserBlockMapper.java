// mapper/UserBlockMapper.java
package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.UserBlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户拉黑 Mapper 接口
 */
@Mapper
public interface UserBlockMapper extends BaseMapper<UserBlock> {
    
    /**
     * 检查用户是否被拉黑
     * @param blockerId 拉黑者 ID
     * @param blockedId 被拉黑者 ID
     * @return 是否存在拉黑关系
     */
    Integer existsBlock(@Param("blockerId") Integer blockerId, @Param("blockedId") Integer blockedId);
    
    /**
     * 获取用户的拉黑列表数量
     * @param blockerId 拉黑者 ID
     * @return 拉黑数量
     */
    Integer countBlockedUsers(@Param("blockerId") Integer blockerId);
}
