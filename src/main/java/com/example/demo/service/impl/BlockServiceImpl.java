// service/impl/BlockServiceImpl.java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.User;
import com.example.demo.entity.UserBlock;
import com.example.demo.entity.dto.BlockListDTO;
import com.example.demo.mapper.UserBlockMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.BlockService;
import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户拉黑服务实现类
 */
@Service
public class BlockServiceImpl extends ServiceImpl<UserBlockMapper, UserBlock> implements BlockService {

    @Autowired
    private UserBlockMapper userBlockMapper;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public Result blockUser(Integer blockerId, Integer blockedId) {
        // 不能拉黑自己
        if (blockerId.equals(blockedId)) {
            return Result.error("不能拉黑自己");
        }
        
        // 检查是否已经拉黑
        Integer exists = userBlockMapper.existsBlock(blockerId, blockedId);
        if (exists != null && exists > 0) {
            return Result.error("该用户已在拉黑列表中");
        }
        
        // 创建拉黑关系
        UserBlock userBlock = new UserBlock();
        userBlock.setBlockerId(blockerId);
        userBlock.setBlockedId(blockedId);
        userBlock.setCreatedAt(LocalDateTime.now());
        userBlock.setUpdatedAt(LocalDateTime.now());
        
        boolean result = this.save(userBlock);
        if (result) {
            return Result.success("拉黑成功");
        } else {
            return Result.error("拉黑失败");
        }
    }

    @Override
    public Result unblockUser(Integer blockerId, Integer blockedId) {
        // 查询拉黑记录
        QueryWrapper<UserBlock> wrapper = new QueryWrapper<>();
        wrapper.eq("blocker_id", blockerId)
               .eq("blocked_id", blockedId);
        
        UserBlock userBlock = this.getOne(wrapper);
        if (userBlock == null) {
            return Result.error("该用户不在拉黑列表中");
        }
        
        // 删除拉黑关系
        boolean result = this.remove(wrapper);
        if (result) {
            return Result.success("取消拉黑成功");
        } else {
            return Result.error("取消拉黑失败");
        }
    }

    @Override
    public Result getBlockList(Integer blockerId, Integer page, Integer size) {
        // 查询拉黑列表
        QueryWrapper<UserBlock> wrapper = new QueryWrapper<>();
        wrapper.eq("blocker_id", blockerId)
               .orderByDesc("created_at");
        
        Page<UserBlock> blockPage = new Page<>(page, size);
        Page<UserBlock> result = this.page(blockPage, wrapper);
        
        // 转换为 DTO，包含被拉黑者的用户信息
        List<BlockListDTO> dtoList = new ArrayList<>();
        for (UserBlock block : result.getRecords()) {
            BlockListDTO dto = new BlockListDTO();
            dto.setId(block.getId());
            dto.setBlockedId(block.getBlockedId());
            
            // 获取被拉黑者的用户信息
            User blockedUser = userMapper.selectById(block.getBlockedId());
            if (blockedUser != null) {
                dto.setBlockedName(blockedUser.getUsername());
                dto.setBlockedAvatar(blockedUser.getAvatarUrl());
            }
            
            dto.setCreatedAt(block.getCreatedAt().toString());
            dtoList.add(dto);
        }
        
        // 构建返回数据
        java.util.Map<String, Object> responseData = new java.util.HashMap<>();
        responseData.put("list", dtoList);
        responseData.put("total", result.getTotal());
        responseData.put("page", result.getCurrent());
        responseData.put("size", result.getSize());
        
        return Result.success(responseData);
    }

    @Override
    public Result isBlocked(Integer blockerId, Integer blockedId) {
        Integer exists = userBlockMapper.existsBlock(blockerId, blockedId);
        boolean isBlocked = exists != null && exists > 0;
        return Result.success(isBlocked);
    }
}
