-- 为 topics 表添加 collections_count 字段
ALTER TABLE `topics` 
ADD COLUMN `collections_count` INT DEFAULT 0 COMMENT '收藏数量' AFTER `views_count`;

-- 初始化已有数据的收藏数为 0（可选）
UPDATE `topics` SET `collections_count` = 0 WHERE `collections_count` IS NULL;
