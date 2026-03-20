-- 商品转发功能测试数据
-- 用于测试 ForwardedProductCard 组件和商品分享流程

-- ========================================
-- 1. 确认数据库字段已添加
-- ========================================
SHOW COLUMNS FROM topics LIKE 'forwarded_from_product_id';

-- ========================================
-- 2. 插入测试用的商品（如果没有）
-- ========================================
INSERT INTO products (seller_id, title, description, price, original_price, category_id, images, contact_info, trade_methods, location, status, view_count, like_count, favorite_count, is_hot, created_at, updated_at)
SELECT 1, '测试商品 - 用于转发', '这是一个用于测试转发功能的商品', 99.00, 199.00, 1, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=Test"]', '微信：test123', '["face-to-face"]', '校内', 1, 0, 0, 0, 0, NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM products WHERE title = '测试商品 - 用于转发'
);

-- ========================================
-- 3. 插入测试用的转发话题（分享商品到话题墙）
-- ========================================
-- 获取刚插入的商品 ID
SET @test_product_id = (SELECT id FROM products WHERE title = '测试商品 - 用于转发' LIMIT 1);

-- 插入转发话题
INSERT INTO topics (
    user_id, 
    content, 
    images, 
    tags,
    level1_tag_code,
    level2_tag_codes,
    level3_tag_codes,
    level4_tag_codes,
    is_forwarded,
    forwarded_from_topic_id,
    forwarded_from_product_id,
    likes_count,
    comments_count,
    views_count,
    collections_count,
    is_essence,
    is_top,
    status,
    created_at,
    updated_at
) VALUES (
    1,  -- 用户 ID
    '给大家推荐一个好用的商品！大家快来看看～',  -- 分享内容
    '[]',  -- 图片
    '["分享", "推荐"]',  -- 标签
    'student',  -- 一级标签：学生
    '["study_experience"]',  -- 二级标签
    '[]',  -- 三级标签
    '[{"name": "分享"}]',  -- 四级标签
    1,  -- is_forwarded = true (表示是转发的)
    NULL,  -- forwarded_from_topic_id = NULL (不是转发话题)
    @test_product_id,  -- forwarded_from_product_id = 商品 ID
    0,
    0,
    0,
    0,
    0,
    0,
    1,
    NOW(),
    NOW()
);

-- ========================================
-- 4. 验证数据是否插入成功
-- ========================================
-- 查看转发话题
SELECT 
    t.id, 
    t.content, 
    t.is_forwarded,
    t.forwarded_from_topic_id,
    t.forwarded_from_product_id,
    p.title as product_title,
    p.price as product_price,
    t.created_at
FROM topics t
LEFT JOIN products p ON t.forwarded_from_product_id = p.id
WHERE t.forwarded_from_product_id IS NOT NULL
ORDER BY t.created_at DESC;

-- ========================================
-- 5. 测试查询
-- ========================================
-- 测试 1：查询所有分享商品的话题
SELECT '测试 1：分享商品的话题' as test_name;
SELECT COUNT(*) as count FROM topics WHERE forwarded_from_product_id IS NOT NULL;

-- 测试 2：查询特定商品的分享话题
SELECT '测试 2：特定商品的分享话题' as test_name;
SELECT t.*, p.title as product_title 
FROM topics t
JOIN products p ON t.forwarded_from_product_id = p.id
WHERE p.title LIKE '%测试商品%';

-- ========================================
-- 6. 清理测试数据（可选）
-- ========================================
-- 如果需要清理，取消以下注释：
-- DELETE FROM topics WHERE content = '给大家推荐一个好用的商品！大家快来看看～';
-- DELETE FROM products WHERE title = '测试商品 - 用于转发';

SELECT '✅ 商品转发功能测试数据准备完成！' AS message;
SELECT '📝 请按以下步骤测试：' AS step;
SELECT '1. 访问话题墙 (/topicwall)，查看是否有分享商品的话题卡片' AS step1;
SELECT '2. 点击商品卡片，应该跳转到商品详情页 (/product/{id})' AS step2;
SELECT '3. 在商品详情页点击分享按钮，选择转发到话题墙' AS step3;
SELECT '4. 发布后应该能在话题墙看到带有商品嵌套卡片的动态' AS step4;
