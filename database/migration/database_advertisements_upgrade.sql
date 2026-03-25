-- ========================================
-- 校园信息平台 - 广告类型升级 SQL
-- 创建时间：2026-03-23
-- 说明：添加广告类型字段，支持三种广告类型
-- ========================================

USE campus_db;

-- ========================================
-- 1. 添加广告类型字段
-- ========================================
ALTER TABLE `advertisements` 
ADD COLUMN `ad_type` VARCHAR(50) DEFAULT 'product' COMMENT '广告类型：activity-活动标签筛选，merchant-商家页面，product-商品广告';

-- ========================================
-- 2. 添加关联 ID 字段（用于关联商品 ID、商家 ID 等）
-- ========================================
ALTER TABLE `advertisements` 
ADD COLUMN `related_id` BIGINT COMMENT '关联 ID：商品 ID、商家 ID 等，根据广告类型而定';

-- ========================================
-- 3. 添加标签筛选字段（用于活动类型广告）
-- ========================================
ALTER TABLE `advertisements` 
ADD COLUMN `filter_tags` VARCHAR(500) COMMENT '筛选标签：JSON 格式存储，用于活动类型广告的标签筛选';

-- ========================================
-- 4. 更新现有数据（如果有）
-- ========================================
-- 将现有数据默认设置为商品广告类型
UPDATE `advertisements` SET ad_type = 'product' WHERE ad_type IS NULL;

-- ========================================
-- 5. 添加索引
-- ========================================
CREATE INDEX `idx_ad_type` ON `advertisements` (`ad_type`);
CREATE INDEX `idx_related_id` ON `advertisements` (`related_id`);

-- ========================================
-- 6. 插入测试数据
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

-- 2. 商家类型广告：校园快递代取服务（使用真实存在的用户 ID）
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

-- 3. 商品类型广告：学霸笔记合集（使用真实存在的商品 ID）
('学霸笔记合集 - 高分必备', 
 'https://placehold.co/800x300/50C878/FFFFFF?text=学霸笔记', 
 '/product/6', 
 '各科重点整理，考试必备资料', 
 'mall', 
 3, 
 1, 
 'product', 
 6, 
 NULL, 
 NOW(), 
 DATE_ADD(NOW(), INTERVAL 45 DAY), 
 1),

-- 4. 商品类型广告：二手教材（使用真实存在的商品 ID）
('二手教材低价出售', 
 'https://placehold.co/800x300/FFA500/FFFFFF?text=二手教材', 
 '/product/1', 
 '九成新教材，价格优惠', 
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
-- 7. 验证数据
-- ========================================
SELECT '广告总数:' as info, COUNT(*) as count FROM advertisements;

SELECT ad_type, COUNT(*) as count, SUM(is_active) as active_count 
FROM advertisements 
GROUP BY ad_type;

SELECT id, title, ad_type, related_id, position, is_active 
FROM advertisements 
ORDER BY sort_order;
