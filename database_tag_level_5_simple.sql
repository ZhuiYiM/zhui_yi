-- ========================================
-- 校园信息平台 - 商业标签数据库升级
-- 创建时间：2026-03-23
-- ========================================

USE campus_db;

-- ========================================
-- 1. 创建 tag_level_5 表
-- ========================================
DROP TABLE IF EXISTS `tag_level_5`;

CREATE TABLE `tag_level_5` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `code` VARCHAR(50) NOT NULL UNIQUE,
  `name` VARCHAR(100) NOT NULL,
  `category` VARCHAR(50) DEFAULT NULL,
  `icon` VARCHAR(100) DEFAULT NULL,
  `color` VARCHAR(20) DEFAULT NULL,
  `usage_count` BIGINT DEFAULT 0,
  `trend_score` DECIMAL(10,2) DEFAULT 0.00,
  `last_used_at` DATETIME DEFAULT NULL,
  `status` VARCHAR(20) DEFAULT 'active',
  `sort_order` INT DEFAULT 0,
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_code` (`code`),
  INDEX `idx_category` (`category`),
  INDEX `idx_status` (`status`),
  INDEX `idx_is_active` (`is_active`),
  INDEX `idx_usage_count` (`usage_count`),
  INDEX `idx_trend_score` (`trend_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 2. 为 products 表添加 tags 字段
-- ========================================
SET @has_tags = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'campus_db' 
    AND TABLE_NAME = 'products' 
    AND COLUMN_NAME = 'tags'
);

SET @sql_add_tags = IF(@has_tags = 0,
    'ALTER TABLE `products` ADD COLUMN `tags` VARCHAR(500) DEFAULT NULL AFTER `category_id`',
    'SELECT "tags column already exists" AS message'
);

PREPARE stmt FROM @sql_add_tags;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 为 tags 字段添加索引（忽略错误如果已存在）
SET @has_idx = (
    SELECT COUNT(*) 
    FROM information_schema.STATISTICS 
    WHERE TABLE_SCHEMA = 'campus_db' 
    AND TABLE_NAME = 'products' 
    AND INDEX_NAME = 'idx_tags'
);

SET @sql_add_idx = IF(@has_idx = 0,
    'ALTER TABLE `products` ADD INDEX `idx_tags` (`tags`(100))',
    'SELECT "index idx_tags already exists" AS message'
);

PREPARE stmt FROM @sql_add_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 3. 初始化商业标签数据（使用 ASCII 字符）
-- ========================================
INSERT INTO `tag_level_5` (`code`, `name`, `category`, `icon`, `color`, `sort_order`, `is_active`, `status`) VALUES
('secondhand', 'Secondhand Items', 'Secondhand', '[icon]', '#4A90E2', 1, 1, 'active'),
('books', 'Books & Textbooks', 'Secondhand', '[icon]', '#50C878', 2, 1, 'active'),
('digital', 'Digital Products', 'Secondhand', '[icon]', '#4169E1', 3, 1, 'active'),
('clothing', 'Clothing & Shoes', 'Secondhand', '[icon]', '#FF6B6B', 4, 1, 'active'),
('daily', 'Daily Necessities', 'Secondhand', '[icon]', '#FFA500', 5, 1, 'active'),
('sports', 'Sports Equipment', 'Secondhand', '[icon]', '#32CD32', 6, 1, 'active'),
('service', 'Services', 'Service', '[icon]', '#7B68EE', 10, 1, 'active'),
('parttime', 'Part-time Jobs', 'Service', '[icon]', '#DDA0DD', 11, 1, 'active'),
('delivery', 'Delivery Service', 'Service', '[icon]', '#FF6347', 12, 1, 'active'),
('tutoring', 'Tutoring', 'Service', '[icon]', '#9370DB', 13, 1, 'active'),
('food', 'Food & Takeout', 'Food', '[icon]', '#FF69B4', 20, 1, 'active'),
('snacks', 'Snacks', 'Food', '[icon]', '#FFB6C1', 21, 1, 'active'),
('fruits', 'Fruits', 'Food', '[icon]', '#FFD700', 22, 1, 'active'),
('electronics', 'Electronics', 'Electronics', '[icon]', '#20B2AA', 30, 1, 'active'),
('virtual', 'Virtual Items', 'Other', '[icon]', '#F0E68C', 31, 1, 'active'),
('other', 'Other', 'Other', '[icon]', '#909399', 99, 1, 'active')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ========================================
-- 4. 验证
-- ========================================
SELECT 'tag_level_5 created successfully!' AS status;
SELECT COUNT(*) as total_tags FROM tag_level_5;
