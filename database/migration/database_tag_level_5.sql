-- ========================================
-- 校园信息平台 - 商业标签数据库升级
-- 创建时间：2026-03-23
-- 说明：创建 tag_level_5 表并初始化商业标签数据
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

-- 为 tags 字段添加索引
ALTER TABLE `products` ADD INDEX `idx_tags` (`tags`(100));

-- ========================================
-- 3. 初始化商业标签数据
-- ========================================
INSERT INTO `tag_level_5` (`code`, `name`, `category`, `icon`, `color`, `sort_order`, `is_active`, `status`, `created_at`, `updated_at`) VALUES
-- 二手物品类
('secondhand', '二手物品', '二手交易', '📦', '#4A90E2', 1, 1, 'active', NOW(), NOW()),
('books', '教材书籍', '二手交易', '📚', '#50C878', 2, 1, 'active', NOW(), NOW()),
('digital', '数码产品', '二手交易', '💻', '#4169E1', 3, 1, 'active', NOW(), NOW()),
('clothing', '服装鞋帽', '二手交易', '👕', '#FF6B6B', 4, 1, 'active', NOW(), NOW()),
('daily', '生活用品', '二手交易', '🏠', '#FFA500', 5, 1, 'active', NOW(), NOW()),
('sports', '体育用品', '二手交易', '⚽', '#32CD32', 6, 1, 'active', NOW(), NOW()),

-- 服务类
('service', '服务需求', '服务', '🛠️', '#7B68EE', 10, 1, 'active', NOW(), NOW()),
('parttime', '兼职信息', '服务', '💼', '#DDA0DD', 11, 1, 'active', NOW(), NOW()),
('delivery', '跑腿代办', '服务', '🏃', '#FF6347', 12, 1, 'active', NOW(), NOW()),
('tutoring', '家教辅导', '服务', '📖', '#9370DB', 13, 1, 'active', NOW(), NOW()),

-- 美食类
('food', '美食外卖', '美食', '🍔', '#FF69B4', 20, 1, 'active', NOW(), NOW()),
('snacks', '零食小吃', '美食', '🍿', '#FFB6C1', 21, 1, 'active', NOW(), NOW()),
('fruits', '水果生鲜', '美食', '🍎', '#FFD700', 22, 1, 'active', NOW(), NOW()),

-- 其他类
('electronics', '电子产品', '电子', '📱', '#20B2AA', 30, 1, 'active', NOW(), NOW()),
('virtual', '虚拟物品', '其他', '🎮', '#F0E68C', 31, 1, 'active', NOW(), NOW()),
('other', '其他', '其他', '📦', '#909399', 99, 1, 'active', NOW(), NOW())
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ========================================
-- 4. 验证数据
-- ========================================
SELECT 'tag_level_5 标签总数:' as info, COUNT(*) as count FROM tag_level_5;

SELECT * FROM tag_level_5 ORDER BY sort_order;

-- ========================================
-- 5. 更新现有商品的 tags 字段（根据描述关键词匹配）
-- ========================================
-- 二手教材类商品
UPDATE products 
SET tags = 'secondhand,books' 
WHERE deleted_at IS NULL 
  AND (title LIKE '%二手%' OR title LIKE '%教材%' OR title LIKE '%书籍%')
  AND category_id IN (6, 8);

-- 数码产品类商品
UPDATE products 
SET tags = 'secondhand,digital,electronics' 
WHERE deleted_at IS NULL 
  AND (title LIKE '%iPhone%' OR title LIKE '%手机%' OR title LIKE '%电脑%' OR title LIKE '%MacBook%')
  AND category_id = 7;

-- 服务类商品
UPDATE products 
SET tags = 'service' 
WHERE deleted_at IS NULL 
  AND category_id IN (2, 3, 9, 10);

-- 美食类商品
UPDATE products 
SET tags = 'food' 
WHERE deleted_at IS NULL 
  AND category_id = 5;

-- 验证更新结果
SELECT '商品标签统计:' as info;
SELECT tags, COUNT(*) as count 
FROM products 
WHERE deleted_at IS NULL AND tags IS NOT NULL
GROUP BY tags;
