// service/impl/MessageServiceImpl.java
package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.entity.Message;
import com.example.demo.mapper.MessageMapper;
import com.example.demo.service.MessageService;
import com.example.demo.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public Result getMessagesByTypeAndUser(String type, Integer page, Integer size, Integer userId) {
        Page<Message> messagePage = new Page<>(page, size);
        QueryWrapper<Message> wrapper = new QueryWrapper<>();

        wrapper.eq("receiver_id", userId);

        if (type != null && !type.isEmpty()) {
            wrapper.eq("message_type", type);
        }

        wrapper.orderByDesc("created_at");

        Page<Message> result = this.page(messagePage, wrapper);
        return Result.success(result);
    }

    @Override
    public Result markAsRead(Integer messageId, Integer userId) {
        Message message = this.getById(messageId);
        if (message == null) {
            return Result.error("消息不存在");
        }

        // 验证消息是否属于当前用户
        if (!message.getReceiverId().equals(userId)) {  // 修复：使用驼峰命名
            return Result.error("无权操作此消息");
        }

        message.setIsRead(1);        // 修复：使用驼峰命名
        message.setUpdatedAt(LocalDateTime.now());  // 修复：使用驼峰命名

        boolean result = this.updateById(message);
        if (result) {
            return Result.success("消息标记为已读成功");
        } else {
            return Result.error("消息标记为已读失败");
        }
    }

    @Override
    public Result getUnreadCount(Integer userId) {
        QueryWrapper<Message> wrapper = new QueryWrapper<>();
        wrapper.eq("receiver_id", userId)
                .eq("is_read", 0);

        int count = (int)this.count(wrapper);  // 修复：将 long 转为 int
        return Result.success(count);
    }

    @Override
    public Result sendMessage(Integer fromUserId, Integer toUserId, String content, String type) {
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }

        Message message = new Message();
        message.setSenderId(fromUserId);
        message.setReceiverId(toUserId);
        message.setContent(content);
        message.setMessageType(type != null ? type : "private");
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());

        boolean result = this.save(message);
        if (result) {
            return Result.success("消息发送成功");
        } else {
            return Result.error("消息发送失败");
        }
    }

    @Override
    public Result markAsReadBatch(java.util.List<Integer> messageIds, Integer userId) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Result.error("消息 ID 列表不能为空");
        }

        for (Integer messageId : messageIds) {
            Message message = this.getById(messageId);
            if (message != null && message.getReceiverId().equals(userId)) {
                message.setIsRead(1);
                message.setUpdatedAt(LocalDateTime.now());
                this.updateById(message);
            }
        }

        return Result.success("批量标记成功", java.util.Map.of("count", messageIds.size()));
    }

    @Override
    public Result deleteMessage(Integer messageId, Integer userId) {
        Message message = this.getById(messageId);
        if (message == null) {
            return Result.error("消息不存在");
        }

        // 验证消息是否属于当前用户
        if (!message.getReceiverId().equals(userId) && !message.getSenderId().equals(userId)) {
            return Result.error("无权删除此消息");
        }

        boolean result = this.removeById(messageId);
        if (result) {
            return Result.success("消息删除成功");
        } else {
            return Result.error("消息删除失败");
        }
    }
}
