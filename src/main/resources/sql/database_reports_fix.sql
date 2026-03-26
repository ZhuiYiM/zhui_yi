-- 举报功能数据库修复：统一字段结构
-- 执行时间：2026-03-27
-- 问题：数据库表同时存在 type 和 target_type 字段，导致插入失败
-- 解决方案：将 type 字段数据迁移到 target_type，然后删除 type 字段

-- 1. 备份数据（重要！）
CREATE TABLE IF NOT EXISTS `reports_backup` AS SELECT * FROM `reports`;

-- 2. 修改字段结构
-- 将 reason 改为 TEXT 类型以支持更长描述
ALTER TABLE `reports` MODIFY COLUMN `reason` TEXT COMMENT '举报原因描述';

-- 将 report_type 扩展为 VARCHAR(50) 以支持更多举报类型
ALTER TABLE `reports` MODIFY COLUMN `report_type` VARCHAR(50) NOT NULL COMMENT '举报类型：pornography-色情低俗、illegal-违法犯罪、political-政治敏感、spam-垃圾广告、fake_info-虚假信息、copyright-侵权内容、harassment-人身攻击、other-其他';

-- 3. 数据迁移：将 type 字段的数据迁移到 target_type
UPDATE `reports` SET `target_type` = `type` WHERE `target_type` IS NULL AND `type` IS NOT NULL;

-- 4. 删除旧的 type 字段（先确保 target_type 已经有数据）
ALTER TABLE `reports` DROP COLUMN `type`;

-- 5. 删除冗余字段（MySQL 不支持 IF EXISTS，直接删除）
-- 注意：如果字段不存在会报错，请根据实际表结构调整
ALTER TABLE `reports` DROP COLUMN `updated_at`;
ALTER TABLE `reports` DROP COLUMN `process_note`;

-- 6. 确保 target_type 为 NOT NULL
ALTER TABLE `reports` MODIFY COLUMN `target_type` VARCHAR(20) NOT NULL COMMENT '被举报类型：topic-话题、comment-评论、product-商品、location-地点、user-用户';

-- 7. 添加索引（如果不存在）
ALTER TABLE `reports` ADD INDEX IF NOT EXISTS idx_target (`target_id`, `target_type`);
ALTER TABLE `reports` ADD INDEX IF NOT EXISTS idx_status (`status`);
ALTER TABLE `reports` ADD INDEX IF NOT EXISTS idx_reporter (`reporter_id`);

-- 8. 验证表结构
-- 执行后请检查：DESC reports;
-- 预期结果：应该只有 target_type，没有 type 字段
