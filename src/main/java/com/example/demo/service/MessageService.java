package com.example.demo.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.entity.Message;
import com.example.demo.common.Result;

public interface MessageService extends IService<Message> {
    Result getMessagesByTypeAndUser(String type, Integer page, Integer size, Integer userId);
    Result markAsRead(Integer messageId, Integer userId);
    Result getUnreadCount(Integer userId);
    Result sendMessage(Integer fromUserId, Integer toUserId, String content, String type);
    Result markAsReadBatch(java.util.List<Integer> messageIds, Integer userId);
    Result deleteMessage(Integer messageId, Integer userId);
    Result deleteMessagesBatch(java.util.List<Integer> messageIds, Integer userId);
}
