-- 消息表
CREATE TABLE IF NOT EXISTS `messages` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
  `sender_id` INT NOT NULL COMMENT '发送者用户 ID',
  `receiver_id` INT NOT NULL COMMENT '接收者用户 ID',
  `title` VARCHAR(200) COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` ENUM('system', 'private_message', 'order_notification') NOT NULL DEFAULT 'private_message' COMMENT '消息类型：system-系统消息，private_message-私信，order_notification-订单通知',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_sender_id` (`sender_id`) COMMENT '发送者索引',
  KEY `idx_receiver_id` (`receiver_id`) COMMENT '接收者索引',
  KEY `idx_message_type` (`message_type`) COMMENT '消息类型索引',
  KEY `idx_is_read` (`is_read`) COMMENT '已读状态索引',
  CONSTRAINT `fk_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户消息表';
