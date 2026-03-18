package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.MessageService;
import com.example.demo.utils.JwtUtil;  // 添加导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private JwtUtil jwtUtil;  // 添加 JwtUtil 注入

    // 获取消息列表
    @GetMapping
    public Result getMessages(@RequestParam(required = false, defaultValue = "") String type,
                              @RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "10") Integer size,
                              @RequestHeader("Authorization") String token) {
        Integer userId = extractUserIdFromToken(token);
        return messageService.getMessagesByTypeAndUser(type, page, size, userId);
    }

    // 标记消息为已读
    @PutMapping("/{id}/read")
    public Result markAsRead(@PathVariable Integer id,
                             @RequestHeader("Authorization") String token) {
        Integer userId = extractUserIdFromToken(token);
        return messageService.markAsRead(id, userId);
    }

    // 获取未读消息数量
    @GetMapping("/unread-count")
    public Result getUnreadCount(@RequestHeader("Authorization") String token) {
        Integer userId = extractUserIdFromToken(token);
        return messageService.getUnreadCount(userId);
    }

    // 从 Token 中提取用户 ID 的辅助方法
    private Integer extractUserIdFromToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null && !token.trim().isEmpty()) {
            return jwtUtil.getUserIdFromToken(token);
        }

        return null; // 表示未登录用户
    }

    // 发送消息
    @PostMapping
    public Result sendMessage(@RequestBody Map<String, Object> requestBody,
                              @RequestHeader("Authorization") String token) {
        Integer userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        Integer toUserId = (Integer) requestBody.get("toUserId");
        String content = (String) requestBody.get("content");
        String type = (String) requestBody.get("type");
        
        return messageService.sendMessage(userId, toUserId, content, type);
    }

    // 批量标记已读
    @PutMapping("/read-batch")
    public Result markAsReadBatch(@RequestBody Map<String, Object> requestBody,
                                   @RequestHeader("Authorization") String token) {
        Integer userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        @SuppressWarnings("unchecked")
        java.util.List<Integer> ids = (java.util.List<Integer>) requestBody.get("ids");
        return messageService.markAsReadBatch(ids, userId);
    }

    // 删除消息
    @DeleteMapping("/{id}")
    public Result deleteMessage(@PathVariable Integer id,
                                @RequestHeader("Authorization") String token) {
        Integer userId = extractUserIdFromToken(token);
        if (userId == null) {
            return Result.error(401, "用户未登录");
        }
        
        return messageService.deleteMessage(id, userId);
    }
}
