-- ========================================
-- 校园信息平台 - 广告管理数据库表
-- 创建时间：2026-03-23
-- 说明：广告位和广告内容管理
-- ========================================

USE campus_db;

-- ========================================
-- 1. 广告位表
-- ========================================
CREATE TABLE IF NOT EXISTS `advertisements` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '广告 ID',
    `title` VARCHAR(200) NOT NULL COMMENT '广告标题',
    `image_url` VARCHAR(500) COMMENT '广告图片 URL',
    `link_url` VARCHAR(500) COMMENT '跳转链接',
    `content` TEXT COMMENT '广告内容描述',
    `position` VARCHAR(50) NOT NULL DEFAULT 'home' COMMENT '广告位置：home-首页，topicwall-话题墙，mall-交易中心，map-地图',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `click_count` INT DEFAULT 0 COMMENT '点击次数',
    `view_count` INT DEFAULT 0 COMMENT '展示次数',
    `created_by` BIGINT COMMENT '创建人 ID',
    `updated_by` BIGINT COMMENT '更新人 ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_position` (`position`),
    INDEX `idx_is_active` (`is_active`),
    INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='广告表';

-- ========================================
-- 2. 插入测试广告数据
-- ========================================
INSERT INTO `advertisements` (`title`, `image_url`, `link_url`, `content`, `position`, `sort_order`, `is_active`, `start_time`, `end_time`, `created_by`) VALUES
('校园跳蚤市场开业大酬宾', 'https://placehold.co/800x300/FF6B6B/FFFFFF?text=跳蚤市场', '/mall', '二手物品交易，物美价廉', 'home', 1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
('图书馆新馆开放通知', 'https://placehold.co/800x300/4A90E2/FFFFFF?text=图书馆', '/location/1', '新图书馆正式开放，欢迎使用', 'home', 2, 1, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), 1),
('春季运动会报名开始', 'https://placehold.co/800x300/50C878/FFFFFF?text=运动会', '/topic/123', '春季运动会即日起开始报名', 'topicwall', 1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 10 DAY), 1),
('毕业季二手书特卖', 'https://placehold.co/800x300/FFA500/FFFFFF?text=二手书', '/mall?category=6', '学长学姐的笔记和经验', 'mall', 1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 20 DAY), 1),
('校园美食节即将开幕', 'https://placehold.co/800x300/DC143C/FFFFFF?text=美食节', '/topic/456', '汇聚各地美食，不容错过', 'home', 3, 1, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1);

-- ========================================
-- 3. 验证数据
-- ========================================
SELECT '广告总数:' as info, COUNT(*) as count FROM advertisements;

SELECT position, COUNT(*) as count, SUM(is_active) as active_count 
FROM advertisements 
GROUP BY position;
