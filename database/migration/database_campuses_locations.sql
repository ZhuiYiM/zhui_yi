-- ========================================
-- 校园信息平台 - 地图导引功能数据库表
-- 创建时间：2026-03-19
-- 说明：校区和地点信息管理
-- ========================================

USE campus_db;

-- ========================================
-- 1. 删除旧表（如果存在）并重新创建
-- ========================================
-- 注意：这会删除已有的 campuses 和 locations 表及其数据
-- 如果需要保留数据，请注释掉下面的 DROP 语句
DROP TABLE IF EXISTS `locations`;
DROP TABLE IF EXISTS `campuses`;

-- 创建校区表
CREATE TABLE `campuses` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '校区 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '校区名称',
    `code` VARCHAR(50) NOT NULL UNIQUE COMMENT '校区编码（main/pingyuan/innovation）',
    `description` TEXT DEFAULT NULL COMMENT '校区描述',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '校区地址',
    `center_latitude` DECIMAL(10, 8) DEFAULT NULL COMMENT '中心点纬度',
    `center_longitude` DECIMAL(11, 8) DEFAULT NULL COMMENT '中心点经度',
    `zoom_level` INT DEFAULT 15 COMMENT '默认缩放级别',
    `map_config` JSON DEFAULT NULL COMMENT '地图配置（各服务商的配置信息）',
    `is_active` TINYINT DEFAULT 1 COMMENT '是否启用：0-禁用，1-启用',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='校区表';

-- ========================================
-- 2. 创建地点表
-- ========================================
CREATE TABLE `locations` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '地点 ID',
    `campus_id` INT NOT NULL COMMENT '校区 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '地点名称',
    `description` TEXT DEFAULT NULL COMMENT '地点描述',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '地点分类：teaching-教学楼，library-图书馆，cafeteria-食堂，dormitory-宿舍，sports-体育设施，admin-行政楼，other-其他',
    `latitude` DECIMAL(10, 8) DEFAULT NULL COMMENT '纬度',
    `longitude` DECIMAL(11, 8) DEFAULT NULL COMMENT '经度',
    `icon` VARCHAR(50) DEFAULT NULL COMMENT '图标 emoji 或 class',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '地点图片 URL',
    `opening_hours` VARCHAR(100) DEFAULT NULL COMMENT '开放时间',
    `contact_info` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
    `facilities` JSON DEFAULT NULL COMMENT '设施标签（JSON 数组）',
    `is_popular` TINYINT DEFAULT 0 COMMENT '是否热门：0-否，1-是',
    `view_count` INT DEFAULT 0 COMMENT '浏览次数',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_campus_id` (`campus_id`),
    INDEX `idx_category` (`category`),
    FOREIGN KEY (`campus_id`) REFERENCES `campuses`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='地点表';

-- ========================================
-- 3. 插入校区测试数据
-- ========================================
-- 河南师范大学本部校区
INSERT INTO `campuses` (`name`, `code`, `description`, `address`, `center_latitude`, `center_longitude`, `zoom_level`, `map_config`, `is_active`, `sort_order`) 
VALUES (
    '本部校区', 
    'main', 
    '河南师范大学主校区，历史悠久，文化底蕴深厚', 
    '河南省新乡市建设东路 46 号', 
    35.307736, 
    113.926765, 
    16,
    JSON_OBJECT(
        'baidu', JSON_OBJECT('lat', 35.307736, 'lng', 113.926765, 'zoom', 16),
        'gaode', JSON_OBJECT('lat', 35.307736, 'lng', 113.926765, 'zoom', 16),
        'tencent', JSON_OBJECT('lat', 35.307736, 'lng', 113.926765, 'zoom', 16)
    ),
    1, 
    1
);

-- 平原湖校区
INSERT INTO `campuses` (`name`, `code`, `description`, `address`, `center_latitude`, `center_longitude`, `zoom_level`, `map_config`, `is_active`, `sort_order`) 
VALUES (
    '平原湖校区', 
    'pingyuan', 
    '平原湖校区，现代化校区，风景优美', 
    '河南省新乡市金穗大道东段', 
    35.295621, 
    113.966851, 
    15,
    JSON_OBJECT(
        'baidu', JSON_OBJECT('lat', 35.295621, 'lng', 113.966851, 'zoom', 15),
        'gaode', JSON_OBJECT('lat', 35.295621, 'lng', 113.966851, 'zoom', 15),
        'tencent', JSON_OBJECT('lat', 35.295621, 'lng', 113.966851, 'zoom', 15)
    ),
    1, 
    2
);

-- 创新港校区
INSERT INTO `campuses` (`name`, `code`, `description`, `address`, `center_latitude`, `center_longitude`, `zoom_level`, `map_config`, `is_active`, `sort_order`) 
VALUES (
    '创新港校区', 
    'innovation', 
    '创新港校区，科研创新中心', 
    '河南省新乡市红旗区新二街', 
    35.274892, 
    113.934567, 
    15,
    JSON_OBJECT(
        'baidu', JSON_OBJECT('lat', 35.274892, 'lng', 113.934567, 'zoom', 15),
        'gaode', JSON_OBJECT('lat', 35.274892, 'lng', 113.934567, 'zoom', 15),
        'tencent', JSON_OBJECT('lat', 35.274892, 'lng', 113.934567, 'zoom', 15)
    ),
    1, 
    3
);

-- ========================================
-- 4. 插入地点测试数据 - 本部校区
-- ========================================
-- 本部校区地点
INSERT INTO `locations` (`campus_id`, `name`, `description`, `category`, `latitude`, `longitude`, `icon`, `is_popular`, `sort_order`) VALUES
(1, '图书馆', '校园最大图书资源中心，藏书丰富', 'library', 35.308123, 113.926234, '📚', 1, 1),
(1, '教学楼 A 栋', '主要教学区域，多媒体教室', 'teaching', 35.307456, 113.927123, '🏫', 1, 2),
(1, '教学楼 B 栋', '公共教学楼，大型阶梯教室', 'teaching', 35.307789, 113.926890, '🏫', 0, 3),
(1, '第一食堂', '学生餐厅，提供各地美食', 'cafeteria', 35.308567, 113.927456, '🍽️', 1, 4),
(1, '第二食堂', '特色小吃窗口', 'cafeteria', 35.307234, 113.926123, '🍽️', 0, 5),
(1, '体育馆', '室内运动场馆，篮球、羽毛球场地', 'sports', 35.306789, 113.928234, '⚽', 1, 6),
(1, '田径场', '标准塑胶跑道操场', 'sports', 35.306234, 113.927890, '🏟️', 0, 7),
(1, '学生宿舍 1 号楼', '本科生宿舍', 'dormitory', 35.309123, 113.926567, '🏠', 0, 8),
(1, '学生宿舍 2 号楼', '研究生宿舍', 'dormitory', 35.309456, 113.927234, '🏠', 0, 9),
(1, '行政楼', '学校行政办公场所', 'admin', 35.307890, 113.925678, '🏢', 0, 10),
(1, '实验楼', '专业实验室', 'teaching', 35.308234, 113.928123, '🔬', 0, 11),
(1, '校医院', '校园医疗服务', 'other', 35.306567, 113.926890, '🏥', 0, 12);

-- ========================================
-- 5. 插入地点测试数据 - 平原湖校区
-- ========================================
-- 平原湖校区地点
INSERT INTO `locations` (`campus_id`, `name`, `description`, `category`, `latitude`, `longitude`, `icon`, `is_popular`, `sort_order`) VALUES
(2, '平原湖图书馆', '平原湖校区图书中心，现代化设施', 'library', 35.296012, 113.966234, '📚', 1, 1),
(2, '教学楼', '平原湖校区教学区', 'teaching', 35.295456, 113.967123, '🏫', 1, 2),
(2, '实验楼', '实验教学综合楼', 'teaching', 35.295789, 113.967456, '🔬', 0, 3),
(2, '学生餐厅', '平原湖校区用餐区', 'cafeteria', 35.296234, 113.966789, '🍽️', 1, 4),
(2, '风味餐厅', '特色餐饮窗口', 'cafeteria', 35.295123, 113.967234, '🍽️', 0, 5),
(2, '体育场', '标准运动场', 'sports', 35.294567, 113.966123, '🏟️', 1, 6),
(2, '游泳馆', '室内游泳馆', 'sports', 35.294890, 113.967890, '🏊', 0, 7),
(2, '学生宿舍 1 号楼', '本科生宿舍', 'dormitory', 35.296567, 113.965678, '🏠', 0, 8),
(2, '学生宿舍 2 号楼', '本科生宿舍', 'dormitory', 35.296890, 113.966345, '🏠', 0, 9),
(2, '学术交流中心', '会议接待场所', 'admin', 35.295345, 113.965234, '🏢', 0, 10),
(2, '创新创业中心', '学生创新创业基地', 'other', 35.295678, 113.968123, '💡', 1, 11);

-- ========================================
-- 6. 插入地点测试数据 - 创新港校区
-- ========================================
-- 创新港校区地点
INSERT INTO `locations` (`campus_id`, `name`, `description`, `category`, `latitude`, `longitude`, `icon`, `is_popular`, `sort_order`) VALUES
(3, '创新港图书馆', '创新港图书资源中心', 'library', 35.275234, 113.934123, '📚', 1, 1),
(3, '实验楼 A 座', '创新港实验教学区', 'teaching', 35.274567, 113.934890, '🔬', 1, 2),
(3, '实验楼 B 座', '科研实验室', 'teaching', 35.274890, 113.935234, '🔬', 0, 3),
(3, '创新港餐厅', '创新港用餐区', 'cafeteria', 35.275567, 113.934567, '🍽️', 1, 4),
(3, '咖啡厅', '休闲交流场所', 'cafeteria', 35.275123, 113.935123, '☕', 0, 5),
(3, '创新中心', '科研创新区域', 'other', 35.274234, 113.933890, '💡', 1, 6),
(3, '会议中心', '学术会议场所', 'admin', 35.275890, 113.934234, '🏢', 0, 7),
(3, '创客空间', '学生创业孵化基地', 'other', 35.274678, 113.935678, '🚀', 1, 8),
(3, '健身中心', '健身器材室', 'sports', 35.273890, 113.934678, '💪', 0, 9);

-- ========================================
-- 7. 验证数据
-- ========================================
-- 查看校区总数
SELECT '校区总数:' as info, COUNT(*) as count FROM campuses;

-- 查看各校区地点数量
SELECT c.name as campus_name, COUNT(l.id) as location_count
FROM campuses c
LEFT JOIN locations l ON c.id = l.campus_id
GROUP BY c.id, c.name;

-- 查看热门地点
SELECT c.name as campus_name, l.name as location_name, l.category, l.is_popular
FROM locations l
JOIN campuses c ON l.campus_id = c.id
WHERE l.is_popular = 1
ORDER BY c.sort_order, l.sort_order;
