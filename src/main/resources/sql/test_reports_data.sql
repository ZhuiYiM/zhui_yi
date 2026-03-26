-- 举报功能测试数据
-- 执行时间：2026-03-27
-- 用途：添加地点、商品、话题的举报测试数据

-- 测试用户 wangwu (ID=4) 举报地点、商品、话题
INSERT INTO `reports` (`reporter_id`, `target_id`, `target_type`, `report_type`, `reason`, `evidence`, `status`, `created_at`) VALUES
-- 举报地点
(4, 1, 'location', 'spam', 'Fake location info', NULL, 0, NOW()),
(4, 2, 'location', 'fake_info', 'Wrong description', NULL, 0, NOW()),

-- 举报商品
(4, 1, 'product', 'fraud', 'Price fraud', NULL, 0, NOW()),
(4, 2, 'product', 'spam', 'Spam advertisement', NULL, 0, NOW()),

-- 举报话题
(4, 3, 'topic', 'harassment', 'Personal attack', NULL, 0, NOW()),
(4, 4, 'topic', 'political', 'Political content', NULL, 0, NOW());

-- 查询验证
SELECT 
    r.id,
    r.reporter_id,
    u.username AS reporter_name,
    r.target_id,
    r.target_type,
    r.report_type,
    r.reason,
    r.status,
    r.created_at
FROM reports r
LEFT JOIN users u ON r.reporter_id = u.id
ORDER BY r.created_at DESC;
