-- 检查用户 ID=4 的身份信息
SELECT '=== users 表 ===' AS info;
SELECT id, username, is_staff, is_merchant, is_organization, student_id, is_verified 
FROM users WHERE id = 4;

SELECT '=== user_identity 表 ===' AS info;
SELECT id, user_id, identity_type, identity_name, verified, verified_at, created_at 
FROM user_identity WHERE user_id = 4;

SELECT '=== user_verifications 表（最近 5 条） ===' AS info;
SELECT id, user_id, verification_type, status, submitted_at, reviewed_at, reviewer_id 
FROM user_verifications 
WHERE user_id = 4 
ORDER BY submitted_at DESC 
LIMIT 5;
