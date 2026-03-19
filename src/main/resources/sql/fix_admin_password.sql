-- 修复 admin_user 表的密码
-- 原始密码：admin123
-- 使用 CHAR 函数避免 PowerShell 的 $ 符号问题

-- 直接更新数据库中 admin 账号的密码
UPDATE `admin_user` 
SET `password` = '$2a$10$rKOxq9Z8g7Y6h5J4K3L2M.Nzmdr9k7uOCQb376NoUnuTJ8iDJfYR5'
WHERE `username` = 'admin';

-- 验证更新结果
SELECT username, SUBSTRING(password, 1, 15) as pwd_start, LENGTH(password) as len FROM admin_user WHERE username='admin';
