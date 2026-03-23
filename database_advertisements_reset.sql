-- ========================================
-- 校园信息平台 - 广告测试数据重置
-- 创建时间：2026-03-23
-- 说明：清理旧的广告数据并重新插入正确的测试数据
-- ========================================

USE campus_db;

-- ========================================
-- 1. 清理现有广告数据（仅 mall 位置）
-- ========================================
DELETE FROM `advertisements` WHERE `position` = 'mall';

-- ========================================
-- 2. 重新插入测试数据（使用真实存在的 ID）
-- ========================================
INSERT INTO `advertisements` 
(`title`, `image_url`, `link_url`, `content`, `position`, `sort_order`, `is_active`, `ad_type`, `related_id`, `filter_tags`, `start_time`, `end_time`, `created_by`) 
VALUES
-- 1. 活动类型广告：毕业季二手交易标签筛选
('毕业季二手交易特卖', 
 'https://placehold.co/800x300/FF6B6B/FFFFFF?text=毕业季特卖', 
 '/mall?tag=secondhand', 
 '毕业季特价商品，低至 3 折起', 
 'mall', 
 1, 
 1, 
 'activity', 
 NULL, 
 '{"tags": ["secondhand", "books", "digital"]}', 
 NOW(), 
 DATE_ADD(NOW(), INTERVAL 30 DAY), 
 1),

-- 2. 商家类型广告：校园快递代取服务（用户 ID=1）
('校园快递代取服务', 
 'https://placehold.co/800x300/4A90E2/FFFFFF?text=快递代取', 
 '/user/1', 
 '快速便捷的快递代取服务', 
 'mall', 
 2, 
 1, 
 'merchant', 
 1, 
 NULL, 
 NOW(), 
 DATE_ADD(NOW(), INTERVAL 60 DAY), 
 1),

-- 3. 商品类型广告：AirPods Pro 二代（商品 ID=6）
('AirPods Pro 二代 - 特价出售', 
 'https://placehold.co/800x300/50C878/FFFFFF?text=AirPods', 
 '/product/6', 
 '苹果原装，主动降噪，续航给力，箱说全', 
 'mall', 
 3, 
 1, 
 'product', 
 6, 
 NULL, 
 NOW(), 
 DATE_ADD(NOW(), INTERVAL 45 DAY), 
 1),

-- 4. 商品类型广告：二手 MacBook Pro（商品 ID=1）
('二手 MacBook Pro，9 成新', 
 'https://placehold.co/800x300/FFA500/FFFFFF?text=MacBook', 
 '/product/1', 
 '2020 款，i5 处理器，16G 内存，512G SSD', 
 'mall', 
 4, 
 1, 
 'product', 
 1, 
 NULL, 
 NOW(), 
 DATE_ADD(NOW(), INTERVAL 20 DAY), 
 1),

-- 5. 活动类型广告：美食节
('校园美食节即将开幕', 
 'https://placehold.co/800x300/DC143C/FFFFFF?text=美食节', 
 '/mall?tag=food', 
 '汇聚各地美食，不容错过', 
 'mall', 
 5, 
 1, 
 'activity', 
 NULL, 
 '{"tags": ["food"]}', 
 NOW(), 
 DATE_ADD(NOW(), INTERVAL 7 DAY), 
 1);

-- ========================================
-- 3. 验证数据
-- ========================================
SELECT '广告总数:' as info, COUNT(*) as count FROM advertisements WHERE position = 'mall';

SELECT id, title, ad_type, related_id, position, is_active, link_url
FROM advertisements 
WHERE position = 'mall'
ORDER BY sort_order;
