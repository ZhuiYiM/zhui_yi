-- ========================================
-- 管理员标签管理系统测试脚本
-- ========================================

USE campus_db;

-- ========================================
-- 1. 查看当前标签数据统计
-- ========================================

SELECT '===== 标签体系统计 =====' AS '';

SELECT 
    '身份标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count
FROM identity_tags
UNION ALL
SELECT 
    '话题标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count
FROM topic_tag
UNION ALL
SELECT 
    '商品标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS active_count
FROM product_tag
UNION ALL
SELECT 
    '地点标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count
FROM location_tag;

-- ========================================
-- 2. 查看待审核的自定义标签
-- ========================================

SELECT '===== 待审核的话题标签 =====' AS '';
SELECT id, name, code, created_by, status, created_at 
FROM topic_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC
LIMIT 10;

SELECT '===== 待审核的商品标签 =====' AS '';
SELECT id, name, code, category, created_by, status, description, created_at 
FROM product_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC
LIMIT 10;

SELECT '===== 待审核的地点标签 =====' AS '';
SELECT id, name, type, created_by, status, created_at 
FROM location_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC
LIMIT 10;

-- ========================================
-- 3. 测试管理员操作
-- ========================================

-- 3.1 测试创建系统话题标签
SELECT '===== 测试创建系统话题标签 =====' AS '';
INSERT INTO topic_tag (name, code, type, status, is_active, sort_order, icon, color)
VALUES ('测试标签', 'test_tag', 'system', 'active', 1, 99, '', '#FF0000');

-- 3.2 测试创建用户自定义话题标签（需要审核）
SELECT '===== 测试创建用户自定义话题标签 =====' AS '';
INSERT INTO topic_tag (name, code, type, status, is_active, sort_order, created_by)
VALUES ('用户自定义标签', 'user_custom', 'custom', 'pending', 0, 100, 1);

-- 3.3 测试创建系统商品标签
SELECT '===== 测试创建系统商品标签 =====' AS '';
INSERT INTO product_tag (name, code, type, category, status, is_active, sort_order, icon, color)
VALUES ('测试商品', 'test_product', 'system', 'all', 'active', 1, 99, '', '#00FF00');

-- 3.4 测试创建用户自定义商品标签（需要审核）
SELECT '===== 测试创建用户自定义商品标签 =====' AS '';
INSERT INTO product_tag (name, code, type, category, status, is_active, sort_order, created_by, description)
VALUES ('用户商品', 'user_product', 'custom', 'all', 'pending', 0, 100, 1, '这是用户提交的商品标签');

-- 3.5 测试创建系统地点标签
SELECT '===== 测试创建系统地点标签 =====' AS '';
INSERT INTO location_tag (name, type, status, is_active, sort_order, icon, color)
VALUES ('测试地点', 'system', 'active', 1, 99, '', '#0000FF');

-- 3.6 测试创建用户自定义地点标签（需要审核）
SELECT '===== 测试创建用户自定义地点标签 =====' AS '';
INSERT INTO location_tag (name, type, status, is_active, sort_order, created_by)
VALUES ('用户地点', 'custom', 'pending', 0, 100, 1);

-- ========================================
-- 4. 测试管理员审核操作
-- ========================================

-- 4.1 审核通过话题标签
SELECT '===== 测试审核通过话题标签 =====' AS '';
UPDATE topic_tag 
SET status = 'active', is_active = 1 
WHERE code = 'user_custom';

-- 4.2 审核通过商品标签
SELECT '===== 测试审核通过商品标签 =====' AS '';
UPDATE product_tag 
SET status = 'active', is_active = 1 
WHERE code = 'user_product';

-- 4.3 审核通过地点标签
SELECT '===== 测试审核通过地点标签 =====' AS '';
UPDATE location_tag 
SET status = 'active', is_active = 1 
WHERE name = '用户地点';

-- ========================================
-- 5. 测试管理员禁用操作
-- ========================================

-- 5.1 禁用话题标签
SELECT '===== 测试禁用话题标签 =====' AS '';
UPDATE topic_tag 
SET is_active = 0 
WHERE code = 'test_tag';

-- 5.2 禁用商品标签
SELECT '===== 测试禁用商品标签 =====' AS '';
UPDATE product_tag 
SET status = 'banned', is_active = 0 
WHERE code = 'test_product';

-- 5.3 禁用地点标签
SELECT '===== 测试禁用地点标签 =====' AS '';
UPDATE location_tag 
SET is_active = 0 
WHERE name = '测试地点';

-- ========================================
-- 6. 查看热门话题标签（按使用次数）
-- ========================================

SELECT '===== 热门话题标签 TOP 10 =====' AS '';
SELECT id, name, code, usage_count, trend_score, status, is_active
FROM topic_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 10;

-- ========================================
-- 7. 查看热门商品标签（按使用次数）
-- ========================================

SELECT '===== 热门商品标签 TOP 10 =====' AS '';
SELECT id, name, code, category, usage_count, trend_score, status, is_active
FROM product_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 10;

-- ========================================
-- 8. 查看热门地点标签（按使用次数）
-- ========================================

SELECT '===== 热门地点标签 TOP 10 =====' AS '';
SELECT id, name, type, usage_count, trend_score, status, is_active
FROM location_tag
WHERE is_active = 1
ORDER BY usage_count DESC, trend_score DESC
LIMIT 10;

-- ========================================
-- 9. 清理测试数据
-- ========================================

SELECT '===== 清理测试数据 =====' AS '';
DELETE FROM topic_tag WHERE code IN ('test_tag', 'user_custom');
DELETE FROM product_tag WHERE code IN ('test_product', 'user_product');
DELETE FROM location_tag WHERE name IN ('测试地点', '用户地点');

-- ========================================
-- 10. 最终统计
-- ========================================

SELECT '===== 最终标签统计 =====' AS '';

SELECT 
    '身份标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) AS inactive_count
FROM identity_tags
UNION ALL
SELECT 
    '话题标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) AS inactive_count
FROM topic_tag
UNION ALL
SELECT 
    '商品标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN status = 'active' THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending_count,
    SUM(CASE WHEN status = 'banned' THEN 1 ELSE 0 END) AS banned_count
FROM product_tag
UNION ALL
SELECT 
    '地点标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END) AS active_count,
    SUM(CASE WHEN is_active = 0 THEN 1 ELSE 0 END) AS inactive_count
FROM location_tag;
