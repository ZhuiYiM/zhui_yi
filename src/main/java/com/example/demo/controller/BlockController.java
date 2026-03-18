// controller/BlockController.java
package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.dto.BlockCreateDTO;
import com.example.demo.service.BlockService;
import com.example.demo.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 用户拉黑控制器
 */
@RestController
@RequestMapping("/api/blocks")
@CrossOrigin
public class BlockController {

    @Autowired
    private BlockService blockService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 从 Token 中提取用户 ID
     */
    private Integer extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token != null && !token.trim().isEmpty()) {
            return jwtUtil.getUserIdFromToken(token);
        }
        return null;
    }

    /**
     * 拉黑用户
     */
    @PostMapping
    public Result blockUser(@RequestBody BlockCreateDTO dto,
                            @RequestHeader("Authorization") String token) {
        Integer blockerId = extractUserIdFromToken(token);
        if (blockerId == null) {
            return Result.error(401, "用户未登录");
        }
        
        if (dto.getBlockedId() == null) {
            return Result.error("被拉黑用户 ID 不能为空");
        }
        
        return blockService.blockUser(blockerId, dto.getBlockedId());
    }

    /**
     * 取消拉黑用户
     */
    @DeleteMapping("/{blockedId}")
    public Result unblockUser(@PathVariable Integer blockedId,
                              @RequestHeader("Authorization") String token) {
        Integer blockerId = extractUserIdFromToken(token);
        if (blockerId == null) {
            return Result.error(401, "用户未登录");
        }
        
        return blockService.unblockUser(blockerId, blockedId);
    }

    /**
     * 获取拉黑列表
     */
    @GetMapping("/list")
    public Result getBlockList(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestHeader("Authorization") String token) {
        Integer blockerId = extractUserIdFromToken(token);
        if (blockerId == null) {
            return Result.error(401, "用户未登录");
        }
        
        return blockService.getBlockList(blockerId, page, size);
    }

    /**
     * 检查是否被拉黑
     */
    @GetMapping("/check")
    public Result isBlocked(@RequestParam Integer userId,
                            @RequestHeader("Authorization") String token) {
        Integer blockerId = extractUserIdFromToken(token);
        if (blockerId == null) {
            return Result.error(401, "用户未登录");
        }
        
        return blockService.isBlocked(blockerId, userId);
    }
}
