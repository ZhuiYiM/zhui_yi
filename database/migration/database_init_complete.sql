-- ========================================
-- 校园信息平台 - 完整数据库初始化脚本
-- 生成时间：2026-03-28
-- 数据库：campus_db
-- ========================================

-- 设置字符集
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========================================
-- 1. 基础表：校区
-- ========================================
DROP TABLE IF EXISTS `campuses`;
CREATE TABLE `campuses` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '校区名称',
  `code` varchar(50) NOT NULL COMMENT '校区编码（main/pingyuan/innovation）',
  `description` varchar(500) DEFAULT NULL COMMENT '校区描述',
  `address` varchar(255) DEFAULT NULL COMMENT '校区地址',
  `center_latitude` decimal(10,8) DEFAULT NULL COMMENT '中心点纬度',
  `center_longitude` decimal(11,8) DEFAULT NULL COMMENT '中心点经度',
  `zoom_level` int(11) DEFAULT 15 COMMENT '默认缩放级别',
  `map_config` text COMMENT '地图配置（JSON）',
  `is_active` tinyint(1) DEFAULT 1 COMMENT '是否启用',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序顺序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='校区表';

-- ========================================
-- 2. 基础表：用户
-- ========================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码（BCrypt 加密）',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone_number` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像 URL',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `student_id` varchar(50) DEFAULT NULL COMMENT '学号',
  `gender` varchar(10) DEFAULT NULL COMMENT '性别',
  `birth_date` date DEFAULT NULL COMMENT '出生日期',
  `college` varchar(100) DEFAULT NULL COMMENT '所在学院',
  `major` varchar(100) DEFAULT NULL COMMENT '专业班级',
  `bio` text COMMENT '个人简介',
  `hobbies` text COMMENT '兴趣爱好（JSON）',
  `status` int(11) DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
  `is_verified` int(11) DEFAULT 0 COMMENT '是否认证：0-否，1-是',
  `is_real_name_verified` int(11) DEFAULT 0 COMMENT '是否实名认证：0-否，1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `department` varchar(100) DEFAULT NULL COMMENT '部门',
  `role` varchar(20) DEFAULT 'user' COMMENT '角色：admin, user',
  `is_admin` int(11) DEFAULT 0 COMMENT '是否管理员：0-否，1-是',
  `is_merchant` int(11) DEFAULT 0 COMMENT '是否商户：0-否，1-是',
  `is_organization` int(11) DEFAULT 0 COMMENT '是否团体：0-否，1-是',
  `is_staff` int(11) DEFAULT 0 COMMENT '是否教职工：0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone_number`),
  KEY `idx_status` (`status`),
  KEY `idx_created_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========================================
-- 3. 基础表：话题
-- ========================================
DROP TABLE IF EXISTS `topics`;
CREATE TABLE `topics` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `title` varchar(200) NOT NULL COMMENT '话题标题',
  `content` text COMMENT '话题内容',
  `images` text COMMENT '图片 URL 数组（JSON）',
  `tags` text COMMENT '标签数组（JSON）',
  `level1_tag_code` varchar(50) DEFAULT NULL COMMENT '一级标签代码（身份标签）',
  `topic_tag_codes` text COMMENT '话题标签代码数组（JSON）',
  `product_tag_codes` text COMMENT '商品标签代码数组（JSON）',
  `location_tag_codes` text COMMENT '地点标签代码数组（JSON）',
  `likes_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `comments_count` int(11) DEFAULT 0 COMMENT '评论数',
  `views_count` int(11) DEFAULT 0 COMMENT '浏览数',
  `collections_count` int(11) DEFAULT 0 COMMENT '收藏数',
  `is_essence` int(11) DEFAULT 0 COMMENT '是否精华：0-否，1-是',
  `is_top` int(11) DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
  `status` int(11) DEFAULT 1 COMMENT '状态：0-删除，1-发布',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

-- ========================================
-- 4. 基础表：商品
-- ========================================
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `seller_id` int(11) NOT NULL COMMENT '卖家 ID',
  `title` varchar(200) NOT NULL COMMENT '商品标题',
  `description` text COMMENT '商品描述',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `category_id` int(11) DEFAULT NULL COMMENT '分类 ID',
  `images` text COMMENT '图片 URL 数组（JSON）',
  `trade_methods` text COMMENT '交易方式（JSON）',
  `contact_info` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `location` varchar(200) DEFAULT NULL COMMENT '交易地点',
  `status` int(11) DEFAULT 1 COMMENT '状态：0-下架，1-上架，2-已售出',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览数',
  `like_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `favorite_count` int(11) DEFAULT 0 COMMENT '收藏数',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ========================================
-- 5. 基础表：商品规格
-- ========================================
DROP TABLE IF EXISTS `product_specifications`;
CREATE TABLE `product_specifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL COMMENT '商品 ID',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称',
  `spec_value` varchar(100) NOT NULL COMMENT '规格值',
  `spec_icon` varchar(255) DEFAULT NULL COMMENT '规格图标',
  `stock_quantity` int(11) DEFAULT 0 COMMENT '库存数量',
  `price_adjustment` decimal(10,2) DEFAULT 0.00 COMMENT '价格调整',
  `is_default` int(11) DEFAULT 0 COMMENT '是否默认：0-否，1-是',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';

-- ========================================
-- 6. 基础表：订单
-- ========================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `buyer_id` int(11) NOT NULL COMMENT '买家 ID',
  `seller_id` int(11) NOT NULL COMMENT '卖家 ID',
  `product_id` int(11) NOT NULL COMMENT '商品 ID',
  `order_status` int(11) DEFAULT 0 COMMENT '订单状态：0-待付款，1-待发货，2-已发货，3-已完成，4-已取消',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `buyer_contact` varchar(100) DEFAULT NULL COMMENT '买家联系方式',
  `buyer_message` text COMMENT '买家留言',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '支付方式：wechat/alipay/station',
  `payment_status` int(11) DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ========================================
-- 7. 基础表：消息
-- ========================================
DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `sender_id` int(11) NOT NULL COMMENT '发送者 ID',
  `receiver_id` int(11) NOT NULL COMMENT '接收者 ID',
  `title` varchar(200) DEFAULT NULL COMMENT '消息标题',
  `content` text NOT NULL COMMENT '消息内容',
  `message_type` varchar(50) NOT NULL COMMENT '消息类型：system/private_message/order_notification',
  `is_read` int(11) DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';

-- ========================================
-- 8. 基础表：地点
-- ========================================
DROP TABLE IF EXISTS `locations`;
CREATE TABLE `locations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `campus_id` int(11) NOT NULL COMMENT '校区 ID',
  `name` varchar(100) NOT NULL COMMENT '地点名称',
  `description` text COMMENT '地点描述',
  `category` varchar(50) DEFAULT NULL COMMENT '地点分类',
  `latitude` decimal(10,8) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(11,8) DEFAULT NULL COMMENT '经度',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `image_url` varchar(500) DEFAULT NULL COMMENT '图片 URL',
  `opening_hours` varchar(100) DEFAULT NULL COMMENT '开放时间',
  `contact_info` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `facilities` text COMMENT '设施标签（JSON）',
  `is_popular` tinyint(1) DEFAULT 0 COMMENT '是否热门',
  `view_count` int(11) DEFAULT 0 COMMENT '浏览次数',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_campus_id` (`campus_id`),
  KEY `idx_category` (`category`),
  KEY `idx_is_popular` (`is_popular`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点表';

-- ========================================
-- 9. 基础表：用户位置标记
-- ========================================
DROP TABLE IF EXISTS `user_location_mark`;
CREATE TABLE `user_location_mark` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '标记者用户 ID',
  `campus_id` int(11) NOT NULL COMMENT '校区 ID',
  `location_name` varchar(100) NOT NULL COMMENT '位置名称',
  `latitude` decimal(10,8) NOT NULL COMMENT '纬度',
  `longitude` decimal(11,8) NOT NULL COMMENT '经度',
  `address_detail` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `mark_type` varchar(50) DEFAULT NULL COMMENT '标记类型：merchant_shop/organization_activity/meeting_point',
  `mark_category` varchar(50) DEFAULT NULL COMMENT '地点分类：building/area/facility/other',
  `user_identity_type` varchar(50) DEFAULT NULL COMMENT '用户身份类型',
  `related_entity_id` bigint(20) DEFAULT NULL COMMENT '关联的商户 ID 或组织 ID',
  `description` text COMMENT '位置描述',
  `contact_info` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `images` text COMMENT '图片 URL 数组（JSON）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `visibility` varchar(20) DEFAULT 'public' COMMENT '可见性：public/followers/private',
  `status` int(11) DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_campus_id` (`campus_id`),
  KEY `idx_mark_type` (`mark_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户位置标记表';

-- ========================================
-- 10. 基础表：点赞
-- ========================================
DROP TABLE IF EXISTS `likes`;
CREATE TABLE `likes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户 ID',
  `target_type` varchar(50) NOT NULL COMMENT '目标类型：topic/comment/product',
  `target_id` int(11) NOT NULL COMMENT '目标 ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_type`, `target_id`),
  KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- ========================================
-- 11. 基础表：话题点赞
-- ========================================
DROP TABLE IF EXISTS `topic_likes`;
CREATE TABLE `topic_likes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `topic_id` bigint(20) NOT NULL COMMENT '话题 ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_topic_user` (`topic_id`, `user_id`),
  KEY `idx_topic_id` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题点赞表';

-- ========================================
-- 12. 基础表：话题评论
-- ========================================
DROP TABLE IF EXISTS `topic_comments`;
CREATE TABLE `topic_comments` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `topic_id` bigint(20) NOT NULL COMMENT '话题 ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论 ID',
  `likes_count` int(11) DEFAULT 0 COMMENT '点赞数',
  `status` int(11) DEFAULT 1 COMMENT '状态：0-删除，1-正常',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题评论表';

-- ========================================
-- 13. 基础表：话题收藏
-- ========================================
DROP TABLE IF EXISTS `topic_collections`;
CREATE TABLE `topic_collections` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `topic_id` bigint(20) NOT NULL COMMENT '话题 ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_topic` (`user_id`, `topic_id`),
  KEY `idx_topic_id` (`topic_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题收藏表';

-- ========================================
-- 14. 基础表：用户关注
-- ========================================
DROP TABLE IF EXISTS `user_follows`;
CREATE TABLE `user_follows` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `follower_id` bigint(20) NOT NULL COMMENT '关注者 ID',
  `followed_id` bigint(20) NOT NULL COMMENT '被关注者 ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followed` (`follower_id`, `followed_id`),
  KEY `idx_followed_id` (`followed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- ========================================
-- 15. 基础表：用户身份
-- ========================================
DROP TABLE IF EXISTS `user_identity`;
CREATE TABLE `user_identity` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `identity_type` varchar(50) NOT NULL COMMENT '身份类型：student/staff/merchant/organization',
  `identity_name` varchar(100) DEFAULT NULL COMMENT '身份显示名称',
  `verified` int(11) DEFAULT 0 COMMENT '是否认证：0-未认证，1-已认证',
  `verified_at` datetime DEFAULT NULL COMMENT '认证通过时间',
  `expires_at` datetime DEFAULT NULL COMMENT '过期时间',
  `extra_info` text COMMENT '额外信息（JSON）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_identity_type` (`identity_type`),
  KEY `idx_verified` (`verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户身份表';

-- ========================================
-- 16. 基础表：用户认证
-- ========================================
DROP TABLE IF EXISTS `user_verification`;
CREATE TABLE `user_verification` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `verification_type` varchar(50) NOT NULL COMMENT '认证类型：student/merchant/organization',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `student_id` varchar(50) DEFAULT NULL COMMENT '学号',
  `college` varchar(100) DEFAULT NULL COMMENT '学院',
  `shop_name` varchar(100) DEFAULT NULL COMMENT '店铺名称',
  `organization_name` varchar(100) DEFAULT NULL COMMENT '组织名称',
  `leader_name` varchar(50) DEFAULT NULL COMMENT '负责人姓名',
  `extra_info` text COMMENT '额外信息（JSON）',
  `status` int(11) DEFAULT 0 COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `submitted_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `reviewed_at` datetime DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` bigint(20) DEFAULT NULL COMMENT '审核人 ID',
  `reject_reason` text COMMENT '拒绝原因',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_verification_type` (`verification_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表';

-- ========================================
-- 17. 基础表：举报
-- ========================================
DROP TABLE IF EXISTS `reports`;
CREATE TABLE `reports` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `reporter_id` bigint(20) NOT NULL COMMENT '举报人 ID',
  `reported_user_id` bigint(20) DEFAULT NULL COMMENT '被举报用户 ID',
  `reported_content_id` bigint(20) DEFAULT NULL COMMENT '被举报内容 ID',
  `content_type` varchar(50) NOT NULL COMMENT '内容类型：topic/comment/product/user',
  `reason` text NOT NULL COMMENT '举报原因',
  `evidence` text COMMENT '证据（JSON）',
  `status` int(11) DEFAULT 0 COMMENT '状态：0-待处理，1-处理中，2-已处理',
  `handle_result` text COMMENT '处理结果',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人 ID',
  `handled_at` datetime DEFAULT NULL COMMENT '处理时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_reported_user_id` (`reported_user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报表';

-- ========================================
-- 18. 基础表：广告
-- ========================================
DROP TABLE IF EXISTS `advertisements`;
CREATE TABLE `advertisements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL COMMENT '广告标题',
  `image_url` varchar(500) NOT NULL COMMENT '广告图片 URL',
  `target_url` varchar(500) DEFAULT NULL COMMENT '目标链接',
  `position` varchar(50) DEFAULT NULL COMMENT '广告位置：home/topic/product',
  `start_date` date NOT NULL COMMENT '开始日期',
  `end_date` date NOT NULL COMMENT '结束日期',
  `click_count` int(11) DEFAULT 0 COMMENT '点击次数',
  `view_count` int(11) DEFAULT 0 COMMENT '展示次数',
  `status` int(11) DEFAULT 1 COMMENT '状态：0-下线，1-上线',
  `priority` int(11) DEFAULT 0 COMMENT '优先级',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_position` (`position`),
  KEY `idx_status` (`status`),
  KEY `idx_date_range` (`start_date`, `end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='广告表';

-- ========================================
-- 19. 基础表：系统设置
-- ========================================
DROP TABLE IF EXISTS `system_settings`;
CREATE TABLE `system_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值',
  `setting_type` varchar(50) DEFAULT 'string' COMMENT '设置类型：string/json/boolean/number',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- ========================================
-- 20. 基础表：操作日志
-- ========================================
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL COMMENT '用户 ID',
  `operation` varchar(100) NOT NULL COMMENT '操作',
  `module` varchar(50) DEFAULT NULL COMMENT '模块',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求 URL',
  `request_params` text COMMENT '请求参数',
  `response_status` int(11) DEFAULT NULL COMMENT '响应状态',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP 地址',
  `user_agent` varchar(500) DEFAULT NULL COMMENT 'User-Agent',
  `execution_time` int(11) DEFAULT NULL COMMENT '执行时间（ms）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ========================================
-- 21. 基础表：分类
-- ========================================
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '图标',
  `parent_id` int(11) DEFAULT NULL COMMENT '父分类 ID',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- ========================================
-- 22. 基础表：商品标签
-- ========================================
DROP TABLE IF EXISTS `product_tags`;
CREATE TABLE `product_tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `usage_count` int(11) DEFAULT 0 COMMENT '使用次数',
  `trend_score` decimal(10,2) DEFAULT 0.00 COMMENT '趋势分数',
  `last_used_at` datetime DEFAULT NULL COMMENT '最后使用时间',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态：active/banned/pending',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品标签表';

-- ========================================
-- 23. 基础表：话题标签
-- ========================================
DROP TABLE IF EXISTS `topic_tags`;
CREATE TABLE `topic_tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `usage_count` int(11) DEFAULT 0 COMMENT '使用次数',
  `status` varchar(20) DEFAULT 'active' COMMENT '状态',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题标签表';

-- ========================================
-- 24. 基础表：地点标签
-- ========================================
DROP TABLE IF EXISTS `location_tags`;
CREATE TABLE `location_tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '标签名称',
  `category` varchar(50) DEFAULT NULL COMMENT '分类',
  `level` int(11) DEFAULT 1 COMMENT '标签级别',
  `parent_id` int(11) DEFAULT NULL COMMENT '父标签 ID',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地点标签表';

-- ========================================
-- 25. 基础表：身份标签
-- ========================================
DROP TABLE IF EXISTS `identity_tags`;
CREATE TABLE `identity_tags` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tag_code` varchar(50) NOT NULL COMMENT '标签代码',
  `tag_name` varchar(100) NOT NULL COMMENT '标签名称',
  `tag_icon` varchar(255) DEFAULT NULL COMMENT '标签图标',
  `tag_color` varchar(20) DEFAULT NULL COMMENT '标签颜色',
  `tag_level` int(11) DEFAULT 1 COMMENT '标签级别',
  `parent_id` int(11) DEFAULT NULL COMMENT '父标签 ID',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `is_active` int(11) DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_code` (`tag_code`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='身份标签表';

-- ========================================
-- 26. 基础表：商品收藏
-- ========================================
DROP TABLE IF EXISTS `product_favorites`;
CREATE TABLE `product_favorites` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户 ID',
  `product_id` int(11) NOT NULL COMMENT '商品 ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品收藏表';

-- ========================================
-- 27. 基础表：订单规格
-- ========================================
DROP TABLE IF EXISTS `order_specifications`;
CREATE TABLE `order_specifications` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL COMMENT '订单 ID',
  `spec_id` int(11) DEFAULT NULL COMMENT '规格 ID',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称',
  `spec_value` varchar(100) NOT NULL COMMENT '规格值',
  `spec_icon` varchar(255) DEFAULT NULL COMMENT '规格图标',
  `quantity` int(11) NOT NULL COMMENT '数量',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单规格表';

-- ========================================
-- 28. 基础表：订单评价
-- ========================================
DROP TABLE IF EXISTS `order_reviews`;
CREATE TABLE `order_reviews` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL COMMENT '订单 ID',
  `buyer_id` int(11) NOT NULL COMMENT '买家 ID',
  `seller_id` int(11) NOT NULL COMMENT '卖家 ID',
  `product_id` int(11) NOT NULL COMMENT '商品 ID',
  `rating` int(11) NOT NULL COMMENT '评分：1-5',
  `content` text COMMENT '评价内容',
  `images` text COMMENT '评价图片（JSON）',
  `is_anonymous` int(11) DEFAULT 0 COMMENT '是否匿名',
  `reply_content` text COMMENT '卖家回复',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单评价表';

-- ========================================
-- 29. 基础表：订单取消原因
-- ========================================
DROP TABLE IF EXISTS `order_cancel_reasons`;
CREATE TABLE `order_cancel_reasons` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL COMMENT '订单 ID',
  `reason` text NOT NULL COMMENT '取消原因',
  `reason_type` varchar(50) DEFAULT NULL COMMENT '原因类型',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单取消原因表';

-- ========================================
-- 30. 基础表：用户屏蔽
-- ========================================
DROP TABLE IF EXISTS `user_blocks`;
CREATE TABLE `user_blocks` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `blocker_id` int(11) NOT NULL COMMENT '屏蔽者 ID',
  `blocked_id` int(11) NOT NULL COMMENT '被屏蔽者 ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blocker_blocked` (`blocker_id`, `blocked_id`),
  KEY `idx_blocked_id` (`blocked_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户屏蔽表';

-- ========================================
-- 31. 基础表：验证码
-- ========================================
DROP TABLE IF EXISTS `verification_codes`;
CREATE TABLE `verification_codes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `phone_or_email` varchar(100) NOT NULL COMMENT '手机号或邮箱',
  `code` varchar(10) NOT NULL COMMENT '验证码',
  `type` varchar(50) NOT NULL COMMENT '类型：register/login/reset',
  `is_used` int(11) DEFAULT 0 COMMENT '是否已使用',
  `expires_at` datetime NOT NULL COMMENT '过期时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_phone_email` (`phone_or_email`),
  KEY `idx_code` (`code`),
  KEY `idx_expires_at` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';

-- ========================================
-- 32. 基础表：默认隐私等级
-- ========================================
DROP TABLE IF EXISTS `default_privacy_levels`;
CREATE TABLE `default_privacy_levels` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `topic_visibility` varchar(20) DEFAULT 'public' COMMENT '话题可见性',
  `location_visibility` varchar(20) DEFAULT 'public' COMMENT '位置可见性',
  `product_visibility` varchar(20) DEFAULT 'public' COMMENT '商品可见性',
  `allow_messages` int(11) DEFAULT 1 COMMENT '是否允许私信',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='默认隐私等级表';

-- ========================================
-- 33. 基础表：隐私设置
-- ========================================
DROP TABLE IF EXISTS `privacy_settings`;
CREATE TABLE `privacy_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户 ID',
  `setting_key` varchar(100) NOT NULL COMMENT '设置键',
  `setting_value` text COMMENT '设置值',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_setting_key` (`setting_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='隐私设置表';

-- ========================================
-- 34. 基础表：用户安全设置
-- ========================================
DROP TABLE IF EXISTS `user_security_settings`;
CREATE TABLE `user_security_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL COMMENT '用户 ID',
  `two_factor_enabled` int(11) DEFAULT 0 COMMENT '是否启用双因素认证',
  `login_notification` int(11) DEFAULT 1 COMMENT '是否登录通知',
  `security_question` text COMMENT '密保问题（JSON）',
  `trusted_devices` text COMMENT '信任设备（JSON）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户安全设置表';

-- ========================================
-- 35. 基础表：管理员配置
-- ========================================
DROP TABLE IF EXISTS `admin_configs`;
CREATE TABLE `admin_configs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员配置表';

-- ========================================
-- 36. 基础表：管理员操作日志
-- ========================================
DROP TABLE IF EXISTS `admin_operation_logs`;
CREATE TABLE `admin_operation_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NOT NULL COMMENT '管理员 ID',
  `operation` varchar(100) NOT NULL COMMENT '操作',
  `module` varchar(50) DEFAULT NULL COMMENT '模块',
  `target_type` varchar(50) DEFAULT NULL COMMENT '目标类型',
  `target_id` bigint(20) DEFAULT NULL COMMENT '目标 ID',
  `old_value` text COMMENT '旧值（JSON）',
  `new_value` text COMMENT '新值（JSON）',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP 地址',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_operation` (`operation`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- ========================================
-- 37. 基础表：管理员通知
-- ========================================
DROP TABLE IF EXISTS `admin_notifications`;
CREATE TABLE `admin_notifications` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NOT NULL COMMENT '管理员 ID',
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `content` text NOT NULL COMMENT '通知内容',
  `is_read` int(11) DEFAULT 0 COMMENT '是否已读',
  `read_at` datetime DEFAULT NULL COMMENT '阅读时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_is_read` (`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员通知表';

-- ========================================
-- 38. 基础表：管理员快捷操作
-- ========================================
DROP TABLE IF EXISTS `admin_quick_actions`;
CREATE TABLE `admin_quick_actions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NOT NULL COMMENT '管理员 ID',
  `action_name` varchar(100) NOT NULL COMMENT '操作名称',
  `action_type` varchar(50) NOT NULL COMMENT '操作类型',
  `action_config` text COMMENT '操作配置（JSON）',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `is_enabled` int(11) DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员快捷操作表';

-- ========================================
-- 39. 基础表：管理员自定义列
-- ========================================
DROP TABLE IF EXISTS `admin_custom_columns`;
CREATE TABLE `admin_custom_columns` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NOT NULL COMMENT '管理员 ID',
  `module` varchar(50) NOT NULL COMMENT '模块',
  `columns` text NOT NULL COMMENT '列配置（JSON）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_module` (`admin_id`, `module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员自定义列表';

-- ========================================
-- 初始化数据
-- ========================================

-- 1. 插入默认管理员账户（密码：admin123，BCrypt 加密）
INSERT INTO `users` (`username`, `password`, `email`, `role`, `is_admin`, `status`, `created_at`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8Qe6mCYYqQ1B6zKZ8qXZJ5YqKZ5qG', 'admin@campus.com', 'admin', 1, 1, NOW());

-- 2. 插入默认校区
INSERT INTO `campuses` (`name`, `code`, `description`, `address`, `center_latitude`, `center_longitude`, `zoom_level`, `is_active`, `sort_order`) VALUES
('主校区', 'main', '主校区', 'XX 省 XX 市 XX 区 XX 路 1 号', 39.9042, 116.4074, 15, 1, 1),
('分校区', 'pingyuan', '分校区', 'XX 省 XX 市 XX 区 XX 路 2 号', 39.9142, 116.4174, 15, 1, 2);

-- 3. 插入系统设置
INSERT INTO `system_settings` (`setting_key`, `setting_value`, `setting_type`, `description`) VALUES
('site_name', '校园信息平台', 'string', '网站名称'),
('site_description', '校园生活服务平台', 'string', '网站描述'),
('allow_registration', 'true', 'boolean', '是否允许注册'),
('default_topic_visibility', 'public', 'string', '默认话题可见性'),
('max_upload_size', '10485760', 'number', '最大上传文件大小（字节）');

-- 4. 插入默认分类
INSERT INTO `categories` (`name`, `icon`, `parent_id`, `sort_order`) VALUES
('二手交易', 'shopping', NULL, 1),
('教材书籍', 'book', 1, 1),
('数码产品', 'digital', 1, 2),
('生活服务', 'service', NULL, 2),
('美食外卖', 'food', 4, 1),
('兼职信息', 'parttime', NULL, 3);

-- 5. 插入默认身份标签
INSERT INTO `identity_tags` (`tag_code`, `tag_name`, `tag_icon`, `tag_color`, `tag_level`, `sort_order`, `is_active`) VALUES
('student', '学生', 'user', '#1890ff', 1, 1, 1),
('merchant', '商户', 'shop', '#52c41a', 1, 2, 1),
('organization', '团体', 'team', '#722ed1', 1, 3, 1),
('staff', '教职工', 'staff', '#fa8c16', 1, 4, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================
-- 完成
-- ========================================
