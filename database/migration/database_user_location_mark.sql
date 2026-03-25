-- ========================================
-- 校园信息平台 - 用户位置标记功能
-- 创建时间：2026-03-24
-- 说明：创建 user_location_mark 表用于存储用户自定义位置标记
-- ========================================

USE campus_db;

-- ========================================
-- 1. 创建 user_location_mark 表
-- ========================================
DROP TABLE IF EXISTS `user_location_mark`;

CREATE TABLE `user_location_mark` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键 ID',
  `user_id` BIGINT NOT NULL COMMENT '标记者用户 ID',
  `campus_id` INT NOT NULL COMMENT '校区 ID',
  
  -- 位置信息
  `location_name` VARCHAR(100) NOT NULL COMMENT '位置名称',
  `latitude` DECIMAL(10,8) NOT NULL COMMENT '纬度',
  `longitude` DECIMAL(11,8) NOT NULL COMMENT '经度',
  `address_detail` VARCHAR(255) COMMENT '详细地址描述',
  
  -- 标记类型
  `mark_type` VARCHAR(50) NOT NULL COMMENT '标记类型：merchant_shop/organization_activity/meeting_point',
  `mark_category` VARCHAR(50) COMMENT '地点分类：building/area/facility/other',
  
  -- 关联身份
  `user_identity_type` VARCHAR(50) COMMENT '用户身份类型：student/merchant/organization',
  `related_entity_id` BIGINT COMMENT '关联的商户 ID 或组织 ID',
  
  -- 描述信息
  `description` TEXT COMMENT '位置描述',
  `contact_info` VARCHAR(100) COMMENT '联系方式',
  `images` JSON COMMENT '图片 URL 数组',
  
  -- 时间信息
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `is_permanent` TINYINT(1) DEFAULT 0 COMMENT '是否永久标记：0-临时，1-永久',
  
  -- 审核状态
  `verification_status` VARCHAR(20) DEFAULT 'pending' COMMENT '审核状态：pending/approved/rejected',
  `verified_at` DATETIME COMMENT '审核通过时间',
  `reviewer_id` BIGINT COMMENT '审核员 ID',
  `review_comment` TEXT COMMENT '审核意见',
  
  -- 可见性
  `visibility` VARCHAR(20) DEFAULT 'public_active' COMMENT '可见性：public_active（主动所有人可见）/public_passive（被动可见）/private（仅自己可见）',
  
  -- 统计数据
  `view_count` INT DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT DEFAULT 0 COMMENT '点赞数',
  
  -- 状态控制
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否有效：0-无效，1-有效',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `expires_at` DATETIME COMMENT '过期时间',
  
  -- 索引
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_campus_id` (`campus_id`),
  INDEX `idx_mark_type` (`mark_type`),
  INDEX `idx_verification_status` (`verification_status`),
  INDEX `idx_visibility` (`visibility`),
  INDEX `idx_location` (`latitude`, `longitude`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户位置标记表';

-- ========================================
-- 2. 插入测试数据
-- ========================================

-- 测试数据 1: 学生标记的约见地点（已审核通过）
INSERT INTO `user_location_mark` (
  `user_id`, `campus_id`, `location_name`, `latitude`, `longitude`, 
  `mark_type`, `mark_category`, `user_identity_type`, 
  `description`, `verification_status`, `visibility`, `is_permanent`
) VALUES (
  2, 1, '图书馆门口见面点', 35.308123, 113.926234,
  'meeting_point', 'building', 'student',
  '经常在图书馆门口集合，方便找', 'approved', 'public', 0
);

-- 测试数据 2: 商户标记的店铺位置（已审核通过）
INSERT INTO `user_location_mark` (
  `user_id`, `campus_id`, `location_name`, `latitude`, `longitude`,
  `mark_type`, `mark_category`, `user_identity_type`,
  `description`, `contact_info`, `verification_status`, `visibility`, `is_permanent`
) VALUES (
  3, 1, '校园打印店', 35.307456, 113.927123,
  'merchant_shop', 'facility', 'merchant',
  '提供打印、复印、扫描服务，价格优惠', '13800138000', 'approved', 'public', 1
);

-- 测试数据 3: 社团标记的活动地点（待审核）
INSERT INTO `user_location_mark` (
  `user_id`, `campus_id`, `location_name`, `latitude`, `longitude`,
  `mark_type`, `mark_category`, `user_identity_type`,
  `description`, `start_time`, `end_time`, `verification_status`, `visibility`
) VALUES (
  4, 1, '社团招新活动点', 35.306789, 113.928234,
  'organization_activity', 'area', 'organization',
  '每学期初的社团招新活动固定地点', 
  '2026-03-25 09:00:00', '2026-03-25 17:00:00',
  'pending', 'public'
);

-- 测试数据 4: 私密约见地点
INSERT INTO `user_location_mark` (
  `user_id`, `campus_id`, `location_name`, `latitude`, `longitude`,
  `mark_type`, `mark_category`, `user_identity_type`,
  `description`, `verification_status`, `visibility`
) VALUES (
  2, 1, '情人湖长椅', 35.306500, 113.931000,
  'meeting_point', 'area', 'student',
  '安静的聊天地点', 'approved', 'friends'
);

-- ========================================
-- 3. 验证数据
-- ========================================

-- 查看标记总数
SELECT '用户位置标记总数:' as info, COUNT(*) as count FROM user_location_mark;

-- 按类型统计
SELECT mark_type, COUNT(*) as count 
FROM user_location_mark 
GROUP BY mark_type;

-- 按审核状态统计
SELECT verification_status, COUNT(*) as count 
FROM user_location_mark 
GROUP BY verification_status;

-- 查看校区的公开标记
SELECT 
  m.id,
  m.location_name,
  m.mark_type,
  m.latitude,
  m.longitude,
  m.description,
  u.username as creator_name
FROM user_location_mark m
JOIN users u ON m.user_id = u.id
WHERE m.campus_id = 1 
  AND m.verification_status = 'approved'
  AND m.visibility = 'public'
  AND m.is_active = 1;
