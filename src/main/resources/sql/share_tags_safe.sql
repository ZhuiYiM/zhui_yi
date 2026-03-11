-- 分享功能数据库初始化脚本
-- 用于添加必要的标签数据

-- 1. 添加四级标签"分享"到 tag_level_4 表
-- tag_level_4 表结构：id, name, category, usage_count, trend_score, last_used_at, created_by, status, created_at, updated_at
INSERT INTO tag_level_4 (name, category, usage_count, trend_score, status, created_at, updated_at)
SELECT '分享', 'interaction', 0, 0.0, 'active', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM tag_level_4 WHERE name = '分享'
);

-- 2. 确保 tag_level_2 表有所需字段
-- 添加 code 字段（如果不存在）
SET @dbname = DATABASE();
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
  AND TABLE_NAME = 'tag_level_2' 
  AND COLUMN_NAME = 'code';

SET @sql = IF(@cnt = 0, 
    'ALTER TABLE tag_level_2 ADD COLUMN `code` VARCHAR(50) AFTER `id`', 
    'SELECT "Column code already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 icon 字段（如果不存在）
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
  AND TABLE_NAME = 'tag_level_2' 
  AND COLUMN_NAME = 'icon';

SET @sql = IF(@cnt = 0, 
    'ALTER TABLE tag_level_2 ADD COLUMN `icon` VARCHAR(255) AFTER `parent_id`', 
    'SELECT "Column icon already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 color 字段（如果不存在）
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
  AND TABLE_NAME = 'tag_level_2' 
  AND COLUMN_NAME = 'color';

SET @sql = IF(@cnt = 0, 
    'ALTER TABLE tag_level_2 ADD COLUMN `color` VARCHAR(50) AFTER `icon`', 
    'SELECT "Column color already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 sort_order 字段（如果不存在）
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
  AND TABLE_NAME = 'tag_level_2' 
  AND COLUMN_NAME = 'sort_order';

SET @sql = IF(@cnt = 0, 
    'ALTER TABLE tag_level_2 ADD COLUMN `sort_order` INT AFTER `color`', 
    'SELECT "Column sort_order already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 添加 is_active 字段（如果不存在）
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
  AND TABLE_NAME = 'tag_level_2' 
  AND COLUMN_NAME = 'is_active';

SET @sql = IF(@cnt = 0, 
    'ALTER TABLE tag_level_2 ADD COLUMN `is_active` TINYINT DEFAULT 1 AFTER `sort_order`', 
    'SELECT "Column is_active already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 添加二级标签"转发"
INSERT INTO tag_level_2 (code, name, icon, color, sort_order, is_active, created_at, updated_at)
SELECT 'forward', '转发', '🔁', '#909399', 100, 1, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM tag_level_2 WHERE code = 'forward'
);

-- 完成提示
SELECT '分享功能标签添加完成！' AS message;
