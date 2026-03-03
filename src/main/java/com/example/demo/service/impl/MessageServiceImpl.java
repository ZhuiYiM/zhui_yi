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

        int count = (int)this.count(wrapper);  // 修复：将long转为int
        return Result.success(count);
    }
}
