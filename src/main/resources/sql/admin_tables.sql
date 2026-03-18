-- 管理员系统数据库表

-- 1. 管理员表
CREATE TABLE IF NOT EXISTS `admin_user` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员 ID',
    `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    `real_name` VARCHAR(50) COMMENT '真实姓名',
    `avatar` VARCHAR(255) COMMENT '头像 URL',
    `role_id` INT DEFAULT 1 COMMENT '角色 ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `last_login_time` DATETIME COMMENT '最后登录时间',
    `last_login_ip` VARCHAR(50) COMMENT '最后登录 IP',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_username` (`username`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 2. 管理员角色表
CREATE TABLE IF NOT EXISTS `admin_role` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色 ID',
    `role_name` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) UNIQUE NOT NULL COMMENT '角色代码',
    `permissions` TEXT COMMENT '权限列表（JSON 格式）',
    `description` VARCHAR(200) COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员角色表';

-- 3. 管理员操作日志表
CREATE TABLE IF NOT EXISTS `admin_operation_log` (
    `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志 ID',
    `admin_id` INT NOT NULL COMMENT '管理员 ID',
    `admin_name` VARCHAR(50) COMMENT '管理员姓名',
    `operation_type` VARCHAR(50) COMMENT '操作类型',
    `operation_module` VARCHAR(50) COMMENT '操作模块',
    `operation_target` VARCHAR(100) COMMENT '操作目标',
    `operation_detail` TEXT COMMENT '操作详情',
    `request_method` VARCHAR(10) COMMENT '请求方法',
    `request_url` VARCHAR(255) COMMENT '请求 URL',
    `ip_address` VARCHAR(50) COMMENT 'IP 地址',
    `user_agent` VARCHAR(500) COMMENT '用户代理',
    `execution_time` INT COMMENT '执行时间（毫秒）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-失败，1-成功',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_admin_id` (`admin_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 4. 插入默认角色数据
INSERT INTO `admin_role` (`role_name`, `role_code`, `permissions`, `description`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '["*"]', '拥有所有权限', 1),
('系统管理员', 'ADMIN', '["user:*", "content:*", "report:*"]', '拥有大部分管理权限', 1),
('内容审核员', 'CONTENT_MODERATOR', '["content:view", "content:audit", "report:view", "report:handle"]', '负责内容审核', 1);

-- 5. 插入默认管理员账号（密码：admin123，已加密）
INSERT INTO `admin_user` (`username`, `password`, `real_name`, `role_id`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJfYR5sYpF5qE9qQhN7sP8xGK2C', '系统管理员', 1, 1);

-- 说明：
-- 默认账号：admin
-- 默认密码：admin123
-- 密码使用 BCrypt 加密
