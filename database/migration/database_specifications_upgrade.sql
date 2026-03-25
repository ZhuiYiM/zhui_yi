-- ========================================
-- 校园信息平台 - 商品规格功能数据库升级
-- 创建时间：2026-03-17
-- 说明：添加商品规格自定义支持和订单规格关联
-- ========================================

USE campus_db;

-- ========================================
-- 1. 创建商品规格表
-- ========================================
CREATE TABLE IF NOT EXISTS `product_specifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '规格 ID',
    `product_id` INT NOT NULL COMMENT '商品 ID',
    `spec_name` VARCHAR(50) NOT NULL COMMENT '规格名称（如：颜色、尺寸）',
    `spec_value` VARCHAR(100) NOT NULL COMMENT '规格值（如：红色、XL）',
    `spec_icon` VARCHAR(255) DEFAULT NULL COMMENT '规格图标或颜色代码（可选）',
    `stock_quantity` INT DEFAULT 0 COMMENT '该规格的库存数量',
    `price_adjustment` DECIMAL(10,2) DEFAULT 0.00 COMMENT '价格调整（可为负数）',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否为默认规格：0-否，1-是',
    `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_product_id` (`product_id`),
    FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品规格表';

-- ========================================
-- 2. 创建订单规格关联表
-- ========================================
CREATE TABLE IF NOT EXISTS `order_specifications` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '关联 ID',
    `order_id` INT NOT NULL COMMENT '订单 ID',
    `spec_id` INT NOT NULL COMMENT '规格 ID',
    `spec_name` VARCHAR(50) NOT NULL COMMENT '规格名称（冗余存储，防止原规格被修改）',
    `spec_value` VARCHAR(100) NOT NULL COMMENT '规格值（冗余存储）',
    `spec_icon` VARCHAR(255) DEFAULT NULL COMMENT '规格图标',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_spec_id` (`spec_id`),
    FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`spec_id`) REFERENCES `product_specifications`(`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单规格关联表';

-- ========================================
-- 3. 更新 orders 表添加必要字段
-- ========================================
-- 使用存储过程来安全地添加列（如果列不存在）

DELIMITER $$

-- 添加 buyer_contact 列
CREATE PROCEDURE AddBuyerContactIfNotExists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA='campus_db' 
        AND TABLE_NAME='orders' 
        AND COLUMN_NAME='buyer_contact'
    ) THEN
        ALTER TABLE `orders` ADD COLUMN `buyer_contact` VARCHAR(50) DEFAULT NULL COMMENT '买家联系方式' AFTER `total_amount`;
    END IF;
END$$

-- 添加 buyer_message 列
CREATE PROCEDURE AddBuyerMessageIfNotExists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA='campus_db' 
        AND TABLE_NAME='orders' 
        AND COLUMN_NAME='buyer_message'
    ) THEN
        ALTER TABLE `orders` ADD COLUMN `buyer_message` TEXT DEFAULT NULL COMMENT '买家留言' AFTER `buyer_contact`;
    END IF;
END$$

-- 添加 payment_method 列
CREATE PROCEDURE AddPaymentMethodIfNotExists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA='campus_db' 
        AND TABLE_NAME='orders' 
        AND COLUMN_NAME='payment_method'
    ) THEN
        ALTER TABLE `orders` ADD COLUMN `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式：wechat-微信，alipay-支付宝，station-站内支付' AFTER `buyer_message`;
    END IF;
END$$

-- 添加 payment_status 列
CREATE PROCEDURE AddPaymentStatusIfNotExists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA='campus_db' 
        AND TABLE_NAME='orders' 
        AND COLUMN_NAME='payment_status'
    ) THEN
        ALTER TABLE `orders` ADD COLUMN `payment_status` TINYINT DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付' AFTER `payment_method`;
    END IF;
END$$

-- 添加 payment_time 列
CREATE PROCEDURE AddPaymentTimeIfNotExists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA='campus_db' 
        AND TABLE_NAME='orders' 
        AND COLUMN_NAME='payment_time'
    ) THEN
        ALTER TABLE `orders` ADD COLUMN `payment_time` TIMESTAMP NULL DEFAULT NULL COMMENT '支付时间' AFTER `payment_status`;
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL AddBuyerContactIfNotExists();
CALL AddBuyerMessageIfNotExists();
CALL AddPaymentMethodIfNotExists();
CALL AddPaymentStatusIfNotExists();
CALL AddPaymentTimeIfNotExists();

-- 修改 order_status 列的注释（如果存在）
ALTER TABLE `orders` MODIFY COLUMN `order_status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待付款，1-待发货，2-已发货，3-已完成，4-已取消';

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS AddBuyerContactIfNotExists;
DROP PROCEDURE IF EXISTS AddBuyerMessageIfNotExists;
DROP PROCEDURE IF EXISTS AddPaymentMethodIfNotExists;
DROP PROCEDURE IF EXISTS AddPaymentStatusIfNotExists;
DROP PROCEDURE IF EXISTS AddPaymentTimeIfNotExists;

-- ========================================
-- 4. 更新 products 表添加规格相关字段
-- ========================================
-- 使用存储过程来安全地添加列（如果列不存在）

DELIMITER $$

-- 添加 has_specifications 列
CREATE PROCEDURE AddHasSpecificationsIfNotExists()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA='campus_db' 
        AND TABLE_NAME='products' 
        AND COLUMN_NAME='has_specifications'
    ) THEN
        ALTER TABLE `products` ADD COLUMN `has_specifications` TINYINT DEFAULT 0 COMMENT '是否有规格：0-无，1-有' AFTER `status`;
    END IF;
END$$

DELIMITER ;

-- 执行存储过程
CALL AddHasSpecificationsIfNotExists();

-- 删除临时存储过程
DROP PROCEDURE IF EXISTS AddHasSpecificationsIfNotExists;

-- ========================================
-- 5. 插入测试数据 - 商品规格示例
-- ========================================
-- 为已有的部分商品添加规格
-- 注意：以下插入会根据实际商品 ID 调整，如果商品不存在则跳过

-- 示例 1: 为 ID 为 1 的商品添加配置规格（如果存在）
INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 1, '配置', 'i5/16G/512G', 5, 0.00, 1, 1 WHERE EXISTS (SELECT 1 FROM products WHERE id = 1);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 1, '配置', 'i7/16G/512G', 3, 500.00, 0, 2 WHERE EXISTS (SELECT 1 FROM products WHERE id = 1);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 1, '配置', 'i7/32G/1TB', 2, 1000.00, 0, 3 WHERE EXISTS (SELECT 1 FROM products WHERE id = 1);

-- 示例 2: 为 ID 为 2 的商品添加颜色和容量规格（如果存在）
INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 2, '颜色', '黑色', '#000000', 3, 0.00, 1, 1 WHERE EXISTS (SELECT 1 FROM products WHERE id = 2);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 2, '颜色', '白色', '#FFFFFF', 2, 0.00, 0, 2 WHERE EXISTS (SELECT 1 FROM products WHERE id = 2);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 2, '颜色', '红色', '#FF0000', 2, 100.00, 0, 3 WHERE EXISTS (SELECT 1 FROM products WHERE id = 2);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 2, '容量', '128G', '', 5, 0.00, 1, 1 WHERE EXISTS (SELECT 1 FROM products WHERE id = 2);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 2, '容量', '256G', '', 2, 200.00, 0, 2 WHERE EXISTS (SELECT 1 FROM products WHERE id = 2);

-- 示例 3: 为 ID 为 10 的商品添加口味和包装规格（如果存在）
INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 10, '口味', '巧克力味', 20, 0.00, 1, 1 WHERE EXISTS (SELECT 1 FROM products WHERE id = 10);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 10, '口味', '草莓味', 15, 0.00, 0, 2 WHERE EXISTS (SELECT 1 FROM products WHERE id = 10);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 10, '口味', '抹茶味', 15, 2.00, 0, 3 WHERE EXISTS (SELECT 1 FROM products WHERE id = 10);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 10, '包装', '简装', 30, 0.00, 1, 1 WHERE EXISTS (SELECT 1 FROM products WHERE id = 10);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 10, '包装', '礼盒装', 10, 10.00, 0, 2 WHERE EXISTS (SELECT 1 FROM products WHERE id = 10);

-- 示例 4: 为 ID 为 19 的商品添加颜色规格（如果存在）
INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 19, '颜色', '白色', '#FFFFFF', 10, 0.00, 1, 1 WHERE EXISTS (SELECT 1 FROM products WHERE id = 19);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 19, '颜色', '粉色', '#FFC0CB', 8, 0.00, 0, 2 WHERE EXISTS (SELECT 1 FROM products WHERE id = 19);

INSERT INTO `product_specifications` (`product_id`, `spec_name`, `spec_value`, `spec_icon`, `stock_quantity`, `price_adjustment`, `is_default`, `sort_order`)
SELECT 19, '颜色', '黑色', '#000000', 5, 5.00, 0, 3 WHERE EXISTS (SELECT 1 FROM products WHERE id = 19);

-- ========================================
-- 6. 验证数据
-- ========================================
-- 查看规格总数
SELECT '规格总数:' as info, COUNT(*) as count FROM product_specifications;

-- 查看各商品的规格情况
SELECT p.id, p.title, COUNT(ps.id) as spec_count
FROM products p
LEFT JOIN product_specifications ps ON p.id = ps.product_id
WHERE p.status = 1
GROUP BY p.id, p.title
HAVING spec_count > 0;

-- 查看某个商品的所有规格
SELECT ps.* 
FROM product_specifications ps
WHERE ps.product_id = 1
ORDER BY ps.sort_order, ps.spec_name, ps.spec_value;
