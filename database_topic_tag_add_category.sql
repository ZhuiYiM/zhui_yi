-- 移除 topic_tag 表的 category 字段
-- 用于支持用户提交自定义标签功能简化（仅保留标签名称和补充说明）

ALTER TABLE topic_tag 
DROP COLUMN category;

-- 查看表结构
DESC topic_tag;
