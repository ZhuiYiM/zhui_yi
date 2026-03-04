-- ============================================
-- 校园信息平台 - 数据库初始化脚本
-- 版本：1.0
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS campus_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE campus_db;

-- ============================================
-- 1. 用户表 (users)
-- ============================================
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户 ID',
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码 (加密)',
    real_name VARCHAR(50) COMMENT '真实姓名',
    student_id VARCHAR(20) UNIQUE COMMENT '学号',
    avatar_url VARCHAR(255) COMMENT '头像 URL',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    bio VARCHAR(500) COMMENT '个人简介',
    college VARCHAR(100) COMMENT '学院',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(20) COMMENT '年级',
    gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常 2-注销',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at DATETIME COMMENT '最后登录时间',
    INDEX idx_username (username),
    INDEX idx_student_id (student_id),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 标签表 (tags)
-- ============================================
DROP TABLE IF EXISTS tags;
CREATE TABLE tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签 ID',
    code VARCHAR(50) UNIQUE NOT NULL COMMENT '标签代码',
    name VARCHAR(50) NOT NULL COMMENT '标签名称',
    level TINYINT NOT NULL COMMENT '标签级别 1-4',
    parent_id BIGINT DEFAULT 0 COMMENT '父标签 ID',
    color VARCHAR(20) COMMENT '标签颜色',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    is_hot BOOLEAN DEFAULT FALSE COMMENT '是否热门',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_code (code),
    INDEX idx_level (level),
    INDEX idx_parent_id (parent_id),
    INDEX idx_hot (is_hot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- ============================================
-- 3. 话题表 (topics)
-- ============================================
DROP TABLE IF EXISTS topics;
CREATE TABLE topics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '话题 ID',
    user_id BIGINT NOT NULL COMMENT '发布者 ID',
    content TEXT NOT NULL COMMENT '话题内容',
    title VARCHAR(200) COMMENT '话题标题',
    level1_tag VARCHAR(50) COMMENT '一级标签代码',
    is_top BOOLEAN DEFAULT FALSE COMMENT '是否置顶',
    is_essence BOOLEAN DEFAULT FALSE COMMENT '是否精华',
    status TINYINT DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    view_count INT DEFAULT 0 COMMENT '浏览数',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    comment_count INT DEFAULT 0 COMMENT '评论数',
    collection_count INT DEFAULT 0 COMMENT '收藏数',
    share_count INT DEFAULT 0 COMMENT '分享数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_level1_tag (level1_tag),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_is_top (is_top),
    INDEX idx_is_essence (is_essence)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题表';

-- ============================================
-- 4. 话题图片表 (topic_images)
-- ============================================
DROP TABLE IF EXISTS topic_images;
CREATE TABLE topic_images (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片 ID',
    topic_id BIGINT NOT NULL COMMENT '话题 ID',
    image_url VARCHAR(255) NOT NULL COMMENT '图片 URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_topic_id (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题图片表';

-- ============================================
-- 5. 话题标签关联表 (topic_tags)
-- ============================================
DROP TABLE IF EXISTS topic_tags;
CREATE TABLE topic_tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联 ID',
    topic_id BIGINT NOT NULL COMMENT '话题 ID',
    tag_id BIGINT NOT NULL COMMENT '标签 ID',
    tag_level TINYINT NOT NULL COMMENT '标签级别',
    tag_code VARCHAR(50) NOT NULL COMMENT '标签代码',
    tag_name VARCHAR(50) NOT NULL COMMENT '标签名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_topic_tag (topic_id, tag_id),
    INDEX idx_topic_id (topic_id),
    INDEX idx_tag_id (tag_id),
    INDEX idx_tag_level (tag_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题标签关联表';

-- ============================================
-- 6. 评论表 (topic_comments)
-- ============================================
DROP TABLE IF EXISTS topic_comments;
CREATE TABLE topic_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论 ID',
    topic_id BIGINT NOT NULL COMMENT '话题 ID',
    user_id BIGINT NOT NULL COMMENT '评论者 ID',
    content TEXT NOT NULL COMMENT '评论内容',
    parent_id BIGINT DEFAULT 0 COMMENT '父评论 ID',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    reply_count INT DEFAULT 0 COMMENT '回复数',
    status TINYINT DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    is_hot BOOLEAN DEFAULT FALSE COMMENT '是否热评',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_topic_id (topic_id),
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_is_hot (is_hot),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- ============================================
-- 7. 点赞表 (likes)
-- ============================================
DROP TABLE IF EXISTS likes;
CREATE TABLE likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '点赞 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    target_type VARCHAR(20) NOT NULL COMMENT '目标类型 topic/comment',
    target_id BIGINT NOT NULL COMMENT '目标 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_target (user_id, target_type, target_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- ============================================
-- 8. 收藏表 (collections)
-- ============================================
DROP TABLE IF EXISTS collections;
CREATE TABLE collections (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    topic_id BIGINT NOT NULL COMMENT '话题 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_topic (user_id, topic_id),
    INDEX idx_user_id (user_id),
    INDEX idx_topic_id (topic_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- ============================================
-- 9. 用户认证表 (user_verifications)
-- ============================================
DROP TABLE IF EXISTS user_verifications;
CREATE TABLE user_verifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '认证 ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户 ID',
    identity_type VARCHAR(50) NOT NULL COMMENT '认证类型 student/organization',
    verified BOOLEAN DEFAULT FALSE COMMENT '是否已认证',
    verified_at DATETIME COMMENT '认证时间',
    audit_status TINYINT DEFAULT 0 COMMENT '审核状态 0-待审核 1-通过 2-拒绝',
    audit_remark VARCHAR(200) COMMENT '审核备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_identity_type (identity_type),
    INDEX idx_audit_status (audit_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户认证表';

-- ============================================
-- 10. 关注表 (follows)
-- ============================================
DROP TABLE IF EXISTS follows;
CREATE TABLE follows (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关注 ID',
    follower_id BIGINT NOT NULL COMMENT '关注者 ID',
    followee_id BIGINT NOT NULL COMMENT '被关注者 ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_follower_followee (follower_id, followee_id),
    INDEX idx_follower_id (follower_id),
    INDEX idx_followee_id (followee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注表';

-- ============================================
-- 11. 隐私设置表 (user_privacy_settings)
-- ============================================
DROP TABLE IF EXISTS user_privacy_settings;
CREATE TABLE user_privacy_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置 ID',
    user_id BIGINT UNIQUE NOT NULL COMMENT '用户 ID',
    topics_public_visible BOOLEAN DEFAULT TRUE COMMENT '话题对外可见',
    participated_topics_visible BOOLEAN DEFAULT TRUE COMMENT '参与话题可见',
    show_student_id BOOLEAN DEFAULT TRUE COMMENT '显示学号',
    show_college BOOLEAN DEFAULT TRUE COMMENT '显示学院',
    show_major BOOLEAN DEFAULT TRUE COMMENT '显示专业',
    allow_messages BOOLEAN DEFAULT TRUE COMMENT '允许私信',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户隐私设置表';

-- ============================================
-- 12. 登录设备表 (login_devices)
-- ============================================
DROP TABLE IF EXISTS login_devices;
CREATE TABLE login_devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备 ID',
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    device_type VARCHAR(50) COMMENT '设备类型 mobile/desktop/tablet',
    ip_address VARCHAR(50) COMMENT 'IP 地址',
    login_location VARCHAR(100) COMMENT '登录地点',
    last_active_at DATETIME COMMENT '最后活跃时间',
    is_current BOOLEAN DEFAULT FALSE COMMENT '是否当前设备',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_is_current (is_current)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录设备表';

-- ============================================
-- 初始化基础数据
-- ============================================

-- 插入一级标签（身份标签）
INSERT INTO tags (code, name, level, icon, sort_order) VALUES
('student', '学生', 1, '👨‍🎓', 1),
('organization', '团体', 1, '👥', 2),
('followed', '关注', 1, '⭐', 3),
('society', '社会', 1, '🌍', 4);

-- 插入二级标签（话题分类）
INSERT INTO tags (code, name, level, color, sort_order) VALUES
('study_experience', '学习经验', 2, '#50C878', 1),
('tech_exchange', '技术交流', 2, '#4169E1', 2),
('campus_life', '校园生活', 2, '#FF6B6B', 3),
('food_recommend', '美食推荐', 2, '#FFA07A', 4),
('activity', '社团活动', 2, '#DDA0DD', 5),
('employment', '就业信息', 2, '#98FB98', 6),
('help', '求助帖', 2, '#F0E68C', 7),
('confession', '表白墙', 2, '#FFB6C1', 8);

-- 插入三级标签（地点标签）
INSERT INTO tags (code, name, level, sort_order) VALUES
('library', '图书馆', 3, 1),
('cafeteria', '食堂', 3, 2),
('dormitory', '宿舍', 3, 3),
('playground', '操场', 3, 4),
('teaching_building', '教学楼', 3, 5),
('laboratory', '实验室', 3, 6);

-- ============================================
-- 示例数据（可选）
-- ============================================

-- 插入一个测试用户（密码为 123456 的 BCrypt 加密）
-- 注意：实际使用时应该使用真实的加密密码
INSERT INTO users (username, password, real_name, student_id, email, college, major, grade) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lqkkO9QS3TzCjH3rS', '张三', '2021001', 'test@example.com', '计算机学院', '计算机科学与技术', '2021 级');

-- 插入测试用户的隐私设置
INSERT INTO user_privacy_settings (user_id, topics_public_visible, participated_topics_visible) VALUES
(1, TRUE, TRUE);

-- 插入测试话题
INSERT INTO topics (user_id, content, level1_tag, view_count, like_count, comment_count) VALUES
(1, '分享我的学习经验：如何高效准备期末考试...\n\n1. 制定复习计划\n2. 重点突破难点\n3. 多做练习题\n\n希望对大家有所帮助！', 'student', 235, 56, 12);

COMMIT;
