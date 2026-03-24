-- 初始化标签数据 - 用于测试
-- 执行时间：2026-03-24

-- 1. 话题标签（原身份标签 + 话题分类）
INSERT INTO `topic_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`) VALUES
-- 身份标签
('admin', 'system', 'active', 1, 1, '👨‍', '#FF6B6B'),
('student', 'system', 'active', 1, 2, '👨‍🎓', '#4ECDC4'),
('merchant', 'system', 'active', 1, 3, '🏪', '#45B7D1'),
('organization', 'system', 'active', 1, 4, '👥', '#96CEB4'),
('individual', 'system', 'active', 1, 5, '👤', '#FFEEAD'),
-- 话题分类
('study', 'system', 'active', 1, 6, '📚', '#FFD93D'),
('life', 'system', 'active', 1, 7, '🍜', '#6BCB77'),
('activity', 'system', 'active', 1, 8, '🎉', '#FF6B6B'),
('question', 'system', 'active', 1, 9, '❓', '#4D96FF'),
('share', 'system', 'active', 1, 10, '📢', '#FF9FF3');

-- 2. 商品标签
INSERT INTO `product_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`) VALUES
('electronics', 'system', 'active', 1, 1, '📱', '#4D96FF'),
('books', 'system', 'active', 1, 2, '📚', '#FFD93D'),
('sports', 'system', 'active', 1, 3, '⚽', '#6BCB77'),
('daily', 'system', 'active', 1, 4, '🛒', '#FF6B6B'),
('food', 'system', 'active', 1, 5, '🍔', '#FF9FF3'),
('clothing', 'system', 'active', 1, 6, '👕', '#4ECDC4'),
('digital', 'system', 'active', 1, 7, '💻', '#45B7D1');

-- 3. 地点标签
INSERT INTO `location_tag` (`name`, `type`, `status`, `is_active`, `sort_order`, `icon`, `color`) VALUES
('library', 'system', 'active', 1, 1, '📚', '#FFD93D'),
('cafeteria', 'system', 'active', 1, 2, '🍜', '#FF6B6B'),
('dormitory', 'system', 'active', 1, 3, '🏠', '#4ECDC4'),
('classroom', 'system', 'active', 1, 4, '🏫', '#4D96FF'),
('playground', 'system', 'active', 1, 5, '⚽', '#6BCB77'),
('gym', 'system', 'active', 1, 6, '💪', '#FF9FF3'),
('lab', 'system', 'active', 1, 7, '🔬', '#45B7D1');
