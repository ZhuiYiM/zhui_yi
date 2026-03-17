-- 订单评价表
CREATE TABLE IF NOT EXISTS `order_review` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL COMMENT '订单 ID',
  `product_id` int(11) NOT NULL COMMENT '商品 ID',
  `buyer_id` int(11) NOT NULL COMMENT '买家 ID',
  `seller_id` int(11) NOT NULL COMMENT '卖家 ID',
  `rating` int(1) NOT NULL COMMENT '评分 1-5 星',
  `content` text COMMENT '评价内容',
  `images` text COMMENT '评价图片，多张用逗号分隔',
  `reply` text COMMENT '卖家回复',
  `reply_time` datetime COMMENT '回复时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_seller_id` (`seller_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单评价表';
