-- 订单取消原因统计表
CREATE TABLE IF NOT EXISTS `order_cancel_reason` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL COMMENT '订单 ID',
  `buyer_id` int(11) NOT NULL COMMENT '买家 ID',
  `seller_id` int(11) NOT NULL COMMENT '卖家 ID',
  `reason` varchar(50) NOT NULL COMMENT '取消原因：不想买了、信息填写错误、卖家同意退款、其他原因',
  `note` text COMMENT '补充说明',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_reason` (`reason`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单取消原因统计表';
