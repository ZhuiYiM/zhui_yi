-- ========================================
-- 校园信息平台 - 标签数据库完整检查与修复
-- 创建时间：2026-03-24
-- 数据库密码：123456
-- 说明：检查并修复所有标签表结构和数据
-- ========================================

USE campus_db;

-- ========================================
-- 第一部分：检查现有表结构
-- ========================================

SELECT '===== 检查现有标签表 =====' AS message;

-- 检查 tag_level_1 表（身份标签）
SELECT 'tag_level_1 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM tag_level_1;
SELECT * FROM tag_level_1 ORDER BY sort_order;

-- 检查 tag_level_2 表（话题分类标签）
SELECT 'tag_level_2 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM tag_level_2;
SELECT * FROM tag_level_2 ORDER BY sort_order;

-- 检查 tag_level_3 表（地点标签）
SELECT 'tag_level_3 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM tag_level_3;
SELECT * FROM tag_level_3 ORDER BY sort_order;

-- 检查 tag_level_4 表（用户自定义话题标签）
SELECT 'tag_level_4 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM tag_level_4;
SELECT * FROM tag_level_4 ORDER BY id DESC LIMIT 20;

-- 检查 tag_level_5 表（商品标签）
SELECT 'tag_level_5 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM tag_level_5;
SELECT * FROM tag_level_5 ORDER BY sort_order;

-- 检查新的标签表
SELECT 'topic_tag 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM topic_tag;
SELECT * FROM topic_tag ORDER BY id;

SELECT 'product_tag 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM product_tag;
SELECT * FROM product_tag ORDER BY id;

SELECT 'location_tag 表:' AS table_name;
SELECT COUNT(*) AS total_tags FROM location_tag;
SELECT * FROM location_tag ORDER BY id;

-- ========================================
-- 第二部分：根据原标签表数据填充新表
-- ========================================

-- 2.1 从 tag_level_1 复制到 topic_tag（身份标签）
SELECT '===== 从 tag_level_1 填充 topic_tag =====' AS message;

INSERT INTO `topic_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`)
SELECT 
    t1.code AS name,
    'system' AS type,
    'active' AS status,
    t1.is_active,
    t1.sort_order,
    t1.icon,
    NULL AS color,  -- tag_level_1 没有 color 字段
    0 AS usage_count,
    0.0 AS trend_score
FROM tag_level_1 t1
WHERE NOT EXISTS (
    SELECT 1 FROM topic_tag t2 WHERE t2.name = t1.code
)
ORDER BY t1.sort_order;

-- 2.2 从 tag_level_2 复制到 topic_tag（话题分类标签）
SELECT '===== 从 tag_level_2 填充 topic_tag =====' AS message;

INSERT INTO `topic_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`)
SELECT 
    t2.code AS name,
    'system' AS type,
    'active' AS status,
    t2.is_active,
    t2.sort_order,
    t2.icon,
    t2.color,
    0 AS usage_count,
    0.0 AS trend_score
FROM tag_level_2 t2
WHERE t2.code IS NOT NULL AND t2.code != '' AND NOT EXISTS (
    SELECT 1 FROM topic_tag t3 WHERE t3.name = t2.code
)
ORDER BY t2.sort_order;

-- 2.3 从 tag_level_3 复制到 location_tag（地点标签）
SELECT '===== 从 tag_level_3 填充 location_tag =====' AS message;

INSERT INTO `location_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`)
SELECT 
    t3.code AS name,
    'system' AS type,
    'active' AS status,
    t3.is_active,
    t3.sort_order,
    t3.icon,
    NULL AS color,  -- tag_level_3 可能没有 color 字段
    0 AS usage_count,
    0.0 AS trend_score
FROM tag_level_3 t3
WHERE t3.code IS NOT NULL AND t3.code != '' AND NOT EXISTS (
    SELECT 1 FROM location_tag t4 WHERE t4.name = t3.code
)
ORDER BY t3.sort_order;

-- 2.4 从 tag_level_5 复制到 product_tag（商品标签）
SELECT '===== 从 tag_level_5 填充 product_tag =====' AS message;

INSERT INTO `product_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`)
SELECT 
    t5.code AS name,
    'system' AS type,
    'active' AS status,
    t5.is_active,
    t5.sort_order,
    t5.icon,
    t5.color,
    0 AS usage_count,
    0.0 AS trend_score
FROM tag_level_5 t5
WHERE NOT EXISTS (
    SELECT 1 FROM product_tag t6 WHERE t6.name = t5.code
)
ORDER BY t5.sort_order;

-- ========================================
-- 第三部分：验证数据迁移结果
-- ========================================

SELECT '===== 验证数据迁移结果 =====' AS message;

-- 验证 topic_tag（应包含身份标签 + 话题分类标签）
SELECT 'topic_tag 最终数据:' AS info;
SELECT COUNT(*) AS total FROM topic_tag;
SELECT name, type, status, icon, sort_order FROM topic_tag ORDER BY sort_order;

-- 验证 product_tag（应包含商品标签）
SELECT 'product_tag 最终数据:' AS info;
SELECT COUNT(*) AS total FROM product_tag;
SELECT name, type, status, icon, sort_order FROM product_tag ORDER BY sort_order;

-- 验证 location_tag（应包含地点标签）
SELECT 'location_tag 最终数据:' AS info;
SELECT COUNT(*) AS total FROM location_tag;
SELECT name, type, status, icon, sort_order FROM location_tag ORDER BY sort_order;

-- ========================================
-- 第四部分：总结报告
-- ========================================

SELECT '===== 标签体系总结 =====' AS message;

SELECT 
    'tag_level_1 (身份标签)' AS source_table,
    COUNT(*) AS tag_count
FROM tag_level_1
UNION ALL
SELECT 
    'tag_level_2 (话题分类)' AS source_table,
    COUNT(*) AS tag_count
FROM tag_level_2
UNION ALL
SELECT 
    'tag_level_3 (地点标签)' AS source_table,
    COUNT(*) AS tag_count
FROM tag_level_3
UNION ALL
SELECT 
    'tag_level_4 (自定义话题)' AS source_table,
    COUNT(*) AS tag_count
FROM tag_level_4
UNION ALL
SELECT 
    'tag_level_5 (商品标签)' AS source_table,
    COUNT(*) AS tag_count
FROM tag_level_5
UNION ALL
SELECT 
    'topic_tag (新 - 身份 + 话题)' AS source_table,
    COUNT(*) AS tag_count
FROM topic_tag
UNION ALL
SELECT 
    'product_tag (新 - 商品)' AS source_table,
    COUNT(*) AS tag_count
FROM product_tag
UNION ALL
SELECT 
    'location_tag (新 - 地点)' AS source_table,
    COUNT(*) AS tag_count
FROM location_tag;

SELECT '数据库检查与修复完成！' AS final_message;
