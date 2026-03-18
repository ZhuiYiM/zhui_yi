// service/BlockService.java
package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.UserBlock;
import com.example.demo.common.Result;

/**
 * 用户拉黑服务接口
 */
public interface BlockService extends IService<UserBlock> {
    
    /**
     * 拉黑用户
     * @param blockerId 拉黑者 ID
     * @param blockedId 被拉黑者 ID
     * @return 操作结果
     */
    Result blockUser(Integer blockerId, Integer blockedId);
    
    /**
     * 取消拉黑用户
     * @param blockerId 拉黑者 ID
     * @param blockedId 被拉黑者 ID
     * @return 操作结果
     */
    Result unblockUser(Integer blockerId, Integer blockedId);
    
    /**
     * 获取拉黑列表
     * @param blockerId 拉黑者 ID
     * @param page 页码
     * @param size 每页数量
     * @return 拉黑列表
     */
    Result getBlockList(Integer blockerId, Integer page, Integer size);
    
    /**
     * 检查是否被拉黑
     * @param blockerId 拉黑者 ID
     * @param blockedId 被拉黑者 ID
     * @return 是否被拉黑
     */
    Result isBlocked(Integer blockerId, Integer blockedId);
}
