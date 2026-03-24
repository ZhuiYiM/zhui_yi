-- ========================================
-- 校园信息平台 - 标签数据快速修复
-- 创建时间：2026-03-24
-- 数据库：campus_db
-- 密码：123456
-- ========================================

USE campus_db;

-- 1. 从 tag_level_1 插入身份标签到 topic_tag
INSERT IGNORE INTO `topic_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `usage_count`, `trend_score`)
SELECT code, 'system', 'active', is_active, sort_order, icon, 0, 0.0
FROM tag_level_1;

-- 2. 从 tag_level_2 插入话题分类标签到 topic_tag
INSERT IGNORE INTO `topic_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`)
SELECT code, 'system', 'active', is_active, sort_order, icon, color, 0, 0.0
FROM tag_level_2 
WHERE code IS NOT NULL AND code != '';

-- 3. 从 tag_level_3 插入地点标签到 location_tag
INSERT IGNORE INTO `location_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `usage_count`, `trend_score`)
SELECT code, 'system', 'active', is_active, sort_order, icon, 0, 0.0
FROM tag_level_3
WHERE code IS NOT NULL AND code != '';

-- 4. 从 tag_level_5 插入商品标签到 product_tag
INSERT IGNORE INTO `product_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`, `usage_count`, `trend_score`)
SELECT code, 'system', 'active', is_active, sort_order, icon, color, 0, 0.0
FROM tag_level_5;

-- 验证结果
SELECT '===== 数据修复完成 =====' AS message;

SELECT 'topic_tag (身份 + 话题分类):' AS info;
SELECT COUNT(*) AS total FROM topic_tag;
SELECT name, type, icon, sort_order FROM topic_tag ORDER BY sort_order;

SELECT 'location_tag (地点):' AS info;
SELECT COUNT(*) AS total FROM location_tag;
SELECT name, icon, sort_order FROM location_tag ORDER BY sort_order;

SELECT 'product_tag (商品):' AS info;
SELECT COUNT(*) AS total FROM product_tag;
SELECT name, type, icon, sort_order FROM product_tag ORDER BY sort_order;
