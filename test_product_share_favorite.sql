-- 商品分享和收藏功能测试数据
-- 用于验证商品收藏和分享功能

-- ========================================
-- 1. 确认表结构
-- ========================================
SHOW COLUMNS FROM product_favorites;
SHOW COLUMNS FROM products LIKE 'favorite_count';

-- ========================================
-- 2. 插入测试收藏数据（如果还没有）
-- ========================================
INSERT IGNORE INTO product_favorites (user_id, product_id, created_at, updated_at) VALUES
(1, 1, NOW(), NOW()),
(1, 3, NOW(), NOW()),
(2, 2, NOW(), NOW()),
(2, 5, NOW(), NOW()),
(3, 1, NOW(), NOW()),
(3, 4, NOW(), NOW());

-- ========================================
-- 3. 更新商品的收藏数
-- ========================================
UPDATE products SET 
    favorite_count = (SELECT COUNT(*) FROM product_favorites WHERE product_id = products.id)
WHERE id > 0;

-- ========================================
-- 4. 验证收藏数据
-- ========================================
-- 查看用户 1 的收藏
SELECT p.id, p.title, p.price, p.status, pf.created_at as favorited_at
FROM product_favorites pf
JOIN products p ON pf.product_id = p.id
WHERE pf.user_id = 1
ORDER BY pf.created_at DESC;

-- 查看商品 1 的收藏者
SELECT u.id, u.username, u.student_id, pf.created_at
FROM product_favorites pf
JOIN users u ON pf.user_id = u.id
WHERE pf.product_id = 1
ORDER BY pf.created_at DESC;

-- ========================================
-- 5. 分享功能提示
-- ========================================
-- 分享功能不需要数据库支持，通过前端实现：
-- 1. 商品详情页点击"分享"按钮
-- 2. 选择"复制链接"或"转发"
-- 3. 转发会跳转到话题墙并携带商品信息
-- 4. 在话题墙发布时会自动添加"转发"标签

SELECT '商品收藏和分享功能测试数据准备完成！' AS message;

-- ========================================
-- 使用说明：
-- 1. 运行此脚本后，刷新个人中心页面
-- 2. 在"我的商品" -> "我收藏的"标签页查看收藏的商品
-- 3. 访问商品详情页 http://localhost:5173/product/1
-- 4. 点击"分享"按钮测试分享功能
-- 5. 选择"转发"可以跳转到话题墙发布新话题
-- ========================================
