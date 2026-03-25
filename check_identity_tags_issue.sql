-- ========================================
-- 检查身份标签系统问题
-- ========================================

USE campus_db;

-- 1. 查看 identity_tags 表的所有数据
SELECT '===== 1. identity_tags 表所有数据 =====' AS message;
SELECT id, code, name, icon, description, sort_order, is_active, created_at
FROM identity_tags
ORDER BY sort_order, id;

-- 2. 查看特定用户 (userId=4) 的完整信息
SELECT '===== 2. 用户 ID=4 的完整信息 =====' AS message;
SELECT 
    id,
    username,
    real_name,
    role,
    is_admin,
    is_merchant,
    is_organization,
    student_id,
    is_real_name_verified,
    created_at
FROM users
WHERE id = 4;

-- 3. 查看该用户的身份认证信息
SELECT '===== 3. 用户 ID=4 的身份认证信息 =====' AS message;
SELECT * FROM user_verifications WHERE userId = 4;

-- 4. 查看该用户的身份标签信息
SELECT '===== 4. 用户 ID=4 的身份标签 (user_identity 表) =====' AS message;
SELECT * FROM user_identity WHERE userId = 4;

-- 5. 测试：按照后端的 determineLevel1Tag 逻辑判断用户 ID=4 的身份
SELECT '===== 5. 后端逻辑判断用户身份 =====' AS message;
SELECT 
    u.id,
    u.username,
    u.role,
    u.is_admin,
    u.is_merchant,
    u.is_organization,
    u.student_id,
    u.is_real_name_verified,
    CASE
        WHEN u.role = 'admin' OR u.is_admin = 1 THEN 'admin (管理员)'
        WHEN u.student_id IS NOT NULL AND u.student_id != '' THEN 'student (学生)'
        WHEN u.is_merchant = 1 THEN 'merchant (商户)'
        WHEN u.is_organization = 1 THEN 'organization (团体)'
        WHEN u.is_real_name_verified = 1 THEN 'society (社会)'
        ELSE 'society (社会)'
    END AS expected_level1_tag
FROM users u
WHERE u.id = 4;

-- 6. 查看 JWT Token 中解析的身份信息 (如果有的话)
-- 这个需要查看实际请求中的 Token，这里只是示例
SELECT '===== 6. 提示 =====' AS message;
SELECT '请检查浏览器开发者工具中 /api/tags/level1 请求的 Authorization header，查看 JWT Token 中的 identities 字段' AS tip;
