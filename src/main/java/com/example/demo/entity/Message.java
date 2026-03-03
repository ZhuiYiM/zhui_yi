// entity/Message.java
package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("messages") // 指定数据库表名为 messages
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer senderId;        // 驼峰命名
    private Integer receiverId;      // 驼峰命名
    private String title;
    private String content;
    private String messageType;      // system, private_message, order_notification
    private Integer isRead;          // 驼峰命名
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // 添加更新时间字段
}
