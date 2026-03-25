-- 重构标签体系
-- 1. 身份标签（原一级标签，逻辑不变）
-- 2. 话题标签（原二级标签，支持系统 + 用户自定义）
-- 3. 商品标签（原三级标签，支持系统 + 用户自定义）
-- 4. 地点标签（原四级标签，支持系统 + 用户自定义）

-- 创建话题标签表
CREATE TABLE IF NOT EXISTS `topic_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签 ID',
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称/代码',
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom' COMMENT '标签类型：system/custom',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DOUBLE DEFAULT 0.0 COMMENT '趋势分数',
  `last_used_at` DATETIME COMMENT '最后使用时间',
  `created_by` BIGINT COMMENT '创建者用户 ID（用户自定义标签）',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：active/banned/pending',
  `icon` VARCHAR(255) COMMENT '图标 URL',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_usage` (`usage_count` DESC),
  INDEX `idx_trend` (`trend_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题标签表';

-- 创建商品标签表
CREATE TABLE IF NOT EXISTS `product_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签 ID',
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称/代码',
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom' COMMENT '标签类型：system/custom',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DOUBLE DEFAULT 0.0 COMMENT '趋势分数',
  `last_used_at` DATETIME COMMENT '最后使用时间',
  `created_by` BIGINT COMMENT '创建者用户 ID（用户自定义标签）',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：active/banned/pending',
  `icon` VARCHAR(255) COMMENT '图标 URL',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_usage` (`usage_count` DESC),
  INDEX `idx_trend` (`trend_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品标签表';

-- 创建地点标签表
CREATE TABLE IF NOT EXISTS `location_tag` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签 ID',
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称/代码',
  `type` VARCHAR(20) NOT NULL DEFAULT 'custom' COMMENT '标签类型：system/custom',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DOUBLE DEFAULT 0.0 COMMENT '趋势分数',
  `last_used_at` DATETIME COMMENT '最后使用时间',
  `created_by` BIGINT COMMENT '创建者用户 ID（用户自定义标签）',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：active/banned/pending',
  `icon` VARCHAR(255) COMMENT '图标 URL',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_name` (`name`),
  INDEX `idx_type` (`type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_usage` (`usage_count` DESC),
  INDEX `idx_trend` (`trend_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点标签表';

-- 修改 topics 表的标签字段
ALTER TABLE `topics` 
CHANGE COLUMN `level2_tag_codes` `topic_tag_codes` TEXT COMMENT '话题标签代码数组（JSON）',
CHANGE COLUMN `level3_tag_codes` `product_tag_codes` TEXT COMMENT '商品标签代码数组（JSON）',
CHANGE COLUMN `level4_tag_codes` `location_tag_codes` TEXT COMMENT '地点标签代码数组（JSON）';

-- 修改 products 表的标签字段
ALTER TABLE `products`
CHANGE COLUMN `level3_tag_codes` `product_tag_codes` TEXT COMMENT '商品标签代码数组（JSON）';

-- 插入默认的系统身份标签（保持 tag_level_1 表不变）
-- student, merchant, organization, admin 已经在 tag_level_1 表中

-- 插入默认的系统话题标签示例
INSERT INTO `topic_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`) VALUES
('study', 'system', 'active', 1, 1, '📚', '#4A90E2', 0, 0.0),
('life', 'system', 'active', 1, 2, '', '#50E3C2', 0, 0.0),
('activity', 'system', 'active', 1, 3, '🎉', '#F5A623', 0, 0.0),
('question', 'system', 'active', 1, 4, '❓', '#D0021B', 0, 0.0),
('share', 'system', 'active', 1, 5, '📢', '#9013FE', 0, 0.0);

-- 插入默认的系统商品标签示例
INSERT INTO `product_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`) VALUES
('electronics', 'system', 'active', 1, 1, '💻', '#4A90E2', 0, 0.0),
('books', 'system', 'active', 1, 2, '📖', '#50E3C2', 0, 0.0),
('sports', 'system', 'active', 1, 3, '⚽', '#F5A623', 0, 0.0),
('daily', 'system', 'active', 1, 4, '🛍️', '#D0021B', 0, 0.0),
('food', 'system', 'active', 1, 5, '🍔', '#9013FE', 0, 0.0);

-- 插入默认的系统地点标签示例
INSERT INTO `location_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`) VALUES
('library', 'system', 'active', 1, 1, '📚', '#4A90E2', 0, 0.0),
('cafeteria', 'system', 'active', 1, 2, '🍽️', '#50E3C2', 0, 0.0),
('dormitory', 'system', 'active', 1, 3, '🏠', '#F5A623', 0, 0.0),
('classroom', 'system', 'active', 1, 4, '🏫', '#D0021B', 0, 0.0),
('playground', 'system', 'active', 1, 5, '🏟️', '#9013FE', 0, 0.0);
