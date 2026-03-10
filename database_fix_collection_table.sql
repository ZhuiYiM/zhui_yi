-- 修复 topic_collections 表缺少 updated_at 字段的问题
ALTER TABLE topic_collections 
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间' AFTER created_at;

-- 为已存在的记录设置初始值
UPDATE topic_collections SET updated_at = created_at WHERE updated_at IS NULL;
