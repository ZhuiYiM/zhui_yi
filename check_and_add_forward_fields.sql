-- 检查并添加转发相关字段到 topics 表
USE campus_db;

-- 添加 is_forwarded 字段（如果不存在）
ALTER TABLE topics ADD COLUMN IF NOT EXISTS is_forwarded BOOLEAN DEFAULT FALSE COMMENT "是否为转发话题";

-- 添加 forwarded_from_topic_id 字段（如果不存在）
ALTER TABLE topics ADD COLUMN IF NOT EXISTS forwarded_from_topic_id BIGINT DEFAULT NULL COMMENT "被转发的话题 ID";

-- 添加 forwarded_from_product_id 字段（如果不存在）
ALTER TABLE topics ADD COLUMN IF NOT EXISTS forwarded_from_product_id BIGINT DEFAULT NULL COMMENT "被分享的商品 ID";

-- 查看 topics 表结构
DESCRIBE topics;

-- 查询已有的转发话题（如果有）
SELECT id, content, is_forwarded, forwarded_from_topic_id, forwarded_from_product_id 
FROM topics 
WHERE is_forwarded = TRUE OR forwarded_from_topic_id IS NOT NULL OR forwarded_from_product_id IS NOT NULL
LIMIT 10;
