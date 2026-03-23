-- 清理 mall 位置的广告
DELETE FROM advertisements WHERE position = 'mall';

-- 插入广告数据
INSERT INTO advertisements (title, image_url, link_url, content, position, sort_order, is_active, ad_type, related_id, filter_tags, start_time, end_time, created_by) VALUES
('Graduation Sale', 'https://placehold.co/800x300/FF6B6B/FFFFFF?text=Graduation', '/mall?tag=secondhand', 'Graduation sale items', 'mall', 1, 1, 'activity', NULL, '{"tags": ["secondhand", "books", "digital"]}', NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
('Express Delivery', 'https://placehold.co/800x300/4A90E2/FFFFFF?text=Express', '/user/1001', 'Campus delivery service', 'mall', 2, 1, 'merchant', 1001, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 60 DAY), 1),
('MacBook Pro', 'https://placehold.co/800x300/50C878/FFFFFF?text=MacBook', '/product/187', '2020 MacBook Pro', 'mall', 3, 1, 'product', 187, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 45 DAY), 1),
('Math Textbook', 'https://placehold.co/800x300/FFA500/FFFFFF?text=Textbook', '/product/193', 'Math textbook', 'mall', 4, 1, 'product', 193, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), 1),
('Food Festival', 'https://placehold.co/800x300/DC143C/FFFFFF?text=Food', '/mall?tag=food', 'Campus food festival', 'mall', 5, 1, 'activity', NULL, '{"tags": ["food"]}', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

-- 查询结果
SELECT id, title, ad_type, related_id, link_url, is_active FROM advertisements WHERE position = 'mall' ORDER BY sort_order;
