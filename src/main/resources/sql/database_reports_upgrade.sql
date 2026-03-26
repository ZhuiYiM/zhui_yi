-- 举报功能升级：支持多种举报类型
-- 执行时间：2026-03-27

-- 1. 修改 target_type 字段注释（已在代码中更新，数据库中如果是新表会自动应用）
-- 如果是旧表需要升级，执行以下 SQL：
ALTER TABLE `reports` 
MODIFY COLUMN `target_type` VARCHAR(20) NOT NULL COMMENT '被举报类型：topic-话题、comment-评论、product-商品、location-地点';

-- 2. 修改 report_type 字段注释
ALTER TABLE `reports` 
MODIFY COLUMN `report_type` VARCHAR(50) NOT NULL COMMENT '举报类型：pornography-色情低俗、illegal-违法犯罪、political-政治敏感、spam-垃圾广告、fake_info-虚假信息、copyright-侵权内容、harassment-人身攻击、other-其他';

-- 3. 修改 evidence 字段注释
ALTER TABLE `reports` 
MODIFY COLUMN `evidence` TEXT COMMENT '证据（包含图片和其他证据）';

-- 说明：
-- - 支持举报类型：话题、评论、商品、地点
-- - 举报原因类型：色情低俗、违法犯罪、政治敏感、垃圾广告、虚假信息、侵权内容、人身攻击、其他
-- - 前端使用统一的 ReportModal 组件进行举报
