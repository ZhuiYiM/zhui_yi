-- 商品转发功能数据库迁移脚本
-- 为 topics 表添加 forwarded_from_product_id 字段

-- 1. 添加 forwarded_from_product_id 字段（如果不存在）
SET @dbname = DATABASE();
SELECT COUNT(*) INTO @cnt FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = @dbname 
  AND TABLE_NAME = 'topics' 
  AND COLUMN_NAME = 'forwarded_from_product_id';

SET @sql = IF(@cnt = 0, 
    'ALTER TABLE topics ADD COLUMN `forwarded_from_product_id` BIGINT DEFAULT NULL AFTER `forwarded_from_topic_id`', 
    'SELECT "Column forwarded_from_product_id already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 添加索引以提高查询性能
SELECT COUNT(*) INTO @cnt FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = @dbname 
  AND TABLE_NAME = 'topics' 
  AND INDEX_NAME = 'idx_forwarded_from_product_id';

SET @sql = IF(@cnt = 0, 
    'ALTER TABLE topics ADD INDEX `idx_forwarded_from_product_id` (`forwarded_from_product_id`)', 
    'SELECT "Index idx_forwarded_from_product_id already exists" AS message');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 验证字段是否添加成功
SHOW COLUMNS FROM topics LIKE 'forwarded_from_product_id';

-- 完成提示
SELECT '商品转发功能数据库迁移完成！topics 表已添加 forwarded_from_product_id 字段。' AS message;
