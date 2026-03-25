-- 验证用户身份更新功能
USE campus_db;

-- 1. 查看当前用户状态
SELECT '===== 修复前的用户状态 =====' AS info;
SELECT 
    id,
    username,
    student_id,
    department,
    is_verified,
    is_staff,
    is_merchant,
    is_organization,
    real_name
FROM users
WHERE id = 4;

-- 2. 查看该用户的认证申请
SELECT '===== 用户 4 的认证申请 =====' AS info;
SELECT 
    id,
    verification_type,
    status,
    extra_info,
    submitted_at,
    reviewed_at
FROM user_verifications
WHERE user_id = 4
ORDER BY submitted_at DESC
LIMIT 1;

-- 3. 手动执行更新（模拟审核通过后的效果）
SELECT '===== 执行更新 SQL =====' AS info;

UPDATE users SET 
    student_id = JSON_UNQUOTE(JSON_EXTRACT(
        (SELECT extra_info FROM user_verifications WHERE user_id = 4 AND status = 'approved' ORDER BY submitted_at DESC LIMIT 1),
        '$.staffId'
    )),
    department = JSON_UNQUOTE(JSON_EXTRACT(
        (SELECT extra_info FROM user_verifications WHERE user_id = 4 AND status = 'approved' ORDER BY submitted_at DESC LIMIT 1),
        '$.department'
    )),
    is_staff = 1,
    is_verified = 1
WHERE id = 4;

-- 4. 验证更新结果
SELECT '===== 修复后的用户状态 =====' AS info;
SELECT 
    id,
    username,
    student_id AS '工号',
    department AS '部门',
    is_verified AS '是否认证',
    is_staff AS '是否教职工',
    is_merchant AS '是否商户',
    is_organization AS '是否组织',
    real_name
FROM users
WHERE id = 4;

-- 5. 验证查询（检查所有已认证但未更新的用户）
SELECT '===== 检查其他可能需要更新的用户 =====' AS info;
SELECT 
    u.id,
    u.username,
    u.is_verified,
    u.is_staff,
    u.is_merchant,
    u.is_organization,
    v.verification_type,
    v.status
FROM users u
LEFT JOIN user_verifications v ON u.id = v.user_id AND v.status = 'approved'
WHERE v.id IS NOT NULL
  AND (
    (v.verification_type = 'staff' AND (u.is_staff = 0 OR u.is_verified = 0))
    OR (v.verification_type = 'student' AND (u.is_verified = 0))
    OR (v.verification_type = 'merchant' AND (u.is_merchant = 0 OR u.is_verified = 0))
    OR (v.verification_type = 'organization' AND (u.is_organization = 0 OR u.is_verified = 0))
  );
