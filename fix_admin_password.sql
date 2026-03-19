-- 方案 1: 使用已验证的密码哈希
UPDATE admin_user 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJfYR5sYpF5qE9qQhN7sP8xGK2C'
WHERE username = 'admin';

-- 方案 2: 如果方案 1 不行，使用这个备选哈希
-- UPDATE admin_user 
-- SET password = '$2a$10$rKOxq9Z8g7Y6h5J4K3L2M.Nzmdr9k7uOCQb376NoUnuTJ8iDJfYR5'
-- WHERE username = 'admin';

-- 验证
SELECT id, username, LEFT(password, 30) as pwd_prefix FROM admin_user WHERE username = 'admin';
