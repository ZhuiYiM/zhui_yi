-- ========================================
-- 校园信息平台 - 标签表快速修复脚本
-- 执行时间：2026-03-24
-- 数据库：campus_db
-- 说明：创建缺失的 tag_level_X 表并迁移数据
-- ========================================

USE campus_db;

-- ========================================
-- 第一步：创建 tag_level_1 表（身份标签）
-- ========================================

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

-- 从 identity_tags 复制数据
INSERT INTO `tag_level_1` (`code`, `name`, `icon`, `description`, `sort_order`, `is_active`, `created_at`, `updated_at`)
SELECT `code`, `name`, `icon`, `description`, `sort_order`, `is_active`, `created_at`, NOW()
FROM `identity_tags`;

-- 验证
SELECT '✅ tag_level_1 创建完成' AS message;
SELECT COUNT(*) AS total_records FROM tag_level_1;
SELECT * FROM tag_level_1 ORDER BY sort_order;

-- ========================================
-- 第二步：创建 tag_level_2 表（话题分类标签）
-- ========================================

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

-- 从 topic_tag 复制系统标签（先修复 studys 错误）
UPDATE topic_tag SET name = 'study' WHERE name = 'studys';

INSERT INTO `tag_level_2` (`code`, `name`, `icon`, `color`, `sort_order`, `is_active`, `created_at`, `updated_at`)
SELECT `name`, `name`, `icon`, `color`, `sort_order`, `is_active`, `created_at`, NOW()
FROM `topic_tag`
WHERE `type` = 'system' AND `status` = 'active';

-- 验证
SELECT '✅ tag_level_2 创建完成' AS message;
SELECT COUNT(*) AS total_records FROM tag_level_2;
SELECT * FROM tag_level_2 ORDER BY sort_order;

-- ========================================
-- 第三步：创建 tag_level_3 表（地点标签）
-- ========================================

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

-- 从 location_tag 复制数据，并补充图标和颜色
INSERT INTO `tag_level_3` (
  `code`, `name`, `location_type`, `latitude`, `longitude`, 
  `address`, `icon`, `color`, `sort_order`, `is_active`, 
  `created_at`, `updated_at`
)
SELECT 
  `name`, 
  `name`, 
  'building' AS location_type,
  NULL AS latitude,
  NULL AS longitude,
  NULL AS address,
  CASE 
    WHEN `name` = 'library' THEN '📚'
    WHEN `name` = 'teaching_building' THEN '🏫'
    WHEN `name` = 'dormitory' THEN '🏠'
    WHEN `name` = 'cafeteria' THEN '🍽️'
    WHEN `name` = 'playground' THEN '🏟️'
    WHEN `name` = 'computer_room' THEN '💻'
    ELSE '📍'
  END AS icon,
  '#4A90E2' AS color,
  `sort_order`,
  `is_active`,
  `created_at`,
  NOW()
FROM `location_tag`
WHERE `type` = 'system' AND `status` = 'active';

-- 同时更新 location_tag 表的图标和颜色
UPDATE location_tag SET 
  icon = CASE 
    WHEN name = 'library' THEN '📚'
    WHEN name = 'teaching_building' THEN '🏫'
    WHEN name = 'dormitory' THEN '🏠'
    WHEN name = 'cafeteria' THEN '🍽️'
    WHEN name = 'playground' THEN '🏟️'
    WHEN name = 'computer_room' THEN '💻'
    ELSE '📍'
  END,
  color = '#4A90E2'
WHERE type = 'system';

-- 验证
SELECT '✅ tag_level_3 创建完成' AS message;
SELECT COUNT(*) AS total_records FROM tag_level_3;
SELECT * FROM tag_level_3 ORDER BY sort_order;

-- ========================================
-- 第四步：创建 tag_level_4 表（用户自定义话题标签）
-- ========================================

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

-- 暂时不插入数据，等待用户创建
SELECT '✅ tag_level_4 创建完成（空表）' AS message;

-- ========================================
-- 第五步：创建 tag_level_5 表（商业标签）
-- ========================================

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

-- 从 product_tag 复制数据
INSERT INTO `tag_level_5` (
  `code`, `name`, `category`, `icon`, `color`, 
  `sort_order`, `is_active`, `created_at`, `updated_at`
)
SELECT 
  `name`, 
  `name`, 
  'general' AS category,
  COALESCE(icon, '') AS icon,
  color,
  `sort_order`,
  `is_active`,
  `created_at`,
  NOW()
FROM `product_tag`
WHERE `type` = 'system' AND `status` = 'active';

-- 验证
SELECT '✅ tag_level_5 创建完成' AS message;
SELECT COUNT(*) AS total_records FROM tag_level_5;
SELECT * FROM tag_level_5 ORDER BY sort_order;

-- ========================================
-- 第六步：总结报告
-- ========================================

SELECT '' AS message;
SELECT '========================================' AS message;
SELECT '标签表创建完成！统计报告' AS message;
SELECT '========================================' AS message;

SELECT 
  'tag_level_1 (身份标签)' AS table_name,
  COUNT(*) AS record_count
FROM tag_level_1
UNION ALL
SELECT 'tag_level_2 (话题分类)', COUNT(*) FROM tag_level_2
UNION ALL
SELECT 'tag_level_3 (地点)', COUNT(*) FROM tag_level_3
UNION ALL
SELECT 'tag_level_4 (自定义)', COUNT(*) FROM tag_level_4
UNION ALL
SELECT 'tag_level_5 (商品)', COUNT(*) FROM tag_level_5
UNION ALL
SELECT 'topic_tag (新 - 话题)', COUNT(*) FROM topic_tag
UNION ALL
SELECT 'product_tag (新 - 商品)', COUNT(*) FROM product_tag
UNION ALL
SELECT 'location_tag (新 - 地点)', COUNT(*) FROM location_tag;

SELECT '' AS message;
SELECT '========================================' AS message;
SELECT '所有标签表创建成功！' AS message;
SELECT '可以开始使用标签管理功能了！' AS message;
SELECT '========================================' AS message;
