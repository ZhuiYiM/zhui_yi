-- 系统设置表
DROP TABLE IF EXISTS `system_settings`;

CREATE TABLE `system_settings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置 ID',
  `setting_key` VARCHAR(100) UNIQUE NOT NULL COMMENT '设置键',
  `setting_value` TEXT COMMENT '设置值',
  `description` VARCHAR(255) COMMENT '描述',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_key (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 插入默认设置
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `description`) VALUES
('site_name', 'Campus Platform', 'Site name'),
('site_description', 'Campus information exchange platform', 'Site description'),
('allow_registration', 'true', 'Allow registration'),
('max_upload_size', '5242880', 'Max upload size in bytes'),
('allowed_image_types', 'jpg,jpeg,png,gif', 'Allowed image types'),
('topic_audit_required', 'true', 'Require topic audit'),
('product_audit_required', 'false', 'Require product audit'),
('maintenance_mode', 'false', 'Maintenance mode');
