-- ========================================
-- 检查标签查询问题
-- 数据库：campus_db
-- 密码：123456
-- ========================================

USE campus_db;

-- 1. 查看 topic_tag 表的结构
SELECT '===== 1. topic_tag 表结构 =====' AS message;
DESC topic_tag;

-- 2. 查看 topic_tag 表的所有数据
SELECT '===== 2. topic_tag 表所有数据 =====' AS message;
SELECT id, name, code, type, status, is_active, icon, color, sort_order 
FROM topic_tag 
ORDER BY sort_order;

-- 3. 查看 location_tag 表的所有数据
SELECT '===== 3. location_tag 表所有数据 =====' AS message;
SELECT id, name, type, status, is_active, icon, color, sort_order 
FROM location_tag 
ORDER BY sort_order;

-- 4. 查看最新话题的标签数据
SELECT '===== 4. 最新话题的标签数据 =====' AS message;
SELECT 
    id,
    content,
    level1_tag_code,
    topic_tag_codes,
    location_tag_codes,
    created_at
FROM topics
ORDER BY created_at DESC
LIMIT 5;

-- 5. 测试查询：能否找到 "studys" 标签
SELECT '===== 5. 测试查询 studys 标签 =====' AS message;
SELECT * FROM topic_tag WHERE name = 'studys';
SELECT * FROM topic_tag WHERE code = 'studys';

-- 6. 测试查询：能否找到 "library" 标签
SELECT '===== 6. 测试查询 library 标签 =====' AS message;
SELECT * FROM location_tag WHERE name = 'library';
SELECT * FROM location_tag WHERE code = 'library';

-- 7. 查看话题标签的 JSON 格式
SELECT '===== 7. 查看 JSON 格式 =====' AS message;
SELECT 
    id,
    JSON_EXTRACT(topic_tag_codes, '$[0]') AS first_topic_tag,
    JSON_EXTRACT(location_tag_codes, '$[0]') AS first_location_tag
FROM topics
WHERE topic_tag_codes IS NOT NULL AND location_tag_codes IS NOT NULL
ORDER BY created_at DESC
LIMIT 5;
