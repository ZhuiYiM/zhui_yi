-- 插入自定义标签测试数据
-- 用于测试用户提交自定义标签功能

-- 插入几条测试数据（status=pending 表示待审核）
INSERT INTO topic_tag (name, type, status, created_by, usage_count, trend_score, is_active, created_at, updated_at, description)
VALUES 
('测试标签 1', 'custom', 'pending', 1, 0, 0.0, false, NOW(), NOW(), '这是一个测试标签'),
('测试标签 2', 'custom', 'pending', 1, 0, 0.0, false, NOW(), NOW(), '另一个测试标签'),
('美食探店', 'custom', 'pending', 1, 0, 0.0, false, NOW(), NOW(), '分享校园周边美食探店体验'),
('考研资料', 'custom', 'pending', 1, 0, 0.0, false, NOW(), NOW(), '考研复习资料分享'),
('二手交易', 'custom', 'active', 1, 5, 0.5, true, NOW(), NOW(), '校园二手物品交易');

-- 查询插入的测试数据
SELECT * FROM topic_tag WHERE type = 'custom' ORDER BY id DESC LIMIT 10;
