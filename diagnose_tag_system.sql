-- ========================================
-- 校园信息平台 - 标签系统问题诊断脚本
-- 创建时间：2026-03-24
-- 数据库：campus_db
-- 密码：123456
-- ========================================

USE campus_db;

-- ========================================
-- 第一部分：检查 topics 表结构
-- ========================================

SELECT '===== 1. topics 表标签字段结构 =====' AS section;

DESC topics;

-- 查看示例数据
SELECT '===== 2. topics 表示例数据（最新 5 条）=====' AS section;

SELECT 
    id,
    user_id,
    content,
    level1_tag_code AS '身份标签',
    topic_tag_codes AS '话题标签 (JSON)',
    product_tag_codes AS '商品标签 (JSON)',
    location_tag_codes AS '地点标签 (JSON)',
    created_at
FROM topics
ORDER BY created_at DESC
LIMIT 5;

-- ========================================
-- 第二部分：检查标签表完整性
-- ========================================

SELECT '===== 3. topic_tag 表（话题标签）结构 =====' AS section;

DESC topic_tag;

SELECT '===== 4. topic_tag 表现有数据 =====' AS section;

SELECT 
    id,
    name,
    code,
    type,
    status,
    is_active,
    icon,
    color,
    sort_order,
    usage_count
FROM topic_tag
ORDER BY sort_order;

SELECT '===== 5. location_tag 表（地点标签）结构 =====' AS section;

DESC location_tag;

SELECT '===== 6. location_tag 表现有数据 =====' AS section;

SELECT 
    id,
    name,
    type,
    status,
    is_active,
    icon,
    color,
    sort_order
FROM location_tag
ORDER BY sort_order;

SELECT '===== 7. product_tag 表（商品标签）结构 =====' AS section;

DESC product_tag;

SELECT '===== 8. product_tag 表现有数据 =====' AS section;

SELECT 
    id,
    name,
    type,
    status,
    is_active,
    icon,
    color,
    sort_order
FROM product_tag
ORDER BY sort_order;

-- ========================================
-- 第三部分：检查旧的标签表是否还在使用
-- ========================================

SELECT '===== 9. 检查旧标签表是否还存在 =====' AS section;

SELECT 
    TABLE_NAME,
    TABLE_TYPE,
    TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'campus_db'
  AND TABLE_NAME IN ('tag_level_1', 'tag_level_2', 'tag_level_3', 'tag_level_4', 'tag_level_5')
ORDER BY TABLE_NAME;

-- 如果存在，查看是否有数据
SELECT '===== 10. 旧表 tag_level_2 数据（如果存在）=====' AS section;

SELECT COUNT(*) AS 'tag_level_2 记录数'
FROM tag_level_2
WHERE EXISTS (SELECT 1 FROM information_schema.TABLES 
              WHERE TABLE_SCHEMA = 'campus_db' 
              AND TABLE_NAME = 'tag_level_2');

-- ========================================
-- 第四部分：诊断问题
-- ========================================

SELECT '===== 11. 问题诊断：检查话题数据的标签格式 =====' AS section;

-- 查看话题的标签字段是否为正确的 JSON 格式
SELECT 
    id,
    topic_tag_codes,
    CASE 
        WHEN topic_tag_codes IS NULL THEN 'NULL'
        WHEN topic_tag_codes = '' THEN 'EMPTY'
        WHEN LEFT(topic_tag_codes, 1) = '[' AND RIGHT(topic_tag_codes, 1) = ']' THEN 'VALID_JSON_ARRAY'
        ELSE 'INVALID_FORMAT'
    END AS format_status
FROM topics
ORDER BY created_at DESC
LIMIT 10;

-- ========================================
-- 第五部分：修复建议
-- ========================================

SELECT '===== 12. 修复建议 =====' AS section;

SELECT '问题 1: 前端发布话题时，level4 自定义标签未合并到 topicTagCodes' AS problem1;
SELECT '解决方案：已在 PublishTopicModal.vue 中修复，将 level4 标签名称合并到 topicTagCodes 数组' AS solution1;

SELECT '问题 2: 后端查询话题时，buildTopicTagsInfo 方法使用 name 字段查询' AS problem2;
SELECT '解决方案：确认前端传递的是标签名称（name）而不是代码（code），或者修改后端支持 code 查询' AS solution2;

SELECT '问题 3: 旧的标签表可能还有数据残留' AS problem3;
SELECT '解决方案：运行此脚本迁移旧表数据到新表，然后删除或归档旧表' AS solution3;

-- ========================================
-- 第六部分：验证测试
-- ========================================

SELECT '===== 13. 验证测试：插入测试数据 =====' AS section;

-- 测试插入一条包含所有类型标签的话题
INSERT INTO topics (
    user_id,
    content,
    images,
    level1_tag_code,
    topic_tag_codes,
    location_tag_codes,
    created_at,
    updated_at
) VALUES (
    1,
    '测试话题：验证标签系统是否正常工作',
    '[]',
    'student',
    '["study", "life", "自定义标签 1"]',  -- 混合系统标签和自定义标签
    '["library"]',
    NOW(),
    NOW()
);

SELECT '✅ 测试话题已插入' AS message;

-- 验证插入的数据
SELECT 
    id,
    content,
    level1_tag_code,
    topic_tag_codes,
    location_tag_codes
FROM topics
WHERE content LIKE '%测试话题%'
ORDER BY created_at DESC
LIMIT 1;

-- 清理测试数据
DELETE FROM topics WHERE content LIKE '%测试话题%';

SELECT '🗑️ 测试数据已清理' AS message;

SELECT '===== 诊断完成 =====' AS final_message;
