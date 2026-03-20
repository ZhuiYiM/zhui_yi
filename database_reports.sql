-- 举报记录表
DROP TABLE IF EXISTS `reports`;

CREATE TABLE `reports` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '举报 ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人 ID',
  `target_id` BIGINT NOT NULL COMMENT '被举报对象 ID（话题 ID、商品 ID、用户 ID、评论 ID）',
  `type` VARCHAR(20) NOT NULL COMMENT '举报类型：topic-话题、product-商品、user-用户、comment-评论',
  `reason` VARCHAR(100) NOT NULL COMMENT '举报原因',
  `description` TEXT COMMENT '详细描述',
  `status` TINYINT DEFAULT 0 COMMENT '处理状态：0-待处理、1-处理中、2-已处理、3-已忽略',
  `process_note` TEXT COMMENT '处理说明',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX idx_type (`type`),
  INDEX idx_status (`status`),
  INDEX idx_reporter (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报记录表';

-- 插入测试数据
INSERT INTO `reports` (`reporter_id`, `target_id`, `type`, `reason`, `description`, `status`) VALUES
(1, 1, 'topic', 'spam', 'Advertising content', 0),
(1, 2, 'topic', 'harassment', 'Inappropriate behavior', 1),
(1, 1, 'product', 'fraud', 'False product information', 2),
(1, 1, 'user', 'harassment', 'Malicious harassment', 0),
(1, 1, 'comment', 'spam', 'Comment contains ads', 3);
