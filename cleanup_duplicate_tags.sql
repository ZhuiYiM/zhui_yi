-- =====================================================
-- 清理 topic_tag 和 location_tag 表中的重复数据
-- 保留 ID 最小的标签
-- =====================================================

-- ==================== 话题标签 ====================
SELECT '===== 清理前：重复的话题标签 =====' as status;
SELECT name, COUNT(*) as cnt 
FROM topic_tag 
WHERE is_active = 1 
GROUP BY name 
HAVING cnt > 1 
ORDER BY cnt DESC;

-- 删除重复的话题标签
DELETE t1 FROM topic_tag t1
INNER JOIN (
    SELECT name, MIN(id) as min_id
    FROM topic_tag
    WHERE is_active = 1
    GROUP BY name
) t2 ON t1.name = t2.name AND t1.id > t2.min_id
WHERE t1.is_active = 1;

SELECT '===== 清理后：重复的话题标签 (应该为空) =====' as status;
SELECT name, COUNT(*) as cnt 
FROM topic_tag 
WHERE is_active = 1 
GROUP BY name 
HAVING cnt > 1;

-- 统计话题标签总数
SELECT '===== 话题标签统计 =====' as status;
SELECT COUNT(*) as total_active_topic_tags
FROM topic_tag
WHERE is_active = 1;

-- ==================== 地点标签 ====================
SELECT '===== 清理前：重复的地点标签 =====' as status;
SELECT name, COUNT(*) as cnt 
FROM location_tag 
WHERE is_active = 1 
GROUP BY name 
HAVING cnt > 1 
ORDER BY cnt DESC;

-- 删除重复的地点标签
DELETE t1 FROM location_tag t1
INNER JOIN (
    SELECT name, MIN(id) as min_id
    FROM location_tag
    WHERE is_active = 1
    GROUP BY name
) t2 ON t1.name = t2.name AND t1.id > t2.min_id
WHERE t1.is_active = 1;

SELECT '===== 清理后：重复的地点标签 (应该为空) =====' as status;
SELECT name, COUNT(*) as cnt 
FROM location_tag 
WHERE is_active = 1 
GROUP BY name 
HAVING cnt > 1;

-- 统计地点标签总数
SELECT '===== 地点标签统计 =====' as status;
SELECT COUNT(*) as total_active_location_tags
FROM location_tag
WHERE is_active = 1;

SELECT '===== 清理完成 =====' as status;
