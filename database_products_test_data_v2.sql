-- ========================================
-- 校园信息平台 - 交易中心测试数据初始化
-- 创建时间：2026-03-13
-- 说明：为 products 表生成完整的测试数据
-- ========================================

USE campus_db;

-- ========================================
-- 1. 插入测试商品数据（覆盖所有 11 个分类）
-- ========================================

-- 分类 1: 二手物品
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(1, '二手 MacBook Pro，9 成新', '2020 款，i5 处理器，16G 内存，512G SSD，电池健康度 90%，无划痕，箱说全', 4500.00, 8000.00, 1, '["https://placehold.co/300x200/4A90E2/FFFFFF?text=MacBook"]', '微信：zhang123', '["face-to-face", "delivery"]', '校内', 1, 235, 18, 5, 1, NOW(), NOW()),
(2, 'iPhone 12 二手手机', '国行在保，128G 黑色，9 成新，轻微使用痕迹，功能完好', 2800.00, 4500.00, 1, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=iPhone"]', 'QQ: 2345678', '["face-to-face"]', '校内', 1, 189, 15, 3, 1, NOW(), NOW()),
(3, 'iPad Air 4 代', '64G WiFi 版，天蓝色，95 成新，带保护壳和类纸膜', 3200.00, 4800.00, 1, '["https://placehold.co/300x200/50C878/FFFFFF?text=iPad"]', '电话：13800138001', '["face-to-face", "campus-delivery"]', '图书馆', 1, 167, 12, 4, 1, NOW(), NOW());

-- 分类 2: 服务需求
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(4, '电脑清灰换硅脂', '专业工具，细心操作，让电脑重获新生，半小时取件', 30.00, NULL, 2, '["https://placehold.co/300x200/9B59B6/FFFFFF?text=电脑维护"]', '微信：fixComputer', '["face-to-face"]', '计算机学院楼', 1, 145, 11, 6, 1, NOW(), NOW()),
(5, 'PPT 制作美化', '经验丰富，审美在线，接急单，学生优惠', 50.00, NULL, 2, '["https://placehold.co/300x200/FFA500/FFFFFF?text=PPT 制作"]', 'QQ: 5678901', '["face-to-face", "delivery"]', '线上', 1, 198, 16, 7, 1, NOW(), NOW());

-- 分类 3: 兼职信息
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(6, '周末家教兼职', '初中数理化辅导，有经验，耐心负责，周末有时间', 100.00, NULL, 3, '["https://placehold.co/300x200/FF9500/FFFFFF?text=家教"]', '电话：13700137000', '["face-to-face"]', '校外', 1, 423, 35, 10, 1, NOW(), NOW()),
(7, '图书馆整理员', '学校图书馆招聘学生助理，工作时间灵活，环境好', 15.00, NULL, 3, '["https://placehold.co/300x200/4A90E2/FFFFFF?text=图书馆"]', '图书馆办公室', '["face-to-face"]', '图书馆', 1, 389, 28, 9, 1, NOW(), NOW()),
(8, '校园代理招募', '知名教育机构校园代理，时间自由，收入可观', 200.00, NULL, 3, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=校园代理"]', '微信：agent2026', '["face-to-face"]', '学生活动中心', 1, 267, 22, 8, 1, NOW(), NOW());

-- 分类 4: 拼车出行
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(9, '寻找拼车去火车站', '本周五下午出发，还有 2 个空位，后备箱空间大', 30.00, NULL, 4, '["https://placehold.co/300x200/50C878/FFFFFF?text=拼车"]', '微信：pinChe123', '["face-to-face"]', '校门口', 1, 201, 16, 4, 1, NOW(), NOW()),
(10, '清明节回家拼车', '4 月 3 日上午出发，目的地市区，可送到小区', 50.00, NULL, 4, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=清明拼车"]', '电话：13600136000', '["face-to-face"]', '校门口', 1, 167, 13, 3, 1, NOW(), NOW());

-- 分类 5: 美食外卖
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(10, '自制手工饼干', '纯手工制作，无添加，多种口味可选，送礼自用两相宜', 25.00, NULL, 5, '["https://placehold.co/300x200/FFB6C1/FFFFFF?text=饼干"]', '微信：cookieMaker', '["face-to-face", "campus-delivery"]', '食堂', 1, 289, 22, 7, 1, NOW(), NOW()),
(11, '家乡特产腊肠', '正宗四川风味，麻辣鲜香，真空包装，保证新鲜', 35.00, NULL, 5, '["https://placehold.co/300x200/DC143C/FFFFFF?text=腊肠"]', 'QQ: 4567890', '["face-to-face", "delivery"]', '宿舍区', 1, 234, 19, 5, 1, NOW(), NOW());

-- 分类 6: 书籍教材
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(1, '高等数学教材上下册', '同济第七版，几乎全新，有少量笔记，适合考研复习', 25.00, 60.00, 6, '["https://placehold.co/300x200/50C878/FFFFFF?text=数学教材"]', '电话：13800138000', '["face-to-face"]', '图书馆', 1, 156, 8, 4, 1, NOW(), NOW()),
(2, '考研英语词汇书', '新东方红宝书，9 成新，有少量划线', 15.00, 45.00, 6, '["https://placehold.co/300x200/FFA500/FFFFFF?text=词汇书"]', '微信：li789', '["face-to-face", "campus-delivery"]', '教学楼', 1, 98, 5, 1, 0, NOW(), NOW()),
(3, 'C++ Primer Plus', '第 6 版中文版，编程经典教材，附赠光盘', 40.00, 99.00, 6, '["https://placehold.co/300x200/4A90E2/FFFFFF?text=C++ 教材"]', 'QQ: 1234567', '["face-to-face"]', '计算机学院', 1, 112, 9, 2, 1, NOW(), NOW());

-- 分类 7: 电子设备
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(4, '罗技 MX Master 3 鼠标', '用了半年，功能正常，微磨损，手感依然很好', 350.00, 699.00, 7, '["https://placehold.co/300x200/9B59B6/FFFFFF?text=鼠标"]', '微信：wang456', '["face-to-face", "campus-delivery"]', '校内', 1, 145, 12, 2, 1, NOW(), NOW()),
(5, '机械键盘 Cherry 青轴', 'Cherry 原厂轴体，RGB 背光，9 成新', 280.00, 499.00, 7, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=键盘"]', '电话：13900139001', '["face-to-face"]', '宿舍区', 1, 178, 14, 3, 1, NOW(), NOW()),
(6, 'AirPods Pro 二代', '苹果原装，主动降噪，续航给力，箱说全', 1200.00, 1899.00, 7, '["https://placehold.co/300x200/FFFFFF/000000?text=AirPods"]', '微信：apple888', '["face-to-face", "delivery"]', '校内', 1, 312, 25, 8, 1, NOW(), NOW());

-- 分类 8: 生活用品
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(7, '小米台灯护眼灯', '买多了，全新未拆封，智能控制，三档调色', 45.00, 79.00, 8, '["https://placehold.co/300x200/FFA500/FFFFFF?text=台灯"]', 'QQ: 3456789', '["face-to-face", "campus-delivery"]', '宿舍区', 1, 98, 5, 2, 0, NOW(), NOW()),
(8, '床上四件套', '纯棉材质，几乎没用过，因毕业出售', 80.00, 200.00, 8, '["https://placehold.co/300x200/FFB6C1/FFFFFF?text=四件套"]', '微信：zhao123', '["face-to-face"]', '女生宿舍', 1, 67, 3, 1, 0, NOW(), NOW()),
(9, '收纳箱大号 3 个', '搬家清理，9 成新，结实耐用', 50.00, NULL, 8, '["https://placehold.co/300x200/50C878/FFFFFF?text=收纳箱"]', '电话：13800138002', '["face-to-face"]', '男生宿舍', 1, 89, 6, 2, 0, NOW(), NOW());

-- 分类 9: 代取快递
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(10, '代取快递，校内配送', '快速便捷，送货上门，大件小件都可接单', 5.00, NULL, 9, '["https://placehold.co/300x200/FF6B6B/FFFFFF?text=快递代取"]', '电话：13900139000', '["campus-delivery"]', '全校', 1, 312, 25, 8, 1, NOW(), NOW()),
(11, '快递代取，当天送达', '中午和下午各送一次，速度快，态度好', 3.00, NULL, 9, '["https://placehold.co/300x200/4A90E2/FFFFFF?text=快递"]', '微信：kuaidai001', '["campus-delivery"]', '全校', 1, 256, 20, 6, 1, NOW(), NOW());

-- 分类 10: 跑腿服务
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(1, '校园跑腿服务', '代买饭、代排队、代办事项，价格面议', 10.00, NULL, 10, '["https://placehold.co/300x200/4A90E2/FFFFFF?text=跑腿服务"]', '微信：paoTui888', '["face-to-face", "campus-delivery"]', '全校', 1, 267, 20, 6, 1, NOW(), NOW()),
(2, '代打印代复印', '图书馆打印机，价格便宜，送货上门', 0.50, NULL, 10, '["https://placehold.co/300x200/9B59B6/FFFFFF?text=打印"]', 'QQ: 6789012', '["campus-delivery"]', '图书馆', 1, 145, 11, 4, 1, NOW(), NOW());

-- 分类 11: 其他
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(3, '多肉植物盆栽', '可爱多肉，带盆出售，好养易活', 15.00, NULL, 11, '["https://placehold.co/300x200/50C878/FFFFFF?text=多肉"]', '微信：plantlover', '["face-to-face"]', '校园花坛边', 1, 123, 10, 3, 1, NOW(), NOW()),
(4, '吉他初学者转让', '全新民谣吉他，送琴包拨片背带，仅试弹', 300.00, 599.00, 11, '["https://placehold.co/300x200/FFA500/FFFFFF?text=吉他"]', '电话：13700137001', '["face-to-face", "delivery"]', '艺术学院', 1, 189, 15, 5, 1, NOW(), NOW());

-- ========================================
-- 2. 已售出/下架商品示例
-- ========================================
INSERT INTO `products` (`seller_id`, `title`, `description`, `price`, `original_price`, `category_id`, `images`, `contact_info`, `trade_methods`, `location`, `status`, `view_count`, `like_count`, `favorite_count`, `is_hot`, `created_at`, `updated_at`) VALUES
(5, 'Switch 游戏机（已售出）', '国行续航版，已破解，送 10 款热门游戏，成色新', 1800.00, 2500.00, 7, '["https://placehold.co/300x200/E6E6FA/FFFFFF?text=Switch"]', '微信：gameplayer', '["face-to-face"]', '校内', 2, 567, 45, 12, 1, NOW(), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(6, '某考研资料（已下架）', '因版权问题暂时下架', 50.00, 100.00, 6, '["https://placehold.co/300x200/CCCCCC/FFFFFF?text=资料"]', 'QQ: 7890123', '["face-to-face"]', '教学楼', 0, 123, 10, 2, 0, NOW(), DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ========================================
-- 3. 插入测试收藏数据
-- ========================================
INSERT INTO `product_favorites` (`user_id`, `product_id`, `created_at`, `updated_at`) VALUES
(1, 1, NOW(), NOW()),
(1, 3, NOW(), NOW()),
(1, 6, NOW(), NOW()),
(1, 10, NOW(), NOW()),
(2, 2, NOW(), NOW()),
(2, 9, NOW(), NOW()),
(2, 15, NOW(), NOW()),
(3, 1, NOW(), NOW()),
(3, 4, NOW(), NOW()),
(3, 7, NOW(), NOW()),
(4, 5, NOW(), NOW()),
(4, 8, NOW(), NOW()),
(4, 11, NOW(), NOW()),
(5, 6, NOW(), NOW()),
(5, 10, NOW(), NOW()),
(5, 11, NOW(), NOW()),
(6, 1, NOW(), NOW()),
(6, 2, NOW(), NOW()),
(7, 3, NOW(), NOW()),
(7, 4, NOW(), NOW()),
(8, 5, NOW(), NOW()),
(8, 6, NOW(), NOW()),
(9, 7, NOW(), NOW()),
(9, 8, NOW(), NOW()),
(10, 9, NOW(), NOW()),
(10, 10, NOW(), NOW()),
(11, 11, NOW(), NOW());

-- ========================================
-- 4. 插入测试浏览记录
-- ========================================
INSERT INTO `product_views` (`user_id`, `product_id`, `view_time`, `ip_address`, `device_type`) VALUES
(1, 1, NOW(), '192.168.1.100', 'desktop'),
(1, 2, NOW(), '192.168.1.100', 'desktop'),
(1, 3, NOW(), '192.168.1.100', 'mobile'),
(2, 3, NOW(), '192.168.1.101', 'mobile'),
(2, 4, NOW(), '192.168.1.101', 'mobile'),
(2, 5, NOW(), '192.168.1.101', 'desktop'),
(3, 5, NOW(), '192.168.1.102', 'desktop'),
(3, 6, NOW(), '192.168.1.102', 'mobile'),
(4, 6, NOW(), '192.168.1.103', 'mobile'),
(4, 7, NOW(), '192.168.1.103', 'desktop'),
(5, 7, NOW(), '192.168.1.104', 'desktop'),
(5, 8, NOW(), '192.168.1.104', 'mobile'),
(6, 8, NOW(), '192.168.1.105', 'mobile'),
(6, 9, NOW(), '192.168.1.105', 'desktop'),
(7, 9, NOW(), '192.168.1.106', 'desktop'),
(7, 10, NOW(), '192.168.1.106', 'mobile'),
(8, 10, NOW(), '192.168.1.107', 'mobile'),
(8, 11, NOW(), '192.168.1.107', 'desktop'),
(9, 11, NOW(), '192.168.1.108', 'desktop'),
(9, 1, NOW(), '192.168.1.108', 'mobile'),
(10, 2, NOW(), '192.168.1.109', 'mobile'),
(10, 3, NOW(), '192.168.1.109', 'desktop'),
(11, 4, NOW(), '192.168.1.110', 'desktop'),
(11, 5, NOW(), '192.168.1.110', 'mobile');

-- ========================================
-- 5. 更新商品的实际收藏数和浏览量
-- ========================================
UPDATE `products` SET 
    `favorite_count` = (SELECT COUNT(*) FROM `product_favorites` WHERE `product_id` = `products`.`id`),
    `view_count` = `view_count` + (SELECT COUNT(*) FROM `product_views` WHERE `product_id` = `products`.`id`)
WHERE `id` > 0;

-- ========================================
-- 6. 验证数据
-- ========================================
-- 查看商品总数
SELECT '商品总数:' as info, COUNT(*) as count FROM products;

-- 查看各分类商品数量
SELECT c.name as category, COUNT(p.id) as product_count 
FROM categories c 
LEFT JOIN products p ON c.id = p.category_id 
GROUP BY c.id, c.name 
ORDER BY c.sort_order;

-- 查看热门商品（前 5）
SELECT id, title, price, view_count, like_count, favorite_count, is_hot
FROM products
WHERE status = 1 AND deleted_at IS NULL
ORDER BY view_count DESC, like_count DESC
LIMIT 5;

-- 查看收藏最多的商品（前 5）
SELECT id, title, favorite_count
FROM products
WHERE status = 1 AND deleted_at IS NULL
ORDER BY favorite_count DESC
LIMIT 5;
