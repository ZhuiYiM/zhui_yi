-- 商品分享标签更新脚本
-- 用于将已有的商品分享话题从"话题转发"标签更新为"商品分享"标签

-- ========================================
-- 1. 查看当前商品分享话题的标签情况
-- ========================================
SELECT 
    id,
    content,
    topic_tag_codes,
    forwarded_from_product_id,
    created_at
FROM topics
WHERE forwarded_from_product_id IS NOT NULL
ORDER BY created_at DESC;

-- ========================================
-- 2. 更新商品分享话题的标签（仅当确实存在时）
-- ========================================
-- 注意：这个更新会将 topic_tag_codes 中的 "topic_forward" 替换为 "product_share"
-- 如果已经是 "product_share"，则不会重复更新

-- 方式 1: 直接替换整个 JSON 数组（简单但会丢失其他标签）
-- UPDATE topics 
-- SET topic_tag_codes = '["product_share"]'
-- WHERE forwarded_from_product_id IS NOT NULL 
--   AND (topic_tag_codes IS NULL OR topic_tag_codes = '["topic_forward"]');

-- 方式 2: 保留原有标签，只替换标签代码（推荐）
UPDATE topics 
SET topic_tag_codes = REPLACE(topic_tag_codes, 'topic_forward', 'product_share')
WHERE forwarded_from_product_id IS NOT NULL 
  AND topic_tag_codes LIKE '%topic_forward%';

-- ========================================
-- 3. 验证更新结果
-- ========================================
SELECT 
    id,
    content,
    topic_tag_codes,
    forwarded_from_product_id,
    CASE 
        WHEN topic_tag_codes LIKE '%product_share%' THEN '✅ 已更新为商品分享'
        WHEN topic_tag_codes LIKE '%topic_forward%' THEN '⚠️ 仍为话题转发'
        ELSE '❓ 未知标签'
    END AS tag_status,
    created_at
FROM topics
WHERE forwarded_from_product_id IS NOT NULL
ORDER BY created_at DESC;

-- ========================================
-- 4. 统计信息
-- ========================================
SELECT 
    COUNT(*) AS total_product_shares,
    SUM(CASE WHEN topic_tag_codes LIKE '%product_share%' THEN 1 ELSE 0 END) AS with_product_share_tag,
    SUM(CASE WHEN topic_tag_codes LIKE '%topic_forward%' THEN 1 ELSE 0 END) AS with_topic_forward_tag,
    SUM(CASE WHEN topic_tag_codes NOT LIKE '%product_share%' AND topic_tag_codes NOT LIKE '%topic_forward%' THEN 1 ELSE 0 END) AS with_other_tags
FROM topics
WHERE forwarded_from_product_id IS NOT NULL;

-- ========================================
-- 5. 检查标签体系是否存在对应标签
-- ========================================
-- 检查 topic_tag 表中是否有 product_share 标签
SELECT * FROM topic_tag WHERE code = 'product_share';

-- 检查 topic_tag 表中是否有 topic_forward 标签
SELECT * FROM topic_tag WHERE code = 'topic_forward';

-- 如果没有，可以手动添加（可选）
-- INSERT INTO topic_tag (code, name, description, sort_order, is_active, created_at, updated_at)
-- VALUES 
--     ('product_share', '商品分享', '用户分享的商品', 1, 1, NOW(), NOW()),
--     ('topic_forward', '话题转发', '用户转发的话题', 2, 1, NOW(), NOW());

-- ========================================
-- 6. 回滚脚本（如果需要恢复）
-- ========================================
-- UPDATE topics 
-- SET topic_tag_codes = REPLACE(topic_tag_codes, 'product_share', 'topic_forward')
-- WHERE forwarded_from_product_id IS NOT NULL 
--   AND topic_tag_codes LIKE '%product_share%';
