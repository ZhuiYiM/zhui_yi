-- ========================================
-- 校园信息平台 - 标签管理数据库升级
-- 创建时间：2026-03-23
-- 说明：为 tag_level_2 表增加管理员所需字段和数据
-- ========================================

USE campus_db;

-- ========================================
-- 1. 为 tag_level_2 表添加管理字段
-- ========================================
-- 检查并添加 icon 字段
SET @has_icon = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'campus_db' 
    AND TABLE_NAME = 'tag_level_2' 
    AND COLUMN_NAME = 'icon'
);

SET @sql_add_icon = IF(@has_icon = 0,
    'ALTER TABLE `tag_level_2` ADD COLUMN `icon` VARCHAR(100) DEFAULT NULL COMMENT ''图标 URL 或类名'' AFTER `parent_id`',
    'SELECT ''icon 字段已存在'' AS message'
);

PREPARE stmt FROM @sql_add_icon;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 color 字段
SET @has_color = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'campus_db' 
    AND TABLE_NAME = 'tag_level_2' 
    AND COLUMN_NAME = 'color'
);

SET @sql_add_color = IF(@has_color = 0,
    'ALTER TABLE `tag_level_2` ADD COLUMN `color` VARCHAR(20) DEFAULT NULL COMMENT ''标签颜色'' AFTER `icon`',
    'SELECT ''color 字段已存在'' AS message'
);

PREPARE stmt FROM @sql_add_color;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 sort_order 字段
SET @has_sort_order = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'campus_db' 
    AND TABLE_NAME = 'tag_level_2' 
    AND COLUMN_NAME = 'sort_order'
);

SET @sql_add_sort_order = IF(@has_sort_order = 0,
    'ALTER TABLE `tag_level_2` ADD COLUMN `sort_order` INT DEFAULT 0 COMMENT ''排序顺序'' AFTER `color`',
    'SELECT ''sort_order 字段已存在'' AS message'
);

PREPARE stmt FROM @sql_add_sort_order;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 检查并添加 is_active 字段
SET @has_is_active = (
    SELECT COUNT(*) 
    FROM information_schema.COLUMNS 
    WHERE TABLE_SCHEMA = 'campus_db' 
    AND TABLE_NAME = 'tag_level_2' 
    AND COLUMN_NAME = 'is_active'
);

SET @sql_add_is_active = IF(@has_is_active = 0,
    'ALTER TABLE `tag_level_2` ADD COLUMN `is_active` TINYINT DEFAULT 1 COMMENT ''是否启用：0-否，1-是'' AFTER `sort_order`',
    'SELECT ''is_active 字段已存在'' AS message'
);

PREPARE stmt FROM @sql_add_is_active;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ========================================
-- 2. 更新索引
-- ========================================
ALTER TABLE `tag_level_2` ADD INDEX IF NOT EXISTS `idx_code` (`code`);
ALTER TABLE `tag_level_2` ADD INDEX IF NOT EXISTS `idx_parent_id` (`parent_id`);
ALTER TABLE `tag_level_2` ADD INDEX IF NOT EXISTS `idx_is_active` (`is_active`);

-- ========================================
-- 3. 插入测试标签数据（补充）
-- ========================================
-- 本部校区 - 二级标签（已存在则跳过）
INSERT INTO `tag_level_2` (`code`, `name`, `parent_id`, `icon`, `color`, `sort_order`, `is_active`, `created_at`, `updated_at`) VALUES
('building', '教学楼', NULL, 'el-icon-office-building', '#409EFF', 1, 1, NOW(), NOW()),
('lab', '实验楼', NULL, 'el-icon-reading', '#67C23A', 2, 1, NOW(), NOW()),
('library', '图书馆', NULL, 'el-icon-reading', '#E6A23C', 3, 1, NOW(), NOW()),
('sports', '体育场馆', NULL, 'el-icon-soccer', '#F56C6C', 4, 1, NOW(), NOW()),
('canteen', '食堂', NULL, 'el-icon-food', '#909399', 5, 1, NOW(), NOW()),
('dormitory', '宿舍', NULL, 'el-icon-house', '#409EFF', 6, 1, NOW(), NOW()),
('admin', '行政楼', NULL, 'el-icon-office-building', '#67C23A', 7, 1, NOW(), NOW()),
('other', '其他建筑', NULL, 'el-icon-location', '#909399', 8, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ========================================
-- 4. 验证数据
-- ========================================
SELECT 'tag_level_2 标签总数:' as info, COUNT(*) as count FROM tag_level_2;

SELECT * FROM tag_level_2 ORDER BY sort_order;
