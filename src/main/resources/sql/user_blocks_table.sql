-- 用户拉黑表
CREATE TABLE IF NOT EXISTS `user_blocks` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
  `blocker_id` INT NOT NULL COMMENT '拉黑者用户 ID',
  `blocked_id` INT NOT NULL COMMENT '被拉黑者用户 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_blocker_blocked` (`blocker_id`, `blocked_id`) COMMENT '唯一索引：防止重复拉黑',
  KEY `idx_blocker_id` (`blocker_id`) COMMENT '拉黑者索引',
  KEY `idx_blocked_id` (`blocked_id`) COMMENT '被拉黑者索引',
  CONSTRAINT `fk_blocker_user` FOREIGN KEY (`blocker_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_blocked_user` FOREIGN KEY (`blocked_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户拉黑关系表';
