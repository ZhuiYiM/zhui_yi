-- 举报记录表
CREATE TABLE IF NOT EXISTS `reports` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '举报 ID',
  `reporter_id` BIGINT NOT NULL COMMENT '举报人 ID',
  `target_id` BIGINT NOT NULL COMMENT '被举报对象 ID（话题 ID 或评论 ID）',
  `target_type` VARCHAR(20) NOT NULL COMMENT '被举报类型：topic-话题、comment-评论',
  `report_type` VARCHAR(50) NOT NULL COMMENT '举报类型：spam-垃圾广告、illegal-违法违规、fraud-诈骗、harassment-骚扰、other-其他',
  `reason` TEXT COMMENT '举报原因描述',
  `evidence` TEXT COMMENT '证据',
  `status` TINYINT DEFAULT 0 COMMENT '处理状态：0-待处理、1-已处理、2-已忽略',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '举报时间',
  `processed_at` DATETIME COMMENT '处理时间',
  `processor_id` BIGINT COMMENT '处理人 ID（管理员）',
  `process_result` TEXT COMMENT '处理结果说明',
  INDEX idx_target (`target_id`, `target_type`),
  INDEX idx_status (`status`),
  INDEX idx_reporter (`reporter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='举报记录表';
