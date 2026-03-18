-- ============================================
-- 消息中心功能测试数据
-- ============================================

-- 1. 准备测试用户数据
INSERT INTO `users` (`id`, `username`, `password`, `student_id`, `avatar_url`, `email`) VALUES
(1001, 'test_user1', '$2a$10$XoLvF5C2dz9.7JHK8sN.TOxl9yYp4CqQKQ3N7KJ8L9M0O1P2Q3R4S', '2021001', 'https://placehold.co/100x100/4A90E2/FFFFFF?text=U1', 'user1@test.com'),
(1002, 'test_user2', '$2a$10$XoLvF5C2dz9.7JHK8sN.TOxl9yYp4CqQKQ3N7KJ8L9M0O1P2Q3R4S', '2021002', 'https://placehold.co/100x100/67C23A/FFFFFF?text=U2', 'user2@test.com'),
(1003, 'test_user3', '$2a$10$XoLvF5C2dz9.7JHK8sN.TOxl9yYp4CqQKQ3N7KJ8L9M0O1P2Q3R4S', '2021003', 'https://placehold.co/100x100/F56C6C/FFFFFF?text=U3', 'user3@test.com');

-- 2. 插入测试消息数据
-- 2.1 用户 1 发送给用户 2 的私信
INSERT INTO `messages` (`sender_id`, `receiver_id`, `content`, `message_type`, `is_read`, `created_at`) VALUES
(1001, 1002, '你好，我是用户 1', 'private_message', 0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1001, 1002, '今天天气不错', 'private_message', 0, DATE_SUB(NOW(), INTERVAL 12 HOUR)),
(1001, 1002, '一起去图书馆吗？', 'private_message', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR));

-- 2.2 用户 2 发送给用户 1 的私信（回复）
INSERT INTO `messages` (`sender_id`, `receiver_id`, `content`, `message_type`, `is_read`, `created_at`) VALUES
(1002, 1001, '你好啊！', 'private_message', 1, DATE_SUB(NOW(), INTERVAL 23 HOUR)),
(1002, 1001, '好呀，几点钟？', 'private_message', 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 2.3 用户 3 发送给用户 1 的私信
INSERT INTO `messages` (`sender_id`, `receiver_id`, `content`, `message_type`, `is_read`, `created_at`) VALUES
(1003, 1001, '你好，我想咨询一下学习问题', 'private_message', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR));

-- 2.4 系统消息
INSERT INTO `messages` (`sender_id`, `receiver_id`, `content`, `message_type`, `is_read`, `created_at`) VALUES
(1, 1001, '欢迎加入校园信息平台！', 'system', 1, DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, 1001, '您的账号已通过认证', 'system', 1, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(1, 1002, '系统维护通知', 'system', 0, DATE_SUB(NOW(), INTERVAL 1 DAY));

-- 2.5 订单通知
INSERT INTO `messages` (`sender_id`, `receiver_id`, `content`, `message_type`, `is_read`, `created_at`) VALUES
(1002, 1001, '您有新的订单', 'order_notification', 0, DATE_SUB(NOW(), INTERVAL 3 HOUR));

-- 3. 验证数据
-- 3.1 查询用户 1 收到的所有消息
SELECT 
    m.id,
    m.sender_id,
    u.username as sender_name,
    u.avatar_url as sender_avatar,
    m.content,
    m.message_type,
    m.is_read,
    m.created_at
FROM messages m
LEFT JOIN users u ON m.sender_id = u.id
WHERE m.receiver_id = 1001
ORDER BY m.created_at DESC;

-- 3.2 查询用户 1 的私信对话（聚合）
SELECT 
    m.sender_id,
    u.username as sender_name,
    u.avatar_url as sender_avatar,
    COUNT(*) as message_count,
    SUM(CASE WHEN m.is_read = 0 THEN 1 ELSE 0 END) as unread_count,
    MAX(m.created_at) as last_message_time,
    (SELECT content FROM messages 
     WHERE sender_id = m.sender_id AND receiver_id = 1001 
     ORDER BY created_at DESC LIMIT 1) as last_message
FROM messages m
LEFT JOIN users u ON m.sender_id = u.id
WHERE m.receiver_id = 1001 AND m.message_type = 'private_message'
GROUP BY m.sender_id, u.username, u.avatar_url
ORDER BY last_message_time DESC;

-- 3.3 查询用户 1 的未读消息总数
SELECT COUNT(*) as unread_count 
FROM messages 
WHERE receiver_id = 1001 AND is_read = 0;

-- 3.4 查询用户 1 的未读私信数量
SELECT COUNT(*) as unread_private_count 
FROM messages 
WHERE receiver_id = 1001 AND is_read = 0 AND message_type = 'private_message';

-- 4. 清理测试数据（执行测试后使用）
-- DELETE FROM messages WHERE sender_id IN (1001, 1002, 1003) OR receiver_id IN (1001, 1002, 1003);
-- DELETE FROM users WHERE id IN (1001, 1002, 1003);
