-- 操作日志表
DROP TABLE IF EXISTS `admin_operation_log`;

CREATE TABLE `admin_operation_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '操作日志 ID',
  `admin_id` BIGINT COMMENT '管理员 ID',
  `admin_name` VARCHAR(50) COMMENT '管理员姓名',
  `operation` VARCHAR(50) COMMENT '操作类型',
  `module` VARCHAR(50) COMMENT '操作模块',
  `target_id` BIGINT COMMENT '操作目标 ID',
  `detail` TEXT COMMENT '操作详情',
  `ip_address` VARCHAR(50) COMMENT 'IP 地址',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  INDEX idx_module (`module`),
  INDEX idx_admin (`admin_id`),
  INDEX idx_created (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 插入测试数据
INSERT INTO `admin_operation_log` (`admin_id`, `admin_name`, `operation`, `module`, `target_id`, `detail`, `ip_address`) VALUES
(1, 'admin', 'delete', 'topic', 10, 'Delete topic', '127.0.0.1'),
(1, 'admin', 'update', 'user', 5, 'Update user status', '127.0.0.1'),
(1, 'admin', 'delete', 'product', 8, 'Remove product', '127.0.0.1'),
(1, 'admin', 'audit', 'verification', 3, 'Approve verification', '127.0.0.1'),
(1, 'admin', 'delete', 'comment', 15, 'Delete comment', '127.0.0.1');
