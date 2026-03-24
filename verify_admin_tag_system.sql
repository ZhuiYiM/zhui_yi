-- ========================================
-- 管理员标签管理系统快速验证脚本
-- ========================================

USE campus_db;

-- ========================================
-- 1. 验证表结构
-- ========================================

SELECT '===== 1. 验证表结构 =====' AS '';

SELECT 'identity_tags' AS table_name, COUNT(*) AS record_count FROM identity_tags
UNION ALL
SELECT 'topic_tag', COUNT(*) FROM topic_tag
UNION ALL
SELECT 'product_tag', COUNT(*) FROM product_tag
UNION ALL
SELECT 'location_tag', COUNT(*) FROM location_tag;

-- ========================================
-- 2. 验证索引
-- ========================================

SELECT '===== 2. 验证索引 =====' AS '';

SHOW INDEX FROM topic_tag WHERE Key_name LIKE 'idx_%';
SHOW INDEX FROM product_tag WHERE Key_name LIKE 'idx_%';
SHOW INDEX FROM location_tag WHERE Key_name LIKE 'idx_%';

-- ========================================
-- 3. 查看系统标签
-- ========================================

SELECT '===== 3.1 身份标签示例 =====' AS '';
SELECT id, code, name, icon, is_active 
FROM identity_tags 
LIMIT 5;

SELECT '===== 3.2 话题标签示例 =====' AS '';
SELECT id, name, code, type, status, is_active, usage_count 
FROM topic_tag 
WHERE type = 'system' AND is_active = 1 
LIMIT 5;

SELECT '===== 3.3 商品标签示例 =====' AS '';
SELECT id, name, code, type, status, category, is_active 
FROM product_tag 
WHERE type = 'system' AND is_active = 1 
LIMIT 5;

SELECT '===== 3.4 地点标签示例 =====' AS '';
SELECT id, name, type, status, is_active, usage_count 
FROM location_tag 
WHERE type = 'system' AND is_active = 1 
LIMIT 5;

-- ========================================
-- 4. 查看待审核标签
-- ========================================

SELECT '===== 4.1 待审核的话题标签 =====' AS '';
SELECT id, name, code, created_by, status, created_at 
FROM topic_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC
LIMIT 10;

SELECT '===== 4.2 待审核的商品标签 =====' AS '';
SELECT id, name, code, category, created_by, status, description, created_at 
FROM product_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC
LIMIT 10;

SELECT '===== 4.3 待审核的地点标签 =====' AS '';
SELECT id, name, type, created_by, status, created_at 
FROM location_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC
LIMIT 10;

-- ========================================
-- 5. 查看热门标签
-- ========================================

SELECT '===== 5.1 热门话题标签 TOP 5 =====' AS '';
SELECT id, name, code, usage_count, trend_score, status, is_active
FROM topic_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 5;

SELECT '===== 5.2 热门商品标签 TOP 5 =====' AS '';
SELECT id, name, code, category, usage_count, trend_score, status, is_active
FROM product_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 5;

SELECT '===== 5.3 热门地点标签 TOP 5 =====' AS '';
SELECT id, name, type, usage_count, trend_score, status, is_active
FROM location_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 5;

-- ========================================
-- 6. 统计信息
-- ========================================

SELECT '===== 6. 标签体系统计 =====' AS '';

SELECT 
    '身份标签' AS tag_type,
    COUNT(*) AS total,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) AS inactive,
    '无需审核' AS need_audit
FROM identity_tags
UNION ALL
SELECT 
    '话题标签',
    COUNT(*),
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END),
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END),
    CONCAT(SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END), ' 待审核')
FROM topic_tag
UNION ALL
SELECT 
    '商品标签',
    COUNT(*),
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END),
    SUM(CASE WHEN status = 'banned' THEN 1 ELSE 0 END),
    CONCAT(SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END), ' 待审核')
FROM product_tag
UNION ALL
SELECT 
    '地点标签',
    COUNT(*),
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END),
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END),
    CONCAT(SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END), ' 待审核')
FROM location_tag;

-- ========================================
-- 7. 验证 API 可用性（测试查询）
-- ========================================

SELECT '===== 7. API 测试查询 =====' AS '';

-- 测试分页查询（模拟 API 调用）
SELECT '测试：分页查询话题标签' AS test_name;
SELECT id, name, code, type, status, is_active 
FROM topic_tag 
ORDER BY sort_order, id 
LIMIT 10 OFFSET 0;

SELECT '测试：关键词搜索' AS test_name;
SELECT id, name, code, type, status 
FROM topic_tag 
WHERE name LIKE '%学习%' OR code LIKE '%study%'
ORDER BY id;

SELECT '测试：按类型筛选' AS test_name;
SELECT id, name, type, status, is_active 
FROM product_tag 
WHERE type = 'custom'
ORDER BY id;

-- ========================================
-- 8. 数据完整性检查
-- ========================================

SELECT '===== 8. 数据完整性检查 =====' AS '';

-- 检查是否有孤立的自定义标签（created_by 对应的用户不存在）
SELECT '检查孤立的自定义话题标签' AS check_name;
SELECT COUNT(*) AS orphan_count
FROM topic_tag t
LEFT JOIN users u ON t.created_by = u.id
WHERE t.type = 'custom' AND u.id IS NULL;

SELECT '检查孤立的自定义商品标签' AS check_name;
SELECT COUNT(*) AS orphan_count
FROM product_tag t
LEFT JOIN users u ON t.created_by = u.id
WHERE t.type = 'custom' AND u.id IS NULL;

SELECT '检查孤立的自定义地点标签' AS check_name;
SELECT COUNT(*) AS orphan_count
FROM location_tag t
LEFT JOIN users u ON t.created_by = u.id
WHERE t.type = 'custom' AND u.id IS NULL;

-- 检查重复的标签代码
SELECT '检查重复的话题标签代码' AS check_name;
SELECT code, COUNT(*) as count
FROM topic_tag
GROUP BY code
HAVING COUNT(*) > 1;

SELECT '检查重复的商品标签代码' AS check_name;
SELECT code, COUNT(*) as count
FROM product_tag
GROUP BY code
HAVING COUNT(*) > 1;

-- ========================================
-- 9. 性能检查
-- ========================================

SELECT '===== 9. 性能检查 =====' AS '';

-- 检查是否有未使用索引的查询
EXPLAIN SELECT id, name, code, type, status, is_active 
FROM topic_tag 
WHERE status = 'pending' AND type = 'custom'
ORDER BY created_at DESC
LIMIT 10;

EXPLAIN SELECT id, name, code, category, usage_count 
FROM product_tag 
WHERE is_active = 1 
ORDER BY usage_count DESC
LIMIT 10;

-- ========================================
-- 10. 最终状态
-- ========================================

SELECT '===== 验证完成 =====' AS '';
SELECT '所有验证已完成，请检查上述输出' AS message;
SELECT CONCAT('数据库：', DATABASE()) AS database_info;
SELECT NOW() AS check_time;
