-- 举报表结构最终修复
-- 执行前请先检查：DESC reports;

-- 1. 备份数据
CREATE TABLE IF NOT EXISTS `reports_backup_final` AS SELECT * FROM `reports`;

-- 2. 修改必填字段
ALTER TABLE `reports` MODIFY COLUMN `reason` TEXT COMMENT '举报原因描述';
ALTER TABLE `reports` MODIFY COLUMN `report_type` VARCHAR(50) NOT NULL COMMENT '举报类型：pornography-色情低俗、illegal-违法犯罪、political-政治敏感、spam-垃圾广告、fake_info-虚假信息、copyright-侵权内容、harassment-人身攻击、other-其他';
ALTER TABLE `reports` MODIFY COLUMN `target_type` VARCHAR(20) NOT NULL COMMENT '被举报类型：topic-话题、comment-评论、product-商品、location-地点、user-用户';

-- 3. 验证表结构
SELECT '修复完成，表结构如下：' AS message;
