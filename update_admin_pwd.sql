UPDATE admin_user 
SET password = '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iDJfYR5sYpF5qE9qQhN7sP8xGK2C'
WHERE username = 'admin';

SELECT 'Password updated successfully!' as result;
