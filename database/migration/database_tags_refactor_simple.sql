-- 重构标签体系 - 简化版
-- 创建话题标签表
CREATE TABLE IF NOT EXISTS `topic_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom',
  `usage_count` BIGINT DEFAULT 0,
  `trend_score` DOUBLE DEFAULT 0.0,
  `last_used_at` DATETIME,
  `created_by` BIGINT,
  `status` VARCHAR(20) DEFAULT 'pending',
  `icon` VARCHAR(255),
  `color` VARCHAR(20),
  `sort_order` INT DEFAULT 0,
  `is_active` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建商品标签表
CREATE TABLE IF NOT EXISTS `product_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom',
  `usage_count` BIGINT DEFAULT 0,
  `trend_score` DOUBLE DEFAULT 0.0,
  `last_used_at` DATETIME,
  `created_by` BIGINT,
  `status` VARCHAR(20) DEFAULT 'pending',
  `icon` VARCHAR(255),
  `color` VARCHAR(20),
  `sort_order` INT DEFAULT 0,
  `is_active` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建地点标签表
CREATE TABLE IF NOT EXISTS `location_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom',
  `usage_count` BIGINT DEFAULT 0,
  `trend_score` DOUBLE DEFAULT 0.0,
  `last_used_at` DATETIME,
  `created_by` BIGINT,
  `status` VARCHAR(20) DEFAULT 'pending',
  `icon` VARCHAR(255),
  `color` VARCHAR(20),
  `sort_order` INT DEFAULT 0,
  `is_active` TINYINT DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 修改 topics 表的标签字段
ALTER TABLE `topics` 
CHANGE COLUMN `level2_tag_codes` `topic_tag_codes` TEXT,
CHANGE COLUMN `level3_tag_codes` `product_tag_codes` TEXT,
CHANGE COLUMN `level4_tag_codes` `location_tag_codes` TEXT;

-- 修改 products 表的标签字段
ALTER TABLE `products`
CHANGE COLUMN `level3_tag_codes` `product_tag_codes` TEXT;

-- 插入默认的系统话题标签
INSERT INTO `topic_tag` (`name`, `type`, `status`, `is_active`, `sort_order`) VALUES
('study', 'system', 'active', 1, 1),
('life', 'system', 'active', 1, 2),
('activity', 'system', 'active', 1, 3),
('question', 'system', 'active', 1, 4),
('share', 'system', 'active', 1, 5);

-- 插入默认的系统商品标签
INSERT INTO `product_tag` (`name`, `type`, `status`, `is_active`, `sort_order`) VALUES
('electronics', 'system', 'active', 1, 1),
('books', 'system', 'active', 1, 2),
('sports', 'system', 'active', 1, 3),
('daily', 'system', 'active', 1, 4),
('food', 'system', 'active', 1, 5);

-- 插入默认的系统地点标签
INSERT INTO `location_tag` (`name`, `type`, `status`, `is_active`, `sort_order`) VALUES
('library', 'system', 'active', 1, 1),
('cafeteria', 'system', 'active', 1, 2),
('dormitory', 'system', 'active', 1, 3),
('classroom', 'system', 'active', 1, 4),
('playground', 'system', 'active', 1, 5);
