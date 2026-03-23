-- User Location Mark Table
USE campus_db;

DROP TABLE IF EXISTS `user_location_mark`;

CREATE TABLE `user_location_mark` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `campus_id` INT NOT NULL,
  `location_name` VARCHAR(100) NOT NULL,
  `latitude` DECIMAL(10,8) NOT NULL,
  `longitude` DECIMAL(11,8) NOT NULL,
  `address_detail` VARCHAR(255),
  `mark_type` VARCHAR(50) NOT NULL,
  `mark_category` VARCHAR(50),
  `user_identity_type` VARCHAR(50),
  `related_entity_id` BIGINT,
  `description` TEXT,
  `contact_info` VARCHAR(100),
  `images` JSON,
  `start_time` DATETIME,
  `end_time` DATETIME,
  `is_permanent` TINYINT(1) DEFAULT 0,
  `verification_status` VARCHAR(20) DEFAULT 'pending',
  `verified_at` DATETIME,
  `reviewer_id` BIGINT,
  `review_comment` TEXT,
  `visibility` VARCHAR(20) DEFAULT 'public',
  `view_count` INT DEFAULT 0,
  `like_count` INT DEFAULT 0,
  `is_active` TINYINT(1) DEFAULT 1,
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `expires_at` DATETIME,
  
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_campus_id` (`campus_id`),
  INDEX `idx_mark_type` (`mark_type`),
  INDEX `idx_verification_status` (`verification_status`),
  INDEX `idx_visibility` (`visibility`),
  INDEX `idx_location` (`latitude`, `longitude`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert test data
INSERT INTO `user_location_mark` (
  user_id, campus_id, location_name, latitude, longitude, 
  mark_type, mark_category, user_identity_type, 
  description, verification_status, visibility, is_permanent
) VALUES (
  2, 1, 'Library Meeting Point', 35.308123, 113.926234,
  'meeting_point', 'building', 'student',
  'Common meeting spot at library entrance', 'approved', 'public', 0
);

INSERT INTO `user_location_mark` (
  user_id, campus_id, location_name, latitude, longitude,
  mark_type, mark_category, user_identity_type,
  description, contact_info, verification_status, visibility, is_permanent
) VALUES (
  3, 1, 'Campus Print Shop', 35.307456, 113.927123,
  'merchant_shop', 'facility', 'merchant',
  'Printing and copying services', '13800138000', 'approved', 'public', 1
);

INSERT INTO `user_location_mark` (
  user_id, campus_id, location_name, latitude, longitude,
  mark_type, mark_category, user_identity_type,
  description, start_time, end_time, verification_status, visibility
) VALUES (
  4, 1, 'Club Recruitment Spot', 35.306789, 113.928234,
  'organization_activity', 'area', 'organization',
  'Semester club recruitment event', 
  '2026-03-25 09:00:00', '2026-03-25 17:00:00',
  'pending', 'public'
);

INSERT INTO `user_location_mark` (
  user_id, campus_id, location_name, latitude, longitude,
  mark_type, mark_category, user_identity_type,
  description, verification_status, visibility
) VALUES (
  2, 1, 'Lovers Lake Bench', 35.306500, 113.931000,
  'meeting_point', 'area', 'student',
  'Quiet chatting spot', 'approved', 'friends'
);
