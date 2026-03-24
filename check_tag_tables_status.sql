-- ========================================
-- 校园信息平台 - 标签数据库表状态检查
-- 执行时间：2026-03-24
-- 数据库：campus_db
-- ========================================

-- ========================================
-- 第一部分：检查所有标签相关表的记录数
-- ========================================

SELECT '===== 标签表数据统计 =====' AS message;

SELECT 'topic_tag (话题标签)' AS table_name, COUNT(*) AS record_count FROM topic_tag
UNION ALL
SELECT 'product_tag (商品标签)', COUNT(*) FROM product_tag
UNION ALL
SELECT 'location_tag (地点标签)', COUNT(*) FROM location_tag
UNION ALL
SELECT 'identity_tags (身份标签)', COUNT(*) FROM identity_tags
UNION ALL
SELECT 'tags (旧标签表)', COUNT(*) FROM tags;

-- ========================================
-- 第二部分：检查缺失的 tag_level_X 表
-- ========================================

-- 尝试查询 tag_level_1，如果不存在会报错
SELECT '===== 检查 tag_level_1 表 =====' AS message;
SELECT * FROM tag_level_1 LIMIT 5;

SELECT '===== 检查 tag_level_2 表 =====' AS message;
SELECT * FROM tag_level_2 LIMIT 5;

SELECT '===== 检查 tag_level_3 表 =====' AS message;
SELECT * FROM tag_level_3 LIMIT 5;

SELECT '===== 检查 tag_level_4 表 =====' AS message;
SELECT * FROM tag_level_4 LIMIT 5;

SELECT '===== 检查 tag_level_5 表 =====' AS message;
SELECT * FROM tag_level_5 LIMIT 5;

-- ========================================
-- 第三部分：查看现有标签数据详情
-- ========================================

SELECT '===== topic_tag 表数据（前 5 条）=====' AS message;
SELECT id, name, type, status, sort_order, icon, color, is_active 
FROM topic_tag 
ORDER BY sort_order 
LIMIT 5;

SELECT '===== product_tag 表数据（全部）=====' AS message;
SELECT id, name, type, status, sort_order, icon, color, is_active 
FROM product_tag 
ORDER BY sort_order;

SELECT '===== location_tag 表数据（全部）=====' AS message;
SELECT id, name, type, status, sort_order, icon, color, is_active 
FROM location_tag 
ORDER BY sort_order;

SELECT '===== identity_tags 表数据（全部）=====' AS message;
SELECT id, code, name, icon, description, sort_order, is_active 
FROM identity_tags 
ORDER BY sort_order;

SELECT '===== tags 表数据（前 5 条）=====' AS message;
SELECT id, name, usage_count, created_at 
FROM tags 
ORDER BY id 
LIMIT 5;

-- ========================================
-- 第四部分：分析标签使用情况
-- ========================================

SELECT '===== topic_tag 使用统计 =====' AS message;
SELECT 
    type,
    status,
    COUNT(*) as count,
    SUM(usage_count) as total_usage,
    AVG(trend_score) as avg_trend_score
FROM topic_tag
GROUP BY type, status;

SELECT '===== product_tag 使用统计 =====' AS message;
SELECT 
    type,
    status,
    COUNT(*) as count,
    SUM(usage_count) as total_usage,
    AVG(trend_score) as avg_trend_score
FROM product_tag
GROUP BY type, status;

SELECT '===== location_tag 使用统计 =====' AS message;
SELECT 
    type,
    status,
    COUNT(*) as count,
    SUM(usage_count) as total_usage,
    AVG(trend_score) as avg_trend_score
FROM location_tag
GROUP BY type, status;

-- ========================================
-- 第五部分：创建缺失的 tag_level_X 表
-- ========================================

-- 如果上述检查发现 tag_level_X 表不存在，执行以下 SQL 创建

-- 创建 tag_level_1 表（身份标签）
DROP TABLE IF EXISTS `tag_level_1`;
CREATE TABLE `tag_level_1` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '身份代码',
  `name` VARCHAR(100) NOT NULL COMMENT '身份名称',
  `icon` VARCHAR(100) COMMENT '图标 emoji',
  `description` VARCHAR(255) COMMENT '描述',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='一级标签 - 身份标签';

-- 从 identity_tags 复制数据到 tag_level_1
INSERT INTO `tag_level_1` (`code`, `name`, `icon`, `description`, `sort_order`, `is_active`, `created_at`, `updated_at`)
SELECT `code`, `name`, `icon`, `description`, `sort_order`, `is_active`, `created_at`, NOW()
FROM `identity_tags`;

-- 创建 tag_level_2 表（话题分类标签）
DROP TABLE IF EXISTS `tag_level_2`;
CREATE TABLE `tag_level_2` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '分类代码',
  `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` INT DEFAULT NULL COMMENT '父级 ID',
  `icon` VARCHAR(100) COMMENT '图标',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二级标签 - 话题分类';

-- 从 topic_tag 复制系统标签到 tag_level_2
INSERT INTO `tag_level_2` (`code`, `name`, `icon`, `color`, `sort_order`, `is_active`, `created_at`, `updated_at`)
SELECT `name`, `name`, `icon`, `color`, `sort_order`, `is_active`, `created_at`, NOW()
FROM `topic_tag`
WHERE `type` = 'system' AND `status` = 'active';

-- 创建 tag_level_3 表（地点标签）
DROP TABLE IF EXISTS `tag_level_3`;
CREATE TABLE `tag_level_3` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '地点代码',
  `name` VARCHAR(100) NOT NULL COMMENT '地点名称',
  `location_type` VARCHAR(50) COMMENT '地点类型',
  `latitude` DECIMAL(10, 8) COMMENT '纬度',
  `longitude` DECIMAL(11, 8) COMMENT '经度',
  `address` VARCHAR(255) COMMENT '详细地址',
  `icon` VARCHAR(100) COMMENT '图标',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='三级标签 - 地点标签';

-- 从 location_tag 复制数据到 tag_level_3
INSERT INTO `tag_level_3` (`code`, `name`, `sort_order`, `is_active`, `created_at`, `updated_at`)
SELECT `name`, `name`, `sort_order`, `is_active`, `created_at`, NOW()
FROM `location_tag`
WHERE `type` = 'system' AND `status` = 'active';

-- 创建 tag_level_4 表（用户自定义话题标签）
DROP TABLE IF EXISTS `tag_level_4`;
CREATE TABLE `tag_level_4` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称',
  `category` VARCHAR(50) COMMENT '所属分类',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DECIMAL(10,2) DEFAULT 0.00 COMMENT '趋势分数',
  `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用时间',
  `created_by` BIGINT COMMENT '创建者用户 ID',
  `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_name` (`name`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='四级标签 - 用户自定义';

-- 创建 tag_level_5 表（商业标签）
DROP TABLE IF EXISTS `tag_level_5`;
CREATE TABLE `tag_level_5` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签代码',
  `name` VARCHAR(100) NOT NULL COMMENT '标签名称',
  `category` VARCHAR(50) COMMENT '商品分类',
  `icon` VARCHAR(100) COMMENT '图标',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `usage_count` BIGINT DEFAULT 0 COMMENT '使用次数',
  `trend_score` DECIMAL(10,2) DEFAULT 0.00 COMMENT '趋势分数',
  `last_used_at` DATETIME DEFAULT NULL COMMENT '最后使用时间',
  `status` VARCHAR(20) DEFAULT 'active' COMMENT '状态',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_code` (`code`),
  INDEX `idx_category` (`category`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='五级标签 - 商业标签';

-- 从 product_tag 复制数据到 tag_level_5
INSERT INTO `tag_level_5` (`code`, `name`, `icon`, `color`, `sort_order`, `is_active`, `created_at`, `updated_at`)
SELECT `name`, `name`, `icon`, `color`, `sort_order`, `is_active`, `created_at`, NOW()
FROM `product_tag`
WHERE `type` = 'system' AND `status` = 'active';

-- ========================================
-- 第六部分：验证创建结果
-- ========================================

SELECT '===== 验证 tag_level_1 数据 =====' AS message;
SELECT COUNT(*) AS total FROM tag_level_1;
SELECT * FROM tag_level_1 ORDER BY sort_order;

SELECT '===== 验证 tag_level_2 数据 =====' AS message;
SELECT COUNT(*) AS total FROM tag_level_2;
SELECT * FROM tag_level_2 ORDER BY sort_order;

SELECT '===== 验证 tag_level_3 数据 =====' AS message;
SELECT COUNT(*) AS total FROM tag_level_3;
SELECT * FROM tag_level_3 ORDER BY sort_order;

SELECT '===== 验证 tag_level_4 数据 =====' AS message;
SELECT COUNT(*) AS total FROM tag_level_4;
SELECT * FROM tag_level_4 ORDER BY id DESC LIMIT 10;

SELECT '===== 验证 tag_level_5 数据 =====' AS message;
SELECT COUNT(*) AS total FROM tag_level_5;
SELECT * FROM tag_level_5 ORDER BY sort_order;

SELECT '===== 所有标签表创建完成！=====' AS final_message;
