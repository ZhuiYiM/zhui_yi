-- 修复 topic_tag 表中 code 为 NULL 的问题
-- 使用 name 字段的值来填充 code 字段

-- 更新所有 code 为 NULL 的记录
UPDATE topic_tag 
SET code = LOWER(REPLACE(name, ' ', '_'))
WHERE code IS NULL OR code = '';

-- 验证修复结果
SELECT id, code, name FROM topic_tag ORDER BY id;
