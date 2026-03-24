-- ========================================
-- 管理员标签管理系统完善脚本
-- ========================================
-- 功能说明：
-- 1. 身份标签（identity_tags）- 一级标签
-- 2. 话题标签（topic_tag）- 二级标签 - 支持系统/自定义
-- 3. 商品标签（product_tag）- 三/五级标签 - 支持系统/自定义
-- 4. 地点标签（location_tag）- 四级标签 - 支持系统/自定义
-- 5. 所有自定义标签都需要管理员审核
-- ========================================

USE campus_db;

-- ========================================
-- 1. 确保表结构完整
-- ========================================

-- 话题标签表（已有，检查是否需要补充索引）
ALTER TABLE topic_tag ADD INDEX idx_topic_status(status);
ALTER TABLE topic_tag ADD INDEX idx_topic_created_by(created_by);

-- 商品标签表（已有，检查是否需要补充索引）
ALTER TABLE product_tag ADD INDEX idx_product_status(status);
ALTER TABLE product_tag ADD INDEX idx_product_category(category);
ALTER TABLE product_tag ADD INDEX idx_product_created_by(created_by);

-- 地点标签表（已有，检查是否需要补充索引）
ALTER TABLE location_tag ADD INDEX idx_location_status(status);
ALTER TABLE location_tag ADD INDEX idx_location_created_by(created_by);

-- ========================================
-- 2. 初始化系统标签数据
-- ========================================

-- 2.1 插入默认的系统话题标签（如果不存在）
INSERT INTO `topic_tag` (`name`, `code`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`) VALUES
('学习', 'study', 'system', 'active', 1, 1, '', '#4A90E2'),
('生活', 'life', 'system', 'active', 1, 2, '', '#50E3C2'),
('工作', 'work', 'system', 'active', 1, 3, '', '#F5A623'),
('服务', 'service', 'system', 'active', 1, 4, '', '#D0021B'),
('交易', 'trade', 'system', 'active', 1, 5, '', '#9013FE'),
('美食', 'food', 'system', 'active', 1, 6, '', '#BD10E0'),
('购物', 'shopping', 'system', 'active', 1, 7, '', '#9013FE'),
('活动', 'activity', 'system', 'active', 1, 8, '', '#F5A623'),
('招聘', 'recruit', 'system', 'active', 1, 9, '', '#4A90E2'),
('二手', 'second_hand', 'system', 'active', 1, 10, '', '#50E3C2')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    type = VALUES(type),
    status = VALUES(status),
    is_active = VALUES(is_active);

-- 2.2 插入默认的系统商品标签（如果不存在）
INSERT INTO `product_tag` (`name`, `code`, `type`, `category`, `status`, `is_active`, `sort_order`, `icon`, `color`) VALUES
('二手物品', 'secondhand', 'system', 'all', 'active', 1, 1, '', '#4A90E2'),
('图书教材', 'books', 'system', 'all', 'active', 1, 2, '', '#50E3C2'),
('数码产品', 'digital', 'system', 'all', 'active', 1, 3, '', '#F5A623'),
('服装饰品', 'clothing', 'system', 'all', 'active', 1, 4, '', '#D0021B'),
('生活用品', 'daily', 'system', 'all', 'active', 1, 5, '', '#9013FE'),
('运动户外', 'sports', 'system', 'all', 'active', 1, 6, '', '#BD10E0'),
('服务技能', 'service', 'system', 'all', 'active', 1, 7, '', '#4A90E2')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    type = VALUES(type),
    status = VALUES(status),
    is_active = VALUES(is_active);

-- 2.3 插入默认的系统地点标签（如果不存在）
INSERT INTO `location_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`) VALUES
('图书馆', 'system', 'active', 1, 1, '', '#4A90E2'),
('教学楼', 'system', 'active', 1, 2, '', '#50E3C2'),
('宿舍楼', 'system', 'active', 1, 3, '', '#F5A623'),
('食堂', 'system', 'active', 1, 4, '', '#D0021B'),
('操场', 'system', 'active', 1, 5, '', '#9013FE'),
('计算机房', 'system', 'active', 1, 6, '', '#BD10E0'),
('体育馆', 'system', 'active', 1, 7, '', '#4A90E2'),
('礼堂', 'system', 'active', 1, 8, '', '#50E3C2')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    type = VALUES(type),
    status = VALUES(status),
    is_active = VALUES(is_active);

-- ========================================
-- 3. 清理测试数据
-- ========================================

-- 删除测试的自定义商品标签（如果有）
DELETE FROM product_tag WHERE name LIKE '%测试%' AND type = 'custom';

-- ========================================
-- 4. 查看统计信息
-- ========================================

-- 统计各类标签数量
SELECT 
    '话题标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN type = 'system' THEN 1 ELSE 0 END) AS system_count,
    SUM(CASE WHEN type = 'custom' THEN 1 ELSE 0 END) AS custom_count,
    SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending_count
FROM topic_tag
UNION ALL
SELECT 
    '商品标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN type = 'system' THEN 1 ELSE 0 END) AS system_count,
    SUM(CASE WHEN type = 'custom' THEN 1 ELSE 0 END) AS custom_count,
    SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending_count
FROM product_tag
UNION ALL
SELECT 
    '地点标签' AS tag_type,
    COUNT(*) AS total_count,
    SUM(CASE WHEN type = 'system' THEN 1 ELSE 0 END) AS system_count,
    SUM(CASE WHEN type = 'custom' THEN 1 ELSE 0 END) AS custom_count,
    SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) AS pending_count
FROM location_tag;

-- ========================================
-- 5. 查询待审核的自定义标签
-- ========================================

-- 查看所有待审核的话题标签
SELECT id, name, code, created_by, status, created_at 
FROM topic_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC;

-- 查看所有待审核的商品标签
SELECT id, name, code, category, created_by, status, created_at 
FROM product_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC;

-- 查看所有待审核的地点标签
SELECT id, name, type, created_by, status, created_at 
FROM location_tag 
WHERE type = 'custom' AND status = 'pending'
ORDER BY created_at DESC;
