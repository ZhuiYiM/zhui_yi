-- 举报表结构修复脚本
-- 执行时间：2026-03-27
-- 目标：统一数据库表结构与代码一致

-- 1. 备份数据
CREATE TABLE IF NOT EXISTS `reports_backup_20260327` AS SELECT * FROM `reports`;

-- 2. 修改字段类型
ALTER TABLE `reports` MODIFY COLUMN `reason` TEXT COMMENT '举报原因描述';
ALTER TABLE `reports` MODIFY COLUMN `report_type` VARCHAR(50) NOT NULL COMMENT '举报类型：pornography-色情低俗、illegal-违法犯罪、political-政治敏感、spam-垃圾广告、fake_info-虚假信息、copyright-侵权内容、harassment-人身攻击、other-其他';
ALTER TABLE `reports` MODIFY COLUMN `target_type` VARCHAR(20) NOT NULL COMMENT '被举报类型：topic-话题、comment-评论、product-商品、location-地点、user-用户';

-- 3. 删除冗余字段
ALTER TABLE `reports` DROP COLUMN `updated_at`;
ALTER TABLE `reports` DROP COLUMN `process_note`;

-- 4. 添加索引（如果不存在）
-- MySQL 中 ADD INDEX IF NOT EXISTS 需要 8.0.17+，使用普通 ADD INDEX
-- 如果索引已存在会报错，可以忽略
ALTER TABLE `reports` ADD INDEX `idx_target` (`target_id`, `target_type`);
ALTER TABLE `reports` ADD INDEX `idx_status` (`status`);
ALTER TABLE `reports` ADD INDEX `idx_reporter` (`reporter_id`);

-- 5. 验证
-- 执行后请检查：DESC reports;
