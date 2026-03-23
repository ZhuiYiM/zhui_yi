-- 清理并重新插入广告数据
DELETE FROM advertisements WHERE position = 'mall';

-- 插入正确的广告数据（使用真实存在的 ID）
INSERT INTO advertisements 
(`title`, `image_url`, `link_url`, `content`, `position`, `sort_order`, `is_active`, `ad_type`, `related_id`, `filter_tags`, `start_time`, `end_time`, `created_by`) 
VALUES
('毕业季特卖', 
 'https://placehold.co/800x300/FF6B6B/FFFFFF?text=毕业季特卖', 
 '/mall?tag=secondhand', 
 '毕业季特价商品，低至 3 折起', 
 'mall', 1, 1, 'activity', NULL, 
 '{"tags": ["secondhand", "books", "digital"]}', 
 NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),

('快递代取服务', 
 'https://placehold.co/800x300/4A90E2/FFFFFF?text=快递代取', 
 '/user/1001', 
 '快速便捷的快递代取服务', 
 'mall', 2, 1, 'merchant', 1001, NULL, 
 NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1),

('MacBook Pro 特价', 
 'https://placehold.co/800x300/50C878/FFFFFF?text=MacBook', 
 '/product/187', 
 '2020 款，i5 处理器，16G 内存', 
 'mall', 3, 1, 'product', 187, NULL, 
 NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY), 1),

('高等数学教材', 
 'https://placehold.co/800x300/FFA500/FFFFFF?text=教材', 
 '/product/193', 
 '同济第七版，9 成新', 
 'mall', 4, 1, 'product', 193, NULL, 
 NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), 1),

('校园美食节', 
 'https://placehold.co/800x300/DC143C/FFFFFF?text=美食节', 
 '/mall?tag=food', 
 '汇聚各地美食', 
 'mall', 5, 1, 'activity', NULL, 
 '{"tags": ["food"]}', 
 NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

-- 验证数据
SELECT id, title, ad_type, related_id, link_url, is_active 
FROM advertisements 
WHERE position = 'mall' 
ORDER BY sort_order;
