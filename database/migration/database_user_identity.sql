-- ============================================
-- 用户身份认证系统数据库表结构
-- 创建时间：2026-03-25
-- ============================================

-- 使用数据库
USE campus_db;

-- --------------------------------------------
-- 1. 用户身份表 (user_identity)
-- --------------------------------------------
CREATE TABLE IF NOT EXISTS `user_identity` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `identity_type` VARCHAR(50) NOT NULL,
  `identity_name` VARCHAR(100) NOT NULL,
  `verified` TINYINT(1) DEFAULT 0,
  `verified_at` DATETIME DEFAULT NULL,
  `expires_at` DATETIME DEFAULT NULL,
  `extra_info` JSON DEFAULT NULL,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_identity_type` (`user_id`, `identity_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_identity_type` (`identity_type`),
  KEY `idx_verified` (`verified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------
-- 2. 在 users 表中添加 is_staff 字段
-- --------------------------------------------
-- 检查字段是否存在，不存在则添加
SET @dbname = DATABASE();
SET @tablename = 'users';
SET @columnname = 'is_staff';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  "SELECT 1",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname, " TINYINT(1) DEFAULT 0")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- --------------------------------------------
-- 3. 更新 admin_role 表
-- --------------------------------------------
INSERT INTO `admin_role` (`role_name`, `role_code`, `permissions`, `description`, `status`)
VALUES 
  ('超级管理员', 'SUPER_ADMIN', '["*"]', '拥有所有权限', 1),
  ('内容管理员', 'CONTENT_MODERATOR', '["content:*", "user:read", "verification:*"]', '负责内容和认证审核', 1)
ON DUPLICATE KEY UPDATE 
  `role_name` = VALUES(`role_name`),
  `permissions` = VALUES(`permissions`),
  `description` = VALUES(`description`);

-- --------------------------------------------
-- 4. 插入测试数据
-- --------------------------------------------
DELETE FROM user_identity WHERE user_id IN (1, 2, 3, 4, 5);

INSERT INTO `user_identity` (`user_id`, `identity_type`, `identity_name`, `verified`, `verified_at`, `extra_info`)
VALUES
  (1, 'student', '学生', 1, NOW(), '{"studentId": "2024001", "college": "计算机学院"}'),
  (2, 'merchant', '商户', 1, NOW(), '{"shopName": "校园小卖部", "businessLicenseUrl": "/uploads/test_license.jpg"}'),
  (3, 'staff', '教职工', 1, NOW(), '{"staffId": "JS001", "department": "教务处"}'),
  (4, 'organization', '团体/部门', 1, NOW(), '{"organizationName": "学生会", "leaderName": "李明"}'),
  (5, 'merchant', '商户', 0, NULL, '{"shopName": "新店铺", "businessLicenseUrl": "/uploads/pending_license.jpg"}');

-- --------------------------------------------
-- 5. 创建视图
-- --------------------------------------------
DROP VIEW IF EXISTS `v_user_identity_summary`;
CREATE VIEW `v_user_identity_summary` AS
SELECT 
  u.id AS user_id,
  u.username,
  u.email,
  u.role,
  u.is_admin,
  u.is_merchant,
  u.is_organization,
  u.is_staff,
  ui.identity_type,
  ui.identity_name,
  ui.verified,
  ui.verified_at,
  ui.extra_info,
  ui.created_at AS identity_created_at
FROM users u
LEFT JOIN user_identity ui ON u.id = ui.user_id
ORDER BY u.id, ui.verified DESC, ui.created_at DESC;

-- --------------------------------------------
-- 6. 创建存储过程
-- --------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS `sp_cleanup_expired_identities`$$
CREATE PROCEDURE `sp_cleanup_expired_identities`()
BEGIN
  UPDATE user_identity 
  SET verified = 0,
      updated_at = NOW()
  WHERE expires_at IS NOT NULL 
    AND expires_at < NOW()
    AND verified = 1;
    
  SELECT ROW_COUNT() AS cleaned_count;
END$$

DELIMITER ;

-- --------------------------------------------
-- 7. 创建触发器
-- --------------------------------------------
DROP TRIGGER IF EXISTS `trg_after_user_delete`;
DELIMITER $$
CREATE TRIGGER `trg_after_user_delete` 
AFTER DELETE ON `users`
FOR EACH ROW
BEGIN
  DELETE FROM user_identity WHERE user_id = OLD.id;
END$$
DELIMITER ;

-- --------------------------------------------
-- 8. 验证
-- --------------------------------------------
SELECT 'Table created successfully' AS status;
SELECT 'Test data inserted' AS status;
SELECT 'View created' AS status;
SELECT 'Procedure created' AS status;
SELECT 'Trigger created' AS status;

SELECT * FROM v_user_identity_summary LIMIT 10;
