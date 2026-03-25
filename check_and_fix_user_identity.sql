-- 检查并修复用户认证相关的数据结构
USE campus_db;

-- 1. 检查 users 表的关键字段
SELECT '===== users 表结构 =====' AS info;
DESC users;

-- 2. 检查 user_verifications 表结构
SELECT '===== user_verifications 表结构 =====' AS info;
DESC user_verifications;

-- 3. 查看当前的认证申请数据
SELECT '===== 当前认证申请数据 =====' AS info;
SELECT 
    id,
    user_id,
    verification_type,
    status,
    extra_info,
    submitted_at
FROM user_verifications
ORDER BY submitted_at DESC
LIMIT 5;

-- 4. 查看已通过认证的用户信息
SELECT '===== 已通过认证的用户信息 =====' AS info;
SELECT 
    u.id,
    u.username,
    u.student_id,
    u.department,
    u.is_verified,
    u.is_staff,
    u.is_merchant,
    u.is_organization,
    u.real_name,
    v.verification_type,
    v.extra_info
FROM users u
LEFT JOIN user_verifications v ON u.id = v.user_id AND v.status = 'approved'
WHERE v.id IS NOT NULL;

-- 5. 测试更新（如果需要手动修复数据）
-- 注意：以下 SQL 仅供检查，不要直接执行
SELECT '===== 测试数据修复 SQL（仅供参考）=====' AS info;
SELECT CONCAT(
    'UPDATE users SET ',
    'student_id = ''', JSON_EXTRACT(extra_info, '$.staffId'), ''', ',
    'department = ''', JSON_EXTRACT(extra_info, '$.department'), ''', ',
    'is_staff = 1, ',
    'is_verified = 1 ',
    'WHERE id = ', user_id, ';'
) AS fix_sql
FROM user_verifications
WHERE verification_type = 'staff' 
  AND status = 'approved'
  AND extra_info IS NOT NULL;
