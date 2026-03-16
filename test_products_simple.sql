USE campus_db;

-- 插入 11 个分类的商品（每个分类 1-3 个）
INSERT INTO products (seller_id, title, description, price, original_price, category_id, images, contact_info, trade_methods, location, status, view_count, like_count, favorite_count, is_hot) VALUES
(1, 'MacBook Pro 2020', 'i5 处理器 16G 内存 512G SSD 9 成新', 4500.00, 8000.00, 1, '["https://placehold.co/300x200/4A90E2/FFFFFF?text=MacBook"]', '微信 zhang123', '["face-to-face", "delivery"]', '校内', 1, 235, 18, 5, 1),
(2, 'iPhone 12 二手', '国行在保 128G 黑色 9 成新', 2800.00, 4500.00, 1, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=iPhone"]', 'QQ 2345678', '["face-to-face"]', '校内', 1, 189, 15, 3, 1),
(4, '电脑清灰换硅脂', '专业工具细心操作', 30.00, NULL, 2, '["https://placehold.co/300x200/9B59B6/FFFFFF?text=PC"]', '微信 fixpc', '["face-to-face"]', '计算机楼', 1, 145, 11, 6, 1),
(6, '周末家教兼职', '初中数理化辅导有经验', 100.00, NULL, 3, '["https://placehold.co/300x200/FF9500/FFFFFF?text=Tutor"]', '电话 13700137000', '["face-to-face"]', '校外', 1, 423, 35, 10, 1),
(9, '拼车去火车站', '周五下午出发 2 个空位', 30.00, NULL, 4, '["https://placehold.co/300x200/50C878/FFFFFF?text=Car"]', '微信 pinche', '["face-to-face"]', '校门口', 1, 201, 16, 4, 1),
(10, '自制手工饼干', '纯手工制作无添加', 25.00, NULL, 5, '["https://placehold.co/300x200/FFB6C1/FFFFFF?text=Cookies"]', '微信 cookie', '["face-to-face", "campus-delivery"]', '食堂', 1, 289, 22, 7, 1),
(1, '高等数学教材', '同济第七版有笔记', 25.00, 60.00, 6, '["https://placehold.co/300x200/50C878/FFFFFF?text=Math"]', '电话 13800138000', '["face-to-face"]', '图书馆', 1, 156, 8, 4, 1),
(4, '罗技 MX Master3', '用了半年功能正常', 350.00, 699.00, 7, '["https://placehold.co/300x200/9B59B6/FFFFFF?text=Mouse"]', '微信 wang456', '["face-to-face"]', '校内', 1, 145, 12, 2, 1),
(7, '小米台灯', '全新未拆封智能控制', 45.00, 79.00, 8, '["https://placehold.co/300x200/FFA500/FFFFFF?text=Lamp"]', 'QQ 3456789', '["face-to-face"]', '宿舍区', 1, 98, 5, 2, 0),
(10, '代取快递', '快速便捷送货上门', 5.00, NULL, 9, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=Express"]', '电话 13900139000', '["campus-delivery"]', '全校', 1, 312, 25, 8, 1),
(1, '校园跑腿', '代买饭代排队', 10.00, NULL, 10, '["https://placehold.co/300x200/4A90E2/FFFFFF?text=Run"]', '微信 paotui', '["face-to-face"]', '全校', 1, 267, 20, 6, 1),
(3, '多肉植物盆栽', '可爱多肉带盆', 15.00, NULL, 11, '["https://placehold.co/300x200/50C878/FFFFFF?text=Plant"]', '微信 plant', '["face-to-face"]', '花坛边', 1, 123, 10, 3, 1);

-- 已售出和下架商品
INSERT INTO products (seller_id, title, description, price, original_price, category_id, images, contact_info, trade_methods, location, status, view_count, like_count, favorite_count, is_hot) VALUES
(5, 'Switch 游戏机已售出', '国行续航版送游戏', 1800.00, 2500.00, 7, '["https://placehold.co/300x200/E6E6FA/FFFFFF?text=Switch"]', '微信 game', '["face-to-face"]', '校内', 2, 567, 45, 12, 1),
(6, '考研资料已下架', '版权原因下架', 50.00, 100.00, 6, '["https://placehold.co/300x200/CCCCCC/FFFFFF?text=Book"]', 'QQ 7890123', '["face-to-face"]', '教学楼', 0, 123, 10, 2, 0);

-- 收藏数据（使用实际的商品 ID: 187-200）
-- 使用 INSERT IGNORE 避免重复插入错误
INSERT IGNORE INTO product_favorites (user_id, product_id) VALUES
(1, 187), (1, 189), (1, 192), (2, 188), (2, 196), (3, 187), (3, 190), (4, 191), (4, 195), (5, 192), (5, 196), (6, 187), (7, 189), (8, 191), (9, 193), (10, 195), (11, 197);

-- 浏览记录（使用实际的商品 ID）
INSERT INTO product_views (user_id, product_id, device_type) VALUES
(1, 187, 'desktop'), (1, 188, 'mobile'), (2, 189, 'desktop'), (2, 190, 'mobile'), (3, 191, 'desktop'), (3, 192, 'mobile'), (4, 193, 'desktop'), (4, 194, 'mobile'), (5, 195, 'desktop'), (5, 196, 'mobile');

-- 更新统计数据
UPDATE products SET 
    favorite_count = (SELECT COUNT(*) FROM product_favorites WHERE product_id = products.id),
    view_count = view_count + (SELECT COUNT(*) FROM product_views WHERE product_id = products.id);
