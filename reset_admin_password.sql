-- Quick fix: Reset admin password to admin123
-- Execute this SQL directly in MySQL

UPDATE admin_user 
SET password = '$2a$10$rKOxq9Z8g7Y6h5J4K3L2M.Nzmdr9k7uOCQb376NoUnuTJ8iDJfYR5'
WHERE username = 'admin';

-- Verify
SELECT id, username, password FROM admin_user WHERE username = 'admin';
