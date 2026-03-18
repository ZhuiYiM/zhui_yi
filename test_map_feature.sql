-- ========================================
-- 地图导引功能 - 快速测试脚本
-- 用于验证数据库表结构和初始数据
-- ========================================

USE campus_db;

-- 1. 验证校区表结构
DESC campuses;

-- 2. 验证地点表结构
DESC locations;

-- 3. 查询所有校区
SELECT * FROM campuses ORDER BY sort_order;

-- 4. 查询各校区地点数量
SELECT 
    c.name AS 校区名称，
    COUNT(l.id) AS 地点数量
FROM campuses c
LEFT JOIN locations l ON c.id = l.campus_id
GROUP BY c.id, c.name
ORDER BY c.sort_order;

-- 5. 查询本部校区热门地点
SELECT 
    l.name AS 地点名称，
    l.category AS 分类，
    l.description AS 描述
FROM locations l
WHERE l.campus_id = 1 AND l.is_popular = 1
ORDER BY l.sort_order;

-- 6. 查询平原湖校区所有地点
SELECT 
    l.name AS 地点名称，
    l.category AS 分类，
    l.icon AS 图标
FROM locations l
WHERE l.campus_id = 2
ORDER BY l.sort_order;

-- 7. 查询创新港校区所有地点
SELECT 
    l.name AS 地点名称，
    l.category AS 分类，
    l.icon AS 图标
FROM locations l
WHERE l.campus_id = 3
ORDER BY l.sort_order;

-- 8. 按分类统计地点数量
SELECT 
    c.name AS 校区名称，
    l.category AS 分类，
    COUNT(*) AS 数量
FROM locations l
JOIN campuses c ON l.campus_id = c.id
GROUP BY c.name, l.category
ORDER BY c.sort_order, l.category;

-- 9. 验证地图配置 JSON 格式
SELECT 
    name AS 校区名称，
    JSON_EXTRACT(map_config, '$.baidu') AS 百度配置，
    JSON_EXTRACT(map_config, '$.gaode') AS 高德配置，
    JSON_EXTRACT(map_config, '$.tencent') AS 腾讯配置
FROM campuses;

-- 10. 测试搜索功能（模拟）
SELECT 
    name AS 地点名称，
    category AS 分类，
    campus_id AS 校区 ID
FROM locations
WHERE name LIKE '%图书馆%' OR description LIKE '%图书%';

-- 11. 查询校区的中心坐标
SELECT 
    name AS 校区名称，
    center_latitude AS 纬度，
    center_longitude AS 经度，
    zoom_level AS 缩放级别
FROM campuses;

-- 12. 验证外键约束
-- 尝试删除一个校区（应该级联删除相关地点）
-- 注意：这只是验证，不实际执行
-- DELETE FROM campuses WHERE id = 3;
