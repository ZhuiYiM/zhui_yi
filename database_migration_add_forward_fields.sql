-- 为 topics 表添加转发相关字段
-- 注意：如果字段已存在，请先删除再执行，或使用以下检查语句

-- 方案 1：检查字段是否存在，不存在才添加
ALTER TABLE topics 
ADD COLUMN IF NOT EXISTS is_forwarded TINYINT DEFAULT 0 COMMENT '是否为转发话题 0-否，1-是',
ADD COLUMN IF NOT EXISTS forwarded_from_topic_id BIGINT NULL COMMENT '被转发的话题 ID';

-- 方案 2：如果 MySQL 版本不支持 IF NOT EXISTS，使用以下语句（需要先手动检查）
-- 检查字段是否存在的查询：
-- SHOW COLUMNS FROM topics LIKE 'is_forwarded';
-- SHOW COLUMNS FROM topics LIKE 'forwarded_from_topic_id';

-- 如果字段已存在但需要删除重建，使用：
-- ALTER TABLE topics DROP COLUMN IF EXISTS is_forwarded;
-- ALTER TABLE topics DROP COLUMN IF EXISTS forwarded_from_topic_id;

-- 然后再执行 ADD COLUMN 语句

-- 添加索引以提高查询性能
CREATE INDEX IF NOT EXISTS idx_forwarded_from_topic_id ON topics(forwarded_from_topic_id);
